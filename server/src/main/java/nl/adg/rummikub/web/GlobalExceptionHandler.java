package nl.adg.rummikub.web;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import nl.adg.rummikub.game.exception.GameAlreadyStartedException;
import nl.adg.rummikub.game.exception.GameNotFinishedException;
import nl.adg.rummikub.game.exception.GameNotStartedException;
import nl.adg.rummikub.game.exception.SessionNotFoundException;
import nl.adg.rummikub.generated.game.model.ErrorResponseDto;
import nl.adg.rummikub.rules.IllegalMoveException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Turns domain exceptions into the ErrorResponse body the specs declare. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(
            SessionNotFoundException ex, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler({
        GameAlreadyStartedException.class,
        GameNotStartedException.class,
        GameNotFinishedException.class
    })
    public ResponseEntity<ErrorResponseDto> handleConflict(RuntimeException ex, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler({IllegalMoveException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponseDto> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    private static ResponseEntity<ErrorResponseDto> response(
            HttpStatus status, @Nullable String message, HttpServletRequest request) {
        // An SSE response has text/event-stream locked in, so there is no JSON converter for the
        // body. Send the status alone instead of failing to serialise ErrorResponseDto.
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        if (accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) {
            return ResponseEntity.status(status).build();
        }
        String body = Objects.requireNonNullElse(message, status.getReasonPhrase());
        return ResponseEntity.status(status).body(new ErrorResponseDto(body, status.value()));
    }
}
