package it.unisa.project.medsafe.service;

import it.unisa.project.medsafe.entity.Referto;
import it.unisa.project.medsafe.entity.TipoEsame;
import it.unisa.project.medsafe.entity.User;
import it.unisa.project.medsafe.entity.UserRole;
import it.unisa.project.medsafe.exception.UnauthorizedException;
import it.unisa.project.medsafe.repository.UserRepository;
import it.unisa.project.medsafe.utils.JwtHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test AuthorizationService")
class AuthorizationServiceTest {

    @Mock
    private JwtHelper jwtHelper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthorizationService authorizationService;

    private Referto refertoMedico1;
    private Referto refertoMedico2;
    private User adminUser;
    private User medicoUser;

    @BeforeEach
    void setUp() {
        // Setup referto del medico1
        refertoMedico1 = new Referto();
        refertoMedico1.setId(1);
        refertoMedico1.setAutoreEmail("medico1@medsafe.local");
        refertoMedico1.setNomePaziente("Mario Rossi");
        refertoMedico1.setCodiceFiscale("RSSMRA80A01H501Z");
        refertoMedico1.setTipoEsame(TipoEsame.TAC);
        refertoMedico1.setDataCaricamento(LocalDateTime.now());

        // Setup referto del medico2
        refertoMedico2 = new Referto();
        refertoMedico2.setId(2);
        refertoMedico2.setAutoreEmail("medico2@medsafe.local");
        refertoMedico2.setNomePaziente("Anna Verdi");
        refertoMedico2.setCodiceFiscale("VRDNNA85B41H501Y");
        refertoMedico2.setTipoEsame(TipoEsame.Radiografia);
        refertoMedico2.setDataCaricamento(LocalDateTime.now());

        // Setup admin user
        adminUser = User.builder()
                .id(1)
                .email("admin@medsafe.local")
                .fullName("Admin Test")
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();

        // Setup medico user
        medicoUser = User.builder()
                .id(2)
                .email("medico1@medsafe.local")
                .fullName("Dr. Mario Rossi")
                .role(UserRole.MEDICO)
                .enabled(true)
                .build();
    }

    // ==================== TEST checkCanModifyReferto - MODIFICA
    // ====================

    @Test
    @DisplayName("MEDICO può modificare il proprio referto")
    void testMedicoPuoModificareProprio() {
        // Arrange
        when(jwtHelper.getCurrentUserEmail()).thenReturn("medico1@medsafe.local");
        when(userRepository.findByEmail("medico1@medsafe.local")).thenReturn(Optional.of(medicoUser));
        when(jwtHelper.hasRole("ADMIN")).thenReturn(false);

        // Act & Assert - Non deve lanciare eccezione
        assertDoesNotThrow(() -> authorizationService.checkCanModifyReferto(refertoMedico1, "modificare"));

        verify(jwtHelper).getCurrentUserEmail();
    }

    @Test
    @DisplayName("MEDICO NON può modificare referto di un altro medico")
    void testMedicoNonPuoModificareAltro() {
        // Arrange
        when(jwtHelper.getCurrentUserEmail()).thenReturn("medico1@medsafe.local");
        when(userRepository.findByEmail("medico1@medsafe.local")).thenReturn(Optional.of(medicoUser));
        when(jwtHelper.hasRole("ADMIN")).thenReturn(false);

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> authorizationService.checkCanModifyReferto(refertoMedico2, "modificare"));

