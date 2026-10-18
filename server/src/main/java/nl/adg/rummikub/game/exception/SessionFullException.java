package nl.adg.rummikub.game.exception;

public class SessionFullException extends RuntimeException {
    @java.io.Serial private static final long serialVersionUID = 1L;
    public SessionFullException(String sessionId) {
        super("session is full: " + sessionId);
    }
}
