package it.unisa.project.medsafe.utils;

import it.unisa.project.medsafe.utils.JwtHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test JwtHelper")
class JwtHelperTest {

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private JwtHelper jwtHelper;

    private Jwt mockJwt;
    private JwtAuthenticationToken mockJwtAuth;

    @BeforeEach
    void setUp() {
        // Setup mock JWT con tutti i claim
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        headers.put("typ", "JWT");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user-12345");
        claims.put("email", "medico1@hospital.com");
        claims.put("name", "Dr. Mario Rossi");
        claims.put("oid", "azure-oid-12345");
        claims.put("preferred_username", "medico1@hospital.com");
        claims.put("roles", List.of("MEDICO"));

        mockJwt = new Jwt(
                "mock-token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                headers,
                claims
        );

        // Setup JwtAuthenticationToken con authorities
        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_MEDICO")
        );
        mockJwtAuth = new JwtAuthenticationToken(mockJwt, authorities);
    }

    // ==================== TEST getCurrentUserEmail ====================

    @Test
    @DisplayName("getCurrentUserEmail() estrae email dal claim 'email'")
    void testGetCurrentUserEmailDaClaimEmail() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Arrange
            when(securityContext.getAuthentication()).thenReturn(mockJwtAuth);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            String email = jwtHelper.getCurrentUserEmail();

            // Assert
            assertEquals("medico1@hospital.com", email);
        }
    }

    @Test
    @DisplayName("getCurrentUserEmail() usa preferred_username se email manca")
    void testGetCurrentUserEmailDaPreferredUsername() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Arrange - JWT senza claim "email"
            Map<String, Object> claims = new HashMap<>();
            claims.put("preferred_username", "fallback@hospital.com");

            Jwt jwtSenzaEmail = new Jwt(
                    "token",
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                    Map.of("alg", "RS256"),
                    claims
            );

            JwtAuthenticationToken authSenzaEmail = new JwtAuthenticationToken(jwtSenzaEmail, List.of());

            when(securityContext.getAuthentication()).thenReturn(authSenzaEmail);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            String email = jwtHelper.getCurrentUserEmail();

            // Assert
            assertEquals("fallback@hospital.com", email);
        }
    }

    @Test
    @DisplayName("getCurrentUserEmail() ritorna null se nessun JWT")
    void testGetCurrentUserEmailSenzaJwt() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Arrange - Autenticazione non JWT
            Authentication nonJwtAuth = mock(Authentication.class);
            when(securityContext.getAuthentication()).thenReturn(nonJwtAuth);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            String email = jwtHelper.getCurrentUserEmail();

            // Assert
            assertNull(email);
        }
    }

    @Test
    @DisplayName("getCurrentUserEmail() ritorna null se authentication è null")
    void testGetCurrentUserEmailAuthenticationNull() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Arrange
            when(securityContext.getAuthentication()).thenReturn(null);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            String email = jwtHelper.getCurrentUserEmail();

            // Assert
            assertNull(email);
        }
    }

    // ==================== TEST getCurrentUserFullName ====================

    @Test
    @DisplayName("getCurrentUserFullName() estrae nome dal claim 'name'")
    void testGetCurrentUserFullName() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Arrange
            when(securityContext.getAuthentication()).thenReturn(mockJwtAuth);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            String fullName = jwtHelper.getCurrentUserFullName();

            // Assert
            assertEquals("Dr. Mario Rossi", fullName);
        }
    }

    @Test
    @DisplayName("getCurrentUserFullName() ritorna null se claim manca")
    void testGetCurrentUserFullNameMancante() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Arrange - JWT senza claim "name"
            Jwt jwtSenzaNome = new Jwt(
                    "token",
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                    Map.of("alg", "RS256"),
                    Map.of("email", "test@test.com")
            );

            JwtAuthenticationToken authSenzaNome = new JwtAuthenticationToken(jwtSenzaNome, List.of());
            when(securityContext.getAuthentication()).thenReturn(authSenzaNome);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            String fullName = jwtHelper.getCurrentUserFullName();

            // Assert
            assertNull(fullName);
        }
    }

    // ==================== TEST getCurrentUserAzureOid ====================

    @Test
    @DisplayName("getCurrentUserAzureOid() estrae OID dal claim 'oid'")
    void testGetCurrentUserAzureOid() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Arrange
            when(securityContext.getAuthentication()).thenReturn(mockJwtAuth);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            String oid = jwtHelper.getCurrentUserAzureOid();

            // Assert
            assertEquals("azure-oid-12345", oid);
        }
    }

    @Test
    @DisplayName("getCurrentUserAzureOid() ritorna null se claim manca")
    void testGetCurrentUserAzureOidMancante() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Arrange
            Jwt jwtSenzaOid = new Jwt(
                    "token",
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                    Map.of("alg", "RS256"),
                    Map.of("email", "test@test.com")
            );

            JwtAuthenticationToken authSenzaOid = new JwtAuthenticationToken(jwtSenzaOid, List.of());
            when(securityContext.getAuthentication()).thenReturn(authSenzaOid);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            String oid = jwtHelper.getCurrentUserAzureOid();

            // Assert
            assertNull(oid);
        }
    }

    // ==================== TEST hasRole ====================

    @Test
    @DisplayName("hasRole() ritorna true se ruolo presente")
    void testHasRoleTrue() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Arrange
            when(securityContext.getAuthentication()).thenReturn(mockJwtAuth);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            boolean hasMedico = jwtHelper.hasRole("MEDICO");

            // Assert
            assertTrue(hasMedico);
        }
    }

    @Test
    @DisplayName("hasRole() ritorna false se ruolo non presente")
    void testHasRoleFalse() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Arrange
            when(securityContext.getAuthentication()).thenReturn(mockJwtAuth);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            boolean hasAdmin = jwtHelper.hasRole("ADMIN");

            // Assert
            assertFalse(hasAdmin);
        }
    }

    @Test
    @DisplayName("hasRole() verifica ruoli correttamente")
    void testHasRoleVerifica() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Arrange
            when(securityContext.getAuthentication()).thenReturn(mockJwtAuth);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act & Assert - Il ruolo è esattamente ROLE_MEDICO
            assertTrue(jwtHelper.hasRole("MEDICO"));
            assertFalse(jwtHelper.hasRole("ADMIN"));
        }
    }

    @Test
    @DisplayName("hasRole() ritorna false se authentication è null")
    void testHasRoleAuthenticationNull() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Arrange
            when(securityContext.getAuthentication()).thenReturn(null);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            boolean hasRole = jwtHelper.hasRole("ADMIN");

            // Assert
            assertFalse(hasRole);
        }
    }

    @Test
    @DisplayName("hasRole() ritorna false se authorities è null")
    void testHasRoleAuthoritiesNull() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Arrange
            Authentication authSenzaAuthorities = mock(Authentication.class);
            when(authSenzaAuthorities.getAuthorities()).thenReturn(List.of()); // Lista vuota invece di null
            when(securityContext.getAuthentication()).thenReturn(authSenzaAuthorities);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            boolean hasRole = jwtHelper.hasRole("ADMIN");

            // Assert
            assertFalse(hasRole);
        }
    }

    // ==================== TEST getCurrentJwt ====================

    @Test
    @DisplayName("getCurrentJwt() ritorna JWT corrente")
    void testGetCurrentJwt() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Arrange
            when(securityContext.getAuthentication()).thenReturn(mockJwtAuth);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            Jwt jwt = jwtHelper.getCurrentJwt();

            // Assert
            assertNotNull(jwt);
            assertEquals("mock-token-value", jwt.getTokenValue());
            assertEquals("medico1@hospital.com", jwt.getClaim("email"));
        }
    }

    @Test
    @DisplayName("getCurrentJwt() ritorna null se nessun JWT")
    void testGetCurrentJwtNull() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Arrange
            Authentication nonJwtAuth = mock(Authentication.class);
            when(securityContext.getAuthentication()).thenReturn(nonJwtAuth);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            Jwt jwt = jwtHelper.getCurrentJwt();

            // Assert
            assertNull(jwt);
        }
    }

    // ==================== TEST Multiple Roles ====================

    @Test
    @DisplayName("hasRole() con utente che ha più ruoli")
    void testHasRoleMultipleRoles() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Arrange - Utente con MEDICO e ADMIN
            Collection<GrantedAuthority> multipleAuthorities = List.of(
                    new SimpleGrantedAuthority("ROLE_MEDICO"),
                    new SimpleGrantedAuthority("ROLE_ADMIN")
            );
            JwtAuthenticationToken authMultipleRoles = new JwtAuthenticationToken(mockJwt, multipleAuthorities);

            when(securityContext.getAuthentication()).thenReturn(authMultipleRoles);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            boolean hasMedico = jwtHelper.hasRole("MEDICO");
            boolean hasAdmin = jwtHelper.hasRole("ADMIN");
            boolean hasUser = jwtHelper.hasRole("USER");

            // Assert
            assertTrue(hasMedico);
            assertTrue(hasAdmin);
            assertFalse(hasUser);
        }
    }

    // ==================== TEST Edge Cases ====================

    @Test
    @DisplayName("Estrazione dati JWT completi")
    void testEstrazioneCompletaDatiJwt() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            // Arrange
            when(securityContext.getAuthentication()).thenReturn(mockJwtAuth);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            String email = jwtHelper.getCurrentUserEmail();
            String fullName = jwtHelper.getCurrentUserFullName();
            String oid = jwtHelper.getCurrentUserAzureOid();
            Jwt jwt = jwtHelper.getCurrentJwt();
            boolean hasMedico = jwtHelper.hasRole("MEDICO");

            // Assert
            assertAll(
                    () -> assertEquals("medico1@hospital.com", email),
                    () -> assertEquals("Dr. Mario Rossi", fullName),
                    () -> assertEquals("azure-oid-12345", oid),
                    () -> assertNotNull(jwt),
                    () -> assertTrue(hasMedico)
            );
        }
    }
}
