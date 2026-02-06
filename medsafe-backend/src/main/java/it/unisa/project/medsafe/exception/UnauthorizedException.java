package it.unisa.project.medsafe.exception;

/**
 * Eccezione lanciata quando un utente tenta di accedere a una risorsa
 * per cui non ha i permessi necessari.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