        assertTrue(exception.getMessage().contains("Non sei autorizzato a modificare questo referto"));
        assertTrue(exception.getMessage().contains("medico2@medsafe.local"));
    }

    @Test
    @DisplayName("ADMIN può modificare qualsiasi referto")
    void testAdminPuoModificareQualsiasi() {
        // Arrange
        when(jwtHelper.getCurrentUserEmail()).thenReturn("admin@medsafe.local");
        when(userRepository.findByEmail("admin@medsafe.local")).thenReturn(Optional.of(adminUser));
        when(jwtHelper.hasRole("ADMIN")).thenReturn(true);

        // Act & Assert - Non deve lanciare eccezione
        assertDoesNotThrow(() -> authorizationService.checkCanModifyReferto(refertoMedico1, "modificare"));
        assertDoesNotThrow(() -> authorizationService.checkCanModifyReferto(refertoMedico2, "modificare"));
    }

    @Test
    @DisplayName("ADMIN può modificare referto con verifica JWT")
    void testAdminPuoModificareConJwt() {
        // Arrange
        when(jwtHelper.getCurrentUserEmail()).thenReturn("admin@medsafe.local");
        when(userRepository.findByEmail("admin@medsafe.local")).thenReturn(Optional.of(adminUser));
        when(jwtHelper.hasRole("ADMIN")).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> authorizationService.checkCanModifyReferto(refertoMedico1, "modificare"));
    }

    // ==================== TEST checkCanModifyReferto - ELIMINA
    // ====================

    @Test
    @DisplayName("MEDICO può eliminare il proprio referto")
    void testMedicoPuoEliminareProprio() {
        // Arrange
        when(jwtHelper.getCurrentUserEmail()).thenReturn("medico1@medsafe.local");
        when(userRepository.findByEmail("medico1@medsafe.local")).thenReturn(Optional.of(medicoUser));
        when(jwtHelper.hasRole("ADMIN")).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> authorizationService.checkCanModifyReferto(refertoMedico1, "eliminare"));
    }

    @Test
    @DisplayName("MEDICO NON può eliminare referto di un altro medico")
    void testMedicoNonPuoEliminareAltro() {
        // Arrange
        when(jwtHelper.getCurrentUserEmail()).thenReturn("medico1@medsafe.local");
        when(userRepository.findByEmail("medico1@medsafe.local")).thenReturn(Optional.of(medicoUser));
        when(jwtHelper.hasRole("ADMIN")).thenReturn(false);

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> authorizationService.checkCanModifyReferto(refertoMedico2, "eliminare"));

        assertTrue(exception.getMessage().contains("Non sei autorizzato a eliminare questo referto"));
    }

    @Test
    @DisplayName("ADMIN può eliminare qualsiasi referto")
    void testAdminPuoEliminareQualsiasi() {
        // Arrange
        when(jwtHelper.getCurrentUserEmail()).thenReturn("admin@medsafe.local");
        when(userRepository.findByEmail("admin@medsafe.local")).thenReturn(Optional.of(adminUser));
        when(jwtHelper.hasRole("ADMIN")).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> authorizationService.checkCanModifyReferto(refertoMedico1, "eliminare"));
        assertDoesNotThrow(() -> authorizationService.checkCanModifyReferto(refertoMedico2, "eliminare"));
    }

    // ==================== TEST Case Insensitive Email ====================

    @Test
    @DisplayName("Verifica email case-insensitive")
    void testEmailCaseInsensitive() {
        // Arrange - Email con diverso case
        when(jwtHelper.getCurrentUserEmail()).thenReturn("MEDICO1@MEDSAFE.LOCAL");
        when(userRepository.findByEmail("MEDICO1@MEDSAFE.LOCAL")).thenReturn(Optional.of(medicoUser));
        when(jwtHelper.hasRole("ADMIN")).thenReturn(false);

        // Act & Assert - Deve funzionare ugualmente
        assertDoesNotThrow(() -> authorizationService.checkCanModifyReferto(refertoMedico1, "modificare"));
    }

    // ==================== TEST Fallback Email ====================

    @Test
    @DisplayName("Email null dal JWT causa UnauthorizedException (utente non trovato)")
    void testNullEmailThrowsUnauthorized() {
        // Arrange - Nessun fallback, email null causa "Utente non trovato"
        when(jwtHelper.getCurrentUserEmail()).thenReturn(null);
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        Referto refertoFallback = new Referto();
        refertoFallback.setId(3);
        refertoFallback.setAutoreEmail("admin@medsafe.local");

        // Act & Assert - Senza utente nel DB, viene lanciata UnauthorizedException
        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authorizationService.checkCanModifyReferto(refertoFallback, "modificare"));
        assertTrue(ex.getMessage().contains("Utente non trovato"));
    }

    @Test
    @DisplayName("Email null NON può modificare referto di altri")
    void testNullEmailNonPuoModificareAltri() {
        // Arrange
        when(jwtHelper.getCurrentUserEmail()).thenReturn(null);
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnauthorizedException.class,
                () -> authorizationService.checkCanModifyReferto(refertoMedico1, "modificare"));
    }

    // ==================== TEST Metodi Pubblici ====================

    @Test
    @DisplayName("getCurrentUserEmailPublic() ritorna email corretta")
    void testGetCurrentUserEmailPublic() {
        // Arrange
        when(jwtHelper.getCurrentUserEmail()).thenReturn("medico1@medsafe.local");

        // Act
        String email = authorizationService.getCurrentUserEmailPublic();

        // Assert
        assertEquals("medico1@medsafe.local", email);
    }

    @Test
    @DisplayName("getCurrentUserEmailPublic() ritorna null se JWT è null")
    void testGetCurrentUserEmailPublicNull() {
        // Arrange
        when(jwtHelper.getCurrentUserEmail()).thenReturn(null);

        // Act
        String email = authorizationService.getCurrentUserEmailPublic();

        // Assert - Nessun fallback, ritorna null
        assertNull(email);
    }

    @Test
    @DisplayName("isCurrentUserAdmin() ritorna true per admin dal JWT")
    void testIsCurrentUserAdminFromJwt() {
        // Arrange - isAdmin() ora controlla solo il JWT, non il DB
        when(jwtHelper.getCurrentUserEmail()).thenReturn("admin@medsafe.local");
        when(jwtHelper.hasRole("ADMIN")).thenReturn(true);

        // Act
        boolean isAdmin = authorizationService.isCurrentUserAdmin();

        // Assert
        assertTrue(isAdmin);
    }

    @Test
    @DisplayName("isCurrentUserAdmin() ritorna true per admin dal JWT")
    void testIsCurrentUserAdminJwt() {
        // Arrange
        when(jwtHelper.getCurrentUserEmail()).thenReturn("admin@azure.com");
        when(jwtHelper.hasRole("ADMIN")).thenReturn(true);

        // Act
        boolean isAdmin = authorizationService.isCurrentUserAdmin();

        // Assert
        assertTrue(isAdmin);
    }

    @Test
    @DisplayName("isCurrentUserAdmin() ritorna false per medico")
    void testIsCurrentUserAdminFalse() {
        // Arrange
        when(jwtHelper.getCurrentUserEmail()).thenReturn("medico1@medsafe.local");
        when(jwtHelper.hasRole("ADMIN")).thenReturn(false);

        // Act
        boolean isAdmin = authorizationService.isCurrentUserAdmin();

        // Assert
        assertFalse(isAdmin);
    }

    // ==================== TEST Operazioni Diverse ====================

    @Test
    @DisplayName("Messaggio di errore dinamico per diverse operazioni")
    void testMessaggioDinamico() {
        // Arrange
        when(jwtHelper.getCurrentUserEmail()).thenReturn("medico1@medsafe.local");
        when(userRepository.findByEmail("medico1@medsafe.local")).thenReturn(Optional.of(medicoUser));
        when(jwtHelper.hasRole("ADMIN")).thenReturn(false);

        // Act & Assert - Modifica
        UnauthorizedException exModifica = assertThrows(UnauthorizedException.class,
                () -> authorizationService.checkCanModifyReferto(refertoMedico2, "modificare"));
        assertTrue(exModifica.getMessage().contains("modificare"));

        // Act & Assert - Elimina
        UnauthorizedException exElimina = assertThrows(UnauthorizedException.class,
                () -> authorizationService.checkCanModifyReferto(refertoMedico2, "eliminare"));
        assertTrue(exElimina.getMessage().contains("eliminare"));

        // Act & Assert - Condividere (esempio futuro)
        UnauthorizedException exCondividere = assertThrows(UnauthorizedException.class,
                () -> authorizationService.checkCanModifyReferto(refertoMedico2, "condividere"));
        assertTrue(exCondividere.getMessage().contains("condividere"));
    }
}
