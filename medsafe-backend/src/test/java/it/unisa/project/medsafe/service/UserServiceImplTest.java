package it.unisa.project.medsafe.service;

import it.unisa.project.medsafe.entity.Genere;
import it.unisa.project.medsafe.entity.Specializzazione;
import it.unisa.project.medsafe.entity.User;
import it.unisa.project.medsafe.entity.UserRole;
import it.unisa.project.medsafe.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test UserServiceImpl")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User medicoUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        medicoUser = User.builder()
                .id(1)
                .email("medico1@medsafe.local")
                .fullName("Dr. Mario Rossi")
                .azureOid("azure-oid-123")
                .genere(Genere.MASCHIO)
                .specializzazione(Specializzazione.CARDIOLOGIA)
                .role(UserRole.MEDICO)
                .enabled(true)
                .createdAt(LocalDateTime.now().minusDays(10))
                .build();

        adminUser = User.builder()
                .id(2)
                .email("admin@medsafe.local")
                .fullName("Admin Test")
                .azureOid("azure-oid-456")
                .genere(Genere.NON_SPECIFICATO)
                .specializzazione(Specializzazione.NESSUNA)
                .role(UserRole.ADMIN)
                .enabled(true)
                .createdAt(LocalDateTime.now().minusDays(30))
                .build();
    }

    // ==================== TEST syncUserFromAzureAd ====================

    @Test
    @DisplayName("Crea nuovo utente da Azure AD se non esiste")
    void testSyncCreaNuovoUtente() {
        // Arrange
        when(userRepository.findByEmail("newmedico@hospital.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(3);
            return user;
        });

        // Act
        User result = userService.syncUserFromAzureAd(
                "newmedico@hospital.com",
                "Dr. Luigi Verdi",
                "azure-oid-789",
                UserRole.MEDICO);

        // Assert
        assertNotNull(result);
        assertEquals("newmedico@hospital.com", result.getEmail());
        assertEquals("Dr. Luigi Verdi", result.getFullName());
        assertEquals("azure-oid-789", result.getAzureOid());
        assertEquals(UserRole.MEDICO, result.getRole());
        assertTrue(result.isEnabled());

        // Verify save called
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Aggiorna utente esistente da Azure AD")
    void testSyncAggiornaUtenteEsistente() {
        // Arrange
        when(userRepository.findByEmail("medico1@medsafe.local")).thenReturn(Optional.of(medicoUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.syncUserFromAzureAd(
                "medico1@medsafe.local",
                "Dr. Mario Rossi UPDATED",
                "azure-oid-999",
                UserRole.ADMIN // Cambia ruolo
        );

        // Assert
        assertNotNull(result);
        assertEquals("Dr. Mario Rossi UPDATED", result.getFullName());
        assertEquals("azure-oid-999", result.getAzureOid());
        assertEquals(UserRole.ADMIN, result.getRole());

        // Verify save called
        verify(userRepository).save(medicoUser);
    }

    // ==================== TEST findByEmail ====================

    @Test
    @DisplayName("Trova utente per email esistente")
    void testFindByEmailEsistente() {
        // Arrange
        when(userRepository.findByEmail("medico1@medsafe.local")).thenReturn(Optional.of(medicoUser));

        // Act
        Optional<User> result = userService.findByEmail("medico1@medsafe.local");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("medico1@medsafe.local", result.get().getEmail());
        verify(userRepository).findByEmail("medico1@medsafe.local");
    }

    @Test
    @DisplayName("Non trova utente per email inesistente")
    void testFindByEmailInesistente() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByEmail("nonexistent@test.com");

        // Assert
        assertFalse(result.isPresent());
    }

    // ==================== TEST getAllUsers ====================

    @Test
    @DisplayName("Ritorna tutti gli utenti")
    void testGetAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(medicoUser, adminUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(medicoUser));
        assertTrue(result.contains(adminUser));
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Ritorna lista vuota se nessun utente")
    void testGetAllUsersVuoto() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of());

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertTrue(result.isEmpty());
    }

    // ==================== TEST disableUser ====================

    @Test
    @DisplayName("Disabilita utente esistente")
    void testDisableUser() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(medicoUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertTrue(medicoUser.isEnabled()); // Inizialmente abilitato

        // Act
        boolean result = userService.disableUser(1);

        // Assert
        assertTrue(result);
        assertFalse(medicoUser.isEnabled());
        verify(userRepository).save(medicoUser);
    }

    @Test
    @DisplayName("disableUser ritorna false se utente non esiste")
    void testDisableUserNonEsiste() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        boolean result = userService.disableUser(999);

        // Assert
        assertFalse(result);
        verify(userRepository, never()).save(any());
    }

    // ==================== TEST enableUser ====================

    @Test
    @DisplayName("Abilita utente disabilitato")
    void testEnableUser() {
        // Arrange
        medicoUser.setEnabled(false); // Disabilita per il test
        when(userRepository.findById(1)).thenReturn(Optional.of(medicoUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertFalse(medicoUser.isEnabled()); // Inizialmente disabilitato

        // Act
        boolean result = userService.enableUser(1);

        // Assert
        assertTrue(result);
        assertTrue(medicoUser.isEnabled());
        verify(userRepository).save(medicoUser);
    }

    @Test
    @DisplayName("enableUser ritorna false se utente non esiste")
    void testEnableUserNonEsiste() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        boolean result = userService.enableUser(999);

        // Assert
        assertFalse(result);
        verify(userRepository, never()).save(any());
    }

    // ==================== TEST Edge Cases ====================

    @Test
    @DisplayName("Disabilita e riabilita utente sequenzialmente")
    void testDisableEnableSequenza() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(medicoUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert - Inizialmente abilitato
        assertTrue(medicoUser.isEnabled());

        // Disabilita
        userService.disableUser(1);
        assertFalse(medicoUser.isEnabled());

        // Abilita
        userService.enableUser(1);
        assertTrue(medicoUser.isEnabled());

        // Verify save chiamato 2 volte
        verify(userRepository, times(2)).save(medicoUser);
    }

    @Test
    @DisplayName("syncUserFromAzureAd preserva createdAt per utenti esistenti")
    void testSyncPreservaCreatedAt() {
        // Arrange
        LocalDateTime originalCreatedAt = medicoUser.getCreatedAt();
        when(userRepository.findByEmail("medico1@medsafe.local")).thenReturn(Optional.of(medicoUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userService.syncUserFromAzureAd(
                "medico1@medsafe.local",
                "Updated Name",
                "new-oid",
                UserRole.MEDICO);

        // Assert - createdAt deve rimanere uguale
        assertEquals(originalCreatedAt, medicoUser.getCreatedAt());
    }

    // ==================== TEST updateUserProfile ====================

    @Test
    @DisplayName("updateUserProfile aggiorna genere e specializzazione")
    void testUpdateUserProfile() {
        // Arrange
        when(userRepository.findByEmail("medico1@medsafe.local")).thenReturn(Optional.of(medicoUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<User> result = userService.updateUserProfile(
                "medico1@medsafe.local",
                Genere.FEMMINA,
                Specializzazione.PEDIATRIA);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(Genere.FEMMINA, result.get().getGenere());
        assertEquals(Specializzazione.PEDIATRIA, result.get().getSpecializzazione());
        verify(userRepository).save(medicoUser);
    }

    @Test
    @DisplayName("updateUserProfile ritorna empty per utente non trovato")
    void testUpdateUserProfileUtenteNonTrovato() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.updateUserProfile(
                "nonexistent@test.com",
                Genere.MASCHIO,
                Specializzazione.CARDIOLOGIA);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUserProfile accetta valori null")
    void testUpdateUserProfileConNull() {
        // Arrange
        when(userRepository.findByEmail("medico1@medsafe.local")).thenReturn(Optional.of(medicoUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<User> result = userService.updateUserProfile(
                "medico1@medsafe.local",
                null,
                null);

        // Assert
        assertTrue(result.isPresent());
        assertNull(result.get().getGenere());
        assertNull(result.get().getSpecializzazione());
    }
}
