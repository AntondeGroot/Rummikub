#!/bin/bash
set -e
# Resolve the repo root from this script's location (it lives in scripts/) so the relative paths
# below work no matter where the script is invoked from.
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

if [ -n "$1" ]; then
  TARGET="$1"
elif ssh -o ConnectTimeout=3 -o BatchMode=yes -o StrictHostKeyChecking=accept-new -o ConnectionAttempts=1 my-pi true 2>/dev/null; then
  TARGET=my-pi
else
  echo "⚠️  my-pi unreachable, falling back to my-pi-ext (Cloudflare Tunnel)..."
  TARGET=my-pi-ext
fi
SSH="ssh -i ~/.ssh/pi_deploy_key $TARGET"
SCP="scp -i ~/.ssh/pi_deploy_key"

echo "🔨 Linting and testing the client..."
(cd client && npm ci && npm run lint:openapi && npm run generate && npm run lint && npm run lint:styles && npm run format:check && CI=1 npx ng test --watch=false --coverage)

# The frontend profile builds the client with base-href /rummikub/ and bundles it into the WAR.
echo "🔨 Building frontend and running all backend tests..."
mvn clean verify -P frontend --file server/pom.xml

# Tomcat itself is provisioned by the GameRoom project (setup-tomcat.sh); this script only drops
# the WAR in. rummikub.war is served at /rummikub.
WEBAPPS=/var/lib/tomcat10/webapps
TOMCAT_URL=http://127.0.0.1:8090
LOG_FILE=/var/log/tomcat10/rummikub.log

if ! $SSH "test -d $WEBAPPS"; then
  echo "❌ No Tomcat on $TARGET. Run ./setup-tomcat.sh in the GameRoom project first."
  exit 1
fi

echo "⚙️  Ensuring application override config exists..."
# Tomcat's unit only lets the JVM write under /var/log/tomcat10, so the log lives there.
$SSH "
sudo mkdir -p /opt/rummikub
if [ ! -f /opt/rummikub/application-override.yaml ]; then
  sudo tee /opt/rummikub/application-override.yaml > /dev/null << 'EOF'
# Activates the rotation caps packaged in application.properties (10MB/file, 100MB total).
logging:
  file:
    name: $LOG_FILE
EOF
fi"

echo "📦 Uploading..."
$SCP server/target/rummikub.war $TARGET:/home/ubuntu/rummikub.war

echo "🚀 Deploying to Tomcat..."
# Copy under a name Tomcat ignores, then rename: a same-filesystem mv is atomic, so Tomcat never
# picks up a half-written WAR. Its autodeployer notices the new file and redeploys the app.
$SSH "sudo install -o tomcat -g tomcat -m 644 /home/ubuntu/rummikub.war $WEBAPPS/.rummikub.war.tmp \
  && sudo mv $WEBAPPS/.rummikub.war.tmp $WEBAPPS/rummikub.war && rm /home/ubuntu/rummikub.war"

# The exploded dir is recreated on redeploy, so 'newer than the WAR' rules out the old version
# still answering; then wait until Spring has started and serves the health probe.
$SSH "for i in \$(seq 1 90); do
  if [ $WEBAPPS/rummikub -nt $WEBAPPS/rummikub.war ] && curl -sf -o /dev/null $TOMCAT_URL/rummikub/health; then exit 0; fi
  sleep 2
done
echo '❌ Rummikub did not come up in Tomcat. See $LOG_FILE and journalctl -u tomcat10.'
exit 1"

# Tomcat answers on the Pi either way; the public URL only works once GameRoom's nginx.conf
# forwards /rummikub/ to Tomcat like it does for /keezen/ and /qwixx/.
if ! $SSH "grep -q '127.0.0.1:8090/rummikub/' /etc/nginx/sites-enabled/gameroom"; then
  echo "⚠️  nginx does not forward /rummikub/ to Tomcat yet, so the game is not reachable from outside."
  echo "   Add a 'location /rummikub/' block to the GameRoom nginx.conf and deploy that."
fi

echo "✅ Done."
