package it.unisa.project.medsafe.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    // ==================== TEST handleUnauthorizedException ====================

    @Test
    @DisplayName("handleUnauthorizedException ritorna 403 Forbidden")
    void testHandleUnauthorizedException() {
        // Arrange
        String message = "Non sei autorizzato a modificare questo referto";
        UnauthorizedException exception = new UnauthorizedException(message);

        // Act
        ResponseEntity<?> responseEntity = exceptionHandler.handleUnauthorizedException(exception);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> response = (Map<String, String>) responseEntity.getBody();
        assertNotNull(response);
        assertEquals(message, response.get("error"));
        assertEquals("403 Forbidden", response.get("status"));
    }

    @Test
    @DisplayName("handleUnauthorizedException con messaggio dettagliato")
    void testHandleUnauthorizedExceptionMessaggioDettagliato() {
        // Arrange
        String detailedMessage = "Non sei autorizzato a eliminare questo referto. " +
                "Solo il medico che lo ha creato (medico2@medsafe.local) o un amministratore può farlo.";
        UnauthorizedException exception = new UnauthorizedException(detailedMessage);

        // Act
        ResponseEntity<?> responseEntity = exceptionHandler.handleUnauthorizedException(exception);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> response = (Map<String, String>) responseEntity.getBody();
        assertTrue(response.get("error").contains("Non sei autorizzato"));
        assertTrue(response.get("error").contains("medico2@medsafe.local"));
        assertTrue(response.get("error").contains("amministratore"));
    }

    @Test
    @DisplayName("handleUnauthorizedException con diversi tipi di operazioni")
    void testHandleUnauthorizedExceptionOperazioniDiverse() {
        // Test per "modificare"
        UnauthorizedException exModifica = new UnauthorizedException(
                "Non sei autorizzato a modificare questo referto");
        ResponseEntity<?> responseEntityModifica = exceptionHandler.handleUnauthorizedException(exModifica);
        @SuppressWarnings("unchecked")
        Map<String, String> responseModifica = (Map<String, String>) responseEntityModifica.getBody();
        assertTrue(responseModifica.get("error").contains("modificare"));

        // Test per "eliminare"
        UnauthorizedException exElimina = new UnauthorizedException(
                "Non sei autorizzato a eliminare questo referto");
        ResponseEntity<?> responseEntityElimina = exceptionHandler.handleUnauthorizedException(exElimina);
        @SuppressWarnings("unchecked")
        Map<String, String> responseElimina = (Map<String, String>) responseEntityElimina.getBody();
        assertTrue(responseElimina.get("error").contains("eliminare"));
    }

    // ==================== TEST handleValidationExceptions ====================

    @Test
    @DisplayName("handleValidationExceptions ritorna mappa di errori di validazione")
    void testHandleValidationExceptions() {
        // Arrange
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "test");
        bindingResult.addError(new FieldError("test", "nomePaziente", "Il nome paziente è obbligatorio"));
        bindingResult.addError(new FieldError("test", "codiceFiscale", "Il codice fiscale non è valido"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<?> responseEntity = exceptionHandler.handleValidationExceptions(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> response = (Map<String, String>) responseEntity.getBody();
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("Il nome paziente è obbligatorio", response.get("nomePaziente"));
        assertEquals("Il codice fiscale non è valido", response.get("codiceFiscale"));
    }

    @Test
    @DisplayName("handleValidationExceptions con singolo errore")
    void testHandleValidationExceptionsSingoloErrore() {
        // Arrange
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "test");
        bindingResult.addError(new FieldError("test", "email", "Email non valida"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<?> responseEntity = exceptionHandler.handleValidationExceptions(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> response = (Map<String, String>) responseEntity.getBody();

        assertEquals(1, response.size());
        assertEquals("Email non valida", response.get("email"));
    }

    // ==================== TEST handleGenericException ====================

    @Test
    @DisplayName("handleGenericException ritorna 500 Internal Server Error")
    void testHandleGenericException() {
        // Arrange
        String message = "Si è verificato un errore imprevisto";
        Exception exception = new RuntimeException(message);

        // Act
        ResponseEntity<?> response = exceptionHandler.handleGeneralException(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals(message, body.get("error"));
    }

    @Test
    @DisplayName("handleGenericException con NullPointerException")
    void testHandleGenericExceptionNullPointer() {
        // Arrange
        Exception exception = new NullPointerException("Valore null non atteso");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleGeneralException(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("handleGenericException con IllegalArgumentException")
    void testHandleGenericExceptionIllegalArgument() {
        // Arrange
        Exception exception = new IllegalArgumentException("Argomento non valido");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleGeneralException(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Argomento non valido", body.get("error"));
    }

    // ==================== TEST Edge Cases ====================

    @Test
    @DisplayName("handleUnauthorizedException con messaggio null")
    void testHandleUnauthorizedExceptionMessaggioNull() {
        // Arrange
        UnauthorizedException exception = new UnauthorizedException(null);

        // Act
        ResponseEntity<?> responseEntity = exceptionHandler.handleUnauthorizedException(exception);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> response = (Map<String, String>) responseEntity.getBody();

        assertNotNull(response);
        assertNull(response.get("error"));
        assertEquals("403 Forbidden", response.get("status"));
    }

    @Test
    @DisplayName("handleValidationExceptions con lista errori vuota")
    void testHandleValidationExceptionsVuota() {
        // Arrange
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "test");
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<?> responseEntity = exceptionHandler.handleValidationExceptions(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> response = (Map<String, String>) responseEntity.getBody();

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    @DisplayName("handleGenericException con messaggio exception null")
    void testHandleGenericExceptionMessaggioNull() {
        // Arrange
        Exception exception = new RuntimeException((String) null);

        // Act
        ResponseEntity<?> response = exceptionHandler.handleGeneralException(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertNull(body.get("error"));
    }

    // ==================== TEST Integrazione ====================

    @Test
    @DisplayName("Tutte le eccezioni ritornano strutture consistenti")
    void testStruttureConsistenti() {
        // Arrange
        UnauthorizedException unauthorized = new UnauthorizedException("Test");
        Exception generic = new RuntimeException("Test");

        // Act
        ResponseEntity<?> unauthorizedResponse = exceptionHandler.handleUnauthorizedException(unauthorized);
        ResponseEntity<?> genericResponse = exceptionHandler.handleGeneralException(generic);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, unauthorizedResponse.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, genericResponse.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> unauthorizedBody = (Map<String, String>) unauthorizedResponse.getBody();
        @SuppressWarnings("unchecked")
        Map<String, String> genericBody = (Map<String, String>) genericResponse.getBody();

        assertNotNull(unauthorizedBody);
        assertNotNull(genericBody);
        assertTrue(unauthorizedBody.containsKey("error"));
        assertTrue(unauthorizedBody.containsKey("status"));
        assertTrue(genericBody.containsKey("error"));
    }

    @Test
    @DisplayName("handleUnauthorizedException con caratteri speciali nel messaggio")
    void testHandleUnauthorizedExceptionCaratteriSpeciali() {
        // Arrange
        String messageWithSpecialChars = "Non autorizzato: l'email 'test@example.com' non corrisponde";
        UnauthorizedException exception = new UnauthorizedException(messageWithSpecialChars);

        // Act
        ResponseEntity<?> responseEntity = exceptionHandler.handleUnauthorizedException(exception);

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, String> response = (Map<String, String>) responseEntity.getBody();
        assertEquals(messageWithSpecialChars, response.get("error"));
        assertTrue(response.get("error").contains("@"));
        assertTrue(response.get("error").contains("'"));
    }
}
