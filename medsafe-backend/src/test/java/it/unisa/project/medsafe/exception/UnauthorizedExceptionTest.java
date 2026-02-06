package it.unisa.project.medsafe.exception;

import it.unisa.project.medsafe.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test UnauthorizedException")
class UnauthorizedExceptionTest {

    @Test
    @DisplayName("Crea eccezione con messaggio")
    void testCreazioneConMessaggio() {
        // Arrange & Act
        String message = "Non sei autorizzato a eseguire questa operazione";
        UnauthorizedException exception = new UnauthorizedException(message);

        // Assert
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Crea eccezione con messaggio e causa")
    void testCreazioneConMessaggioECausa() {
        // Arrange & Act
        String message = "Errore di autorizzazione";
        Throwable cause = new RuntimeException("Causa dell'errore");
        UnauthorizedException exception = new UnauthorizedException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("Causa dell'errore", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("Eccezione è istanza di RuntimeException")
    void testIstanzaDiRuntimeException() {
        // Arrange & Act
        UnauthorizedException exception = new UnauthorizedException("Test");

        // Assert
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("Eccezione può essere lanciata e catturata")
    void testLancioECattura() {
        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            throw new UnauthorizedException("Test exception");
        });
    }

    @Test
    @DisplayName("Messaggio contiene informazioni dettagliate")
    void testMessaggioDettagliato() {
        // Arrange
        String autoreEmail = "medico1@medsafe.local";
        String message = String.format(
                "Non sei autorizzato a modificare questo referto. " +
                "Solo il medico che lo ha creato (%s) o un amministratore può farlo.",
                autoreEmail
        );

        // Act
        UnauthorizedException exception = new UnauthorizedException(message);

        // Assert
        assertTrue(exception.getMessage().contains("Non sei autorizzato"));
        assertTrue(exception.getMessage().contains(autoreEmail));
        assertTrue(exception.getMessage().contains("amministratore"));
    }

    @Test
    @DisplayName("Eccezione con causa nidificata")
    void testCausaNidificata() {
        // Arrange
        Throwable rootCause = new IllegalArgumentException("Root cause");
        Throwable cause = new RuntimeException("Middle cause", rootCause);

        // Act
        UnauthorizedException exception = new UnauthorizedException("Top level message", cause);

        // Assert
        assertEquals("Top level message", exception.getMessage());
        assertEquals("Middle cause", exception.getCause().getMessage());
        assertEquals("Root cause", exception.getCause().getCause().getMessage());
    }

    @Test
    @DisplayName("Stack trace è disponibile")
    void testStackTraceDisponibile() {
        // Act
        UnauthorizedException exception = new UnauthorizedException("Test");

        // Assert
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
    }
}
