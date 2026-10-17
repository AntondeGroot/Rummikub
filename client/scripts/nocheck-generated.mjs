// Prefixes every generated .ts file with // @ts-nocheck. The generator's output has unused imports
// that our strict tsconfig (noUnusedLocals) rejects, and tsconfig.app.json includes all of src/, so
// it would fail the build even when nothing imports it. Call sites still get the generated types.
import { readdirSync, readFileSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';

const MARKER = '// @ts-nocheck\n';

function markTypeScriptFiles(dir) {
  for (const entry of readdirSync(dir, { withFileTypes: true })) {
    const path = join(dir, entry.name);
    if (entry.isDirectory()) {
      markTypeScriptFiles(path);
    } else if (entry.name.endsWith('.ts')) {
      const source = readFileSync(path, 'utf8');
      if (!source.startsWith(MARKER)) {
        writeFileSync(path, MARKER + source);
      }
    }
  }
}

markTypeScriptFiles('src/generated');
