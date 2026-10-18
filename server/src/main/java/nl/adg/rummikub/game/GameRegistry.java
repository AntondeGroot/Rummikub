package nl.adg.rummikub.game;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameRegistry {

    private static final Map<String, GameSession> SESSIONS = new ConcurrentHashMap<>();

    private GameRegistry() {}

    public static String createGame(String roomName, int maxPlayers) {
        String id = UUID.randomUUID().toString();
        SESSIONS.put(id, new GameSession(id, roomName, maxPlayers));
        return id;
    }

    @Nullable
    public static GameSession getGame(String sessionId) {
        return SESSIONS.get(sessionId);
    }

    public static List<GameSession> getAllGames() {
        return List.copyOf(SESSIONS.values());
    }

    public static void removeGame(String sessionId) {
        SESSIONS.remove(sessionId);
    }

    // visible for testing
    public static void clear() {
        SESSIONS.clear();
    }
}
