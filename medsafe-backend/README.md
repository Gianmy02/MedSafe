# ⚙️ MedSafe Backend

REST API Spring Boot per la gestione sicura di referti medici con autenticazione JWT via Microsoft Entra ID.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)

---

## 📋 Stack Tecnologico

| Tecnologia | Versione | Utilizzo |
|------------|----------|----------|
| Java | 21 | Linguaggio |
| Spring Boot | 3.5.10 | Framework |
| Spring Security | 6.x | Autenticazione JWT |
| Spring Data JPA | 6.x | Persistenza |
| MapStruct | 1.5.5 | Mapping Entity ↔ DTO |
| OpenPDF | 1.3.30 | Generazione PDF |
| SpringDoc | 2.8.4 | Swagger/OpenAPI |
| Lombok | latest | Riduzione boilerplate |
| JaCoCo | 0.8.13 | Code Coverage (min 60%) |
| Testcontainers | 1.19.3 | Integration Testing |

---

## 🏗️ Struttura Progetto

```
src/main/java/it/unisa/project/medsafe/
├── config/
│   ├── SecurityConfig.java              # Security Azure AD (profilo azure)
│   ├── SecurityConfigLocal.java         # Security disabilitata (profilo local/docker)
│   ├── CustomJwtAuthenticationConverter.java  # Conversione JWT → Authentication
│   ├── OpenApiConfig.java               # Configurazione Swagger
│   └── AzureBlobConfig.java             # Azure Blob Storage client
├── entity/
│   ├── User.java                        # Utente (email, ruolo, Azure OID)
│   ├── Referto.java                     # Referto medico
│   ├── UserRole.java                    # Enum: MEDICO, ADMIN
│   ├── TipoEsame.java                  # Enum: TAC, RADIOGRAFIA, ECOGRAFIA...
│   ├── Genere.java                      # Enum: MASCHIO, FEMMINA, NON_SPECIFICATO
│   └── Specializzazione.java           # Enum specializzazioni mediche
├── repository/
│   ├── UserRepository.java
│   └── RefertoRepository.java
├── service/
│   ├── UserService.java / UserServiceImpl.java
│   ├── RefertoService.java / RefertoServiceImpl.java
│   ├── BlobStorageService.java / BlobStorageServiceImpl.java
│   ├── PdfService.java / PdfServiceImpl.java
│   └── AuthorizationService.java        # RBAC: chi può modificare/eliminare
├── rest/
│   ├── UserController.java              # /users/**
│   └── RefertoController.java           # /referti/**
├── dto/
│   ├── RefertoDTO.java
│   └── UserDTO.java
├── utils/
│   ├── JwtHelper.java                   # Estrazione email/nome/OID dal JWT
│   ├── RefertoMapper.java              # MapStruct mapper
│   └── UserMapper.java
└── exception/
    ├── GlobalExceptionHandler.java
    ├── RefertoNotFoundException.java
    └── UnauthorizedException.java
```

---

## 🔐 Autenticazione e Autorizzazione

### Profili Spring

| Profilo | Security | Database | Uso |
|---------|----------|----------|-----|
| `local` | ❌ Disabilitata | H2 in-memory | Sviluppo IDE |
| `docker` | ❌ Disabilitata | MySQL (Docker) | Sviluppo locale |
| `azure` | ✅ JWT + Entra ID | Azure SQL | Produzione |

### Flusso JWT (profilo `azure`)

1. Il frontend invia l'**id_token** di Azure EasyAuth come `Bearer` token
2. `CustomJwtAuthenticationConverter` estrae l'email dai claim (`email` o `preferred_username`)
3. Carica i ruoli dal database (o assegna `MEDICO` al primo login)
4. `JwtHelper` fornisce metodi per estrarre email, nome e OID in qualsiasi punto del codice
5. `AuthorizationService` gestisce RBAC: ADMIN può tutto, MEDICO solo i propri referti

### Ruoli

| Ruolo | Permessi |
|-------|----------|
| `MEDICO` | CRUD sui propri referti, visualizzazione profilo |
| `ADMIN` | Tutto + gestione utenti (abilita/disabilita) |

---

## 🌐 API Endpoints

### Referti (`/referti`)

