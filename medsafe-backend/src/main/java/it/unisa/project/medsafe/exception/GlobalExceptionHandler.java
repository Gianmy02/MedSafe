package it.unisa.project.medsafe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Il file è troppo grande!");
        response.put("details", exc.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException exc) {
        String name = exc.getName();
        String type = exc.getRequiredType() != null ? exc.getRequiredType().getSimpleName() : "unknown";
        Object value = exc.getValue();
        String message = String.format("Il parametro '%s' dovrebbe essere di tipo '%s', ma hai inviato '%s'",
                name, type, value);

        Map<String, String> response = new HashMap<>();
        response.put("error", "Errore di validazione dati");
        response.put("details", message);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParams(MissingServletRequestParameterException exc) {
        String name = exc.getParameterName();
        String message = String.format("Il parametro obbligatorio '%s' è mancante", name);

        Map<String, String> response = new HashMap<>();
        response.put("error", "Dati mancanti");
        response.put("details", message);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorizedException(UnauthorizedException exc) {
        Map<String, String> response = new HashMap<>();
        response.put("error", exc.getMessage());
        response.put("status", "403 Forbidden");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException exc) {
        Map<String, String> errors = new HashMap<>();
        exc.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception exc) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Errore generico del server");
        response.put("details", exc.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }
}
