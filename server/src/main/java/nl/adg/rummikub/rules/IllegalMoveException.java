package nl.adg.rummikub.rules;

/** A move that breaks the rules of Rummikub; answered with 400 Bad Request. */
public class IllegalMoveException extends RuntimeException {
    @java.io.Serial private static final long serialVersionUID = 1L;

    public IllegalMoveException(String message) {
        super(message);
    }
}
