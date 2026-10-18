package nl.adg.rummikub.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nl.adg.rummikub.game.exception.GameAlreadyStartedException;
import nl.adg.rummikub.game.exception.SessionFullException;

public final class GameSession {

    /** Rummikub is played with two to four players. */
    public static final int MAX_PLAYERS = 4;

    private final String       sessionId;
    private final String       roomName;
    private final int          maxPlayers;
    private final List<Player> players = new ArrayList<>();
    private SessionStatus      status  = SessionStatus.WAITING;

    public GameSession(String sessionId, String roomName, int maxPlayers) {
        if (maxPlayers > MAX_PLAYERS)
            throw new IllegalArgumentException(
                    "Rummikub supports at most " + MAX_PLAYERS + " players, got " + maxPlayers);
        this.sessionId  = sessionId;
        this.roomName   = roomName;
        this.maxPlayers = maxPlayers;
    }

    public synchronized void addPlayer(Player player) {
        if (status != SessionStatus.WAITING) throw new GameAlreadyStartedException(sessionId);
        if (players.size() >= maxPlayers) throw new SessionFullException(sessionId);
        players.add(player);
    }

    /** Starts the game in a random turn order. */
    public synchronized void start() {
        if (status != SessionStatus.WAITING) throw new GameAlreadyStartedException(sessionId);
        Collections.shuffle(players);
        status = SessionStatus.IN_PROGRESS;
    }

    public String sessionId()                  { return sessionId; }
    public String roomName()                   { return roomName; }
    public int maxPlayers()                    { return maxPlayers; }
    public synchronized List<Player> players() { return List.copyOf(players); }
    public synchronized SessionStatus status() { return status; }
}