| Method | Endpoint | Auth | Descrizione |
|--------|----------|------|-------------|
| `POST` | `/referti` | ✅ | Carica nuovo referto (multipart) |
| `PUT` | `/referti` | ✅ | Modifica referto (owner/admin) |
| `DELETE` | `/referti/{id}` | ✅ | Elimina referto (owner/admin) |
| `GET` | `/referti` | ✅ | Lista tutti i referti |
| `GET` | `/referti/{id}` | ✅ | Singolo referto per ID |
| `GET` | `/referti/codiceFiscale?value=...` | ✅ | Cerca per codice fiscale |
| `GET` | `/referti/tipoEsame?value=...` | ✅ | Filtra per tipo esame |
| `GET` | `/referti/email?value=...` | ✅ | Referti per autore |
| `GET` | `/referti/download/pdf/{id}` | ✅ | Download PDF generato |
| `GET` | `/referti/download/immagine/{id}` | ✅ | Download immagine diagnostica |

### Utenti (`/users`)

| Method | Endpoint | Auth | Descrizione |
|--------|----------|------|-------------|
| `GET` | `/users/me` | ✅ | Info utente corrente (auto-crea al primo login) |
| `PUT` | `/users/profile` | ✅ | Aggiorna genere e specializzazione |
| `GET` | `/users` | 🔒 ADMIN | Lista tutti gli utenti |
| `PUT` | `/users/{id}/disable` | 🔒 ADMIN | Disabilita account |
| `PUT` | `/users/{id}/enable` | 🔒 ADMIN | Abilita account |
| `GET` | `/users/generi` | ✅ | Lista enum generi |
| `GET` | `/users/specializzazioni` | ✅ | Lista enum specializzazioni |

---

## 📊 Database Schema

### Tabella `users`

| Campo | Tipo | Descrizione |
|-------|------|-------------|
| `id` | INT (PK) | Auto-increment |
| `email` | VARCHAR(255) UNIQUE | Email da Azure AD |
| `azure_oid` | VARCHAR(100) | Object ID Azure |
| `full_name` | VARCHAR(255) | Nome completo |
| `genere` | ENUM | MASCHIO, FEMMINA, NON_SPECIFICATO |
| `specializzazione` | ENUM | Specializzazione medica |
| `role` | ENUM | MEDICO o ADMIN |
| `enabled` | BOOLEAN | Account attivo (default: true) |
| `created_at` | TIMESTAMP | Data creazione |

### Tabella `referti`

| Campo | Tipo | Descrizione |
|-------|------|-------------|
| `id` | INT (PK) | Auto-increment |
| `nome_paziente` | VARCHAR(255) | Nome del paziente |
| `codice_fiscale` | CHAR(16) | Codice fiscale |
| `tipo_esame` | ENUM | TAC, RADIOGRAFIA, ECOGRAFIA, etc. |
| `testo_referto` | TEXT | Contenuto del referto |
| `conclusioni` | TEXT | Conclusioni mediche |
| `file_url_immagine` | VARCHAR(1000) | URL immagine su Blob Storage |
| `url_pdf_generato` | VARCHAR(1000) | URL PDF su Blob Storage |
| `nome_file` | VARCHAR(255) | Nome file (unique) |
| `autore_email` | VARCHAR(255) | Email medico (da JWT) |
| `data_caricamento` | TIMESTAMP | Data creazione |

---

## 🚀 Avvio Locale

### Prerequisiti

- Java 21+
- Docker Desktop (per profilo `docker`)
- Maven (wrapper `mvnw` incluso)

### Avvio rapido

```bash
# Profilo Docker (MySQL + Azurite)
docker-compose up -d
./mvnw spring-boot:run

# Profilo locale (H2 in-memory)
./mvnw spring-boot:run -Dspring.profiles.active=local

# Swagger UI
http://localhost:8080/swagger-ui.html
```

---

## 🧪 Testing

```bash
# Esegui tutti i test
./mvnw test

# Test con report coverage (JaCoCo)
./mvnw clean test jacoco:report

# Apri report
open target/site/jacoco/index.html
```

I test usano **H2 in-memory** e **Testcontainers** (MySQL) per l'integrazione.

---

## 📁 File di Configurazione

| File | Descrizione |
|------|-------------|
| `application.properties` | Configurazione base |
| `application-local.properties` | H2 database, security disabilitata |
| `application-docker.properties` | MySQL Docker, Azurite locale |
| `application-azure.properties` | Azure SQL, Blob Storage, Key Vault, Entra ID |

---

## 🚀 Deploy su Azure

```bash
# Build del JAR
./mvnw clean package -DskipTests

# Deploy con Maven plugin
./mvnw azure-webapp:deploy
```

Il deploy è configurato per **Azure App Service Linux** nella regione `italynorth` (tier F1).

---

## 📝 Licenza

Progetto universitario — Università degli Studi di Salerno — Cloud Computing 2026
