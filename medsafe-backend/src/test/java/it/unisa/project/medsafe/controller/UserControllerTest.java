package it.unisa.project.medsafe.controller;

import it.unisa.project.medsafe.dto.UserDTO;
import it.unisa.project.medsafe.entity.Genere;
import it.unisa.project.medsafe.entity.Specializzazione;
import it.unisa.project.medsafe.entity.User;
import it.unisa.project.medsafe.entity.UserRole;
import it.unisa.project.medsafe.rest.UserController;
import it.unisa.project.medsafe.service.UserService;
import it.unisa.project.medsafe.utils.JwtHelper;
import it.unisa.project.medsafe.utils.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test UserController")
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtHelper jwtHelper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserController userController;

    private User medicoUser;
    private UserDTO medicoUserDTO;

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

        medicoUserDTO = UserDTO.builder()
                .id(1)
                .email("medico1@medsafe.local")
                .fullName("Dr. Mario Rossi")
                .azureOid("azure-oid-123")
                .genere(Genere.MASCHIO)
                .specializzazione(Specializzazione.CARDIOLOGIA)
                .role(UserRole.MEDICO)
                .enabled(true)
                .createdAt(medicoUser.getCreatedAt())
                .build();
    }

    // ==================== TEST getCurrentUser ====================

    @Test
    @DisplayName("GET /users/me con JWT valido")
    void testGetCurrentUserConJwt() {
        // Arrange
        when(jwtHelper.getCurrentUserEmail()).thenReturn("medico1@medsafe.local");
        when(userService.findByEmail("medico1@medsafe.local")).thenReturn(Optional.of(medicoUser));
        when(userMapper.userToUserDTO(medicoUser)).thenReturn(medicoUserDTO);

        // Act
        ResponseEntity<UserDTO> response = userController.getCurrentUser();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("medico1@medsafe.local", response.getBody().getEmail());
        assertEquals(Genere.MASCHIO, response.getBody().getGenere());
        assertEquals(Specializzazione.CARDIOLOGIA, response.getBody().getSpecializzazione());
        assertEquals(UserRole.MEDICO, response.getBody().getRole());
    }

    @Test
    @DisplayName("GET /users/me senza JWT crea utente automaticamente (primo login)")
    void testGetCurrentUserSenzaJwt() {
        // Arrange
        when(jwtHelper.getCurrentUserEmail()).thenReturn(null);
        when(userService.findByEmail(null)).thenReturn(Optional.empty());
        when(jwtHelper.getCurrentUserFullName()).thenReturn(null);
        when(jwtHelper.getCurrentUserAzureOid()).thenReturn(null);

        // Il controller crea automaticamente l'utente al primo login
        User autoCreatedUser = User.builder()
                .id(10)
                .email(null)
                .fullName(null)
                .role(UserRole.MEDICO)
                .enabled(true)
                .build();
        UserDTO autoCreatedDTO = UserDTO.builder()
                .id(10)
                .role(UserRole.MEDICO)
                .enabled(true)
                .build();

        when(userService.syncUserFromAzureAd(eq(null), any(), eq(null), eq(UserRole.MEDICO)))
                .thenReturn(autoCreatedUser);
        when(userMapper.userToUserDTO(autoCreatedUser)).thenReturn(autoCreatedDTO);

        // Act
        ResponseEntity<UserDTO> response = userController.getCurrentUser();

        // Assert - Il controller ora crea l'utente automaticamente e ritorna 200
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("GET /users/me crea utente automaticamente se non trovato (primo login)")
    void testGetCurrentUserNonTrovato() {
        // Arrange
        when(jwtHelper.getCurrentUserEmail()).thenReturn("nonexistent@test.com");
        when(userService.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());
        when(jwtHelper.getCurrentUserFullName()).thenReturn("New User");
        when(jwtHelper.getCurrentUserAzureOid()).thenReturn("oid-new");

        User autoCreatedUser = User.builder()
                .id(99)
                .email("nonexistent@test.com")
                .fullName("New User")
                .azureOid("oid-new")
                .role(UserRole.MEDICO)
                .enabled(true)
                .build();
        UserDTO autoCreatedDTO = UserDTO.builder()
                .id(99)
                .email("nonexistent@test.com")
                .fullName("New User")
                .role(UserRole.MEDICO)
                .enabled(true)
                .build();

        when(userService.syncUserFromAzureAd("nonexistent@test.com", "New User", "oid-new", UserRole.MEDICO))
                .thenReturn(autoCreatedUser);
        when(userMapper.userToUserDTO(autoCreatedUser)).thenReturn(autoCreatedDTO);

        // Act
        ResponseEntity<UserDTO> response = userController.getCurrentUser();

        // Assert - Il controller ora crea l'utente automaticamente e ritorna 200
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("nonexistent@test.com", response.getBody().getEmail());
        assertEquals(UserRole.MEDICO, response.getBody().getRole());
    }

    // ==================== TEST updateProfile ====================

    @Test
    @DisplayName("PUT /users/profile aggiorna profilo con successo")
    void testUpdateProfile() {
        // Arrange
        when(jwtHelper.getCurrentUserEmail()).thenReturn("medico1@medsafe.local");

        UserDTO requestDTO = UserDTO.builder()
                .genere(Genere.FEMMINA)
                .specializzazione(Specializzazione.PEDIATRIA)
                .build();

        User updatedUser = User.builder()
                .id(1)
                .email("medico1@medsafe.local")
                .fullName("Dr. Mario Rossi")
                .genere(Genere.FEMMINA)
                .specializzazione(Specializzazione.PEDIATRIA)
                .role(UserRole.MEDICO)
                .enabled(true)
                .build();

        UserDTO updatedDTO = UserDTO.builder()
                .id(1)
                .email("medico1@medsafe.local")
                .fullName("Dr. Mario Rossi")
                .genere(Genere.FEMMINA)
                .specializzazione(Specializzazione.PEDIATRIA)
                .role(UserRole.MEDICO)
                .enabled(true)
                .build();

        when(userService.updateUserProfile("medico1@medsafe.local", Genere.FEMMINA, Specializzazione.PEDIATRIA))
                .thenReturn(Optional.of(updatedUser));
        when(userMapper.userToUserDTO(updatedUser)).thenReturn(updatedDTO);

        // Act
        ResponseEntity<UserDTO> response = userController.updateProfile(requestDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Genere.FEMMINA, response.getBody().getGenere());
        assertEquals(Specializzazione.PEDIATRIA, response.getBody().getSpecializzazione());
    }

    @Test
    @DisplayName("PUT /users/profile ritorna 404 se utente non trovato")
    void testUpdateProfileUtenteNonTrovato() {
        // Arrange
        when(jwtHelper.getCurrentUserEmail()).thenReturn("nonexistent@test.com");

        UserDTO requestDTO = UserDTO.builder()
                .genere(Genere.MASCHIO)
                .specializzazione(Specializzazione.CARDIOLOGIA)
                .build();

        when(userService.updateUserProfile(anyString(), any(), any())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<UserDTO> response = userController.updateProfile(requestDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ==================== TEST getAllUsers ====================

    @Test
    @DisplayName("GET /users ritorna lista utenti")
    void testGetAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(medicoUser);
        List<UserDTO> usersDTO = Arrays.asList(medicoUserDTO);

        when(userService.getAllUsers()).thenReturn(users);
        when(userMapper.usersToUsersDTO(users)).thenReturn(usersDTO);

        // Act
        ResponseEntity<List<UserDTO>> response = userController.getAllUsers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    // ==================== TEST disableUser ====================

    @Test
    @DisplayName("PUT /users/{id}/disable disabilita utente")
    void testDisableUser() {
        // Arrange
        when(userService.disableUser(1)).thenReturn(true);

        // Act
        ResponseEntity<Void> response = userController.disableUser(1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("PUT /users/{id}/disable ritorna 404 se utente non trovato")
    void testDisableUserNonTrovato() {
        // Arrange
        when(userService.disableUser(999)).thenReturn(false);

        // Act
        ResponseEntity<Void> response = userController.disableUser(999);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ==================== TEST enableUser ====================

    @Test
    @DisplayName("PUT /users/{id}/enable abilita utente")
    void testEnableUser() {
        // Arrange
        when(userService.enableUser(1)).thenReturn(true);

        // Act
        ResponseEntity<Void> response = userController.enableUser(1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("PUT /users/{id}/enable ritorna 404 se utente non trovato")
    void testEnableUserNonTrovato() {
        // Arrange
        when(userService.enableUser(999)).thenReturn(false);

        // Act
        ResponseEntity<Void> response = userController.enableUser(999);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ==================== TEST getAllGeneri ====================

    @Test
    @DisplayName("GET /users/generi ritorna array di tutti i generi")
    void testGetAllGeneri() {
        // Act
        ResponseEntity<Genere[]> response = userController.getAllGeneri();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().length); // MASCHIO, FEMMINA, NON_SPECIFICATO

        // Verifica che i generi siano presenti
        List<Genere> generi = Arrays.asList(response.getBody());
        assertTrue(generi.contains(Genere.MASCHIO));
        assertTrue(generi.contains(Genere.FEMMINA));
        assertTrue(generi.contains(Genere.NON_SPECIFICATO));
    }

    @Test
    @DisplayName("GET /users/generi ogni genere ha codice e descrizione")
    void testGetAllGeneriFormat() {
        // Act
        ResponseEntity<Genere[]> response = userController.getAllGeneri();

        // Assert
        assertNotNull(response.getBody());
        Genere primo = response.getBody()[0];
        assertNotNull(primo.getCodice());
        assertNotNull(primo.getDescrizione());
    }

    // ==================== TEST getAllSpecializzazioni ====================

    @Test
    @DisplayName("GET /users/specializzazioni ritorna array di tutte le specializzazioni")
    void testGetAllSpecializzazioni() {
        // Act
        ResponseEntity<Specializzazione[]> response = userController.getAllSpecializzazioni();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(42, response.getBody().length); // Tutte le specializzazioni

        // Verifica che NESSUNA sia la prima
        assertEquals(Specializzazione.NESSUNA, response.getBody()[0]);
    }

    @Test
    @DisplayName("GET /users/specializzazioni contiene specializzazioni chiave")
    void testGetAllSpecializzazioniContenutiChiave() {
        // Act
        ResponseEntity<Specializzazione[]> response = userController.getAllSpecializzazioni();

        // Assert
        assertNotNull(response.getBody());
        List<Specializzazione> specializzazioni = Arrays.asList(response.getBody());

        assertTrue(specializzazioni.contains(Specializzazione.CARDIOLOGIA));
        assertTrue(specializzazioni.contains(Specializzazione.PEDIATRIA));
        assertTrue(specializzazioni.contains(Specializzazione.NEUROLOGIA));
        assertTrue(specializzazioni.contains(Specializzazione.NESSUNA));
    }

    @Test
    @DisplayName("GET /users/specializzazioni ogni specializzazione ha descrizione")
    void testGetAllSpecializzazioniFormat() {
        // Act
        ResponseEntity<Specializzazione[]> response = userController.getAllSpecializzazioni();

        // Assert
        assertNotNull(response.getBody());
        Specializzazione prima = response.getBody()[0];
        assertNotNull(prima.getDescrizione());
        assertEquals("Nessuna", prima.getDescrizione());
    }
}
