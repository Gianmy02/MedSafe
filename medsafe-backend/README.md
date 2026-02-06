# 🏥 MedSafe - Secure Cloud Health Portal

**Gestione sicura e archiviazione di referti medici su Azure con Microsoft Entra ID**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![Azure](https://img.shields.io/badge/Azure-Cloud-0078D4.svg)](https://azure.microsoft.com/)
[![Security](https://img.shields.io/badge/Security-JWT%20%2B%20Azure%20AD-red.svg)](https://learn.microsoft.com/azure/active-directory/)

---

## 📋 Descrizione

**MedSafe** è un'applicazione Cloud-Native per la gestione sicura di referti medici. Implementa un'architettura **Headless** con:

- 🎨 **Frontend**: Angular SPA (Azure Static Web Apps)
- ⚙️ **Backend**: Spring Boot REST API (Azure App Service)
- 🔐 **Autenticazione**: Microsoft Entra ID (Azure Active Directory) con JWT
- 💾 **Database**: Azure SQL Database
- 📁 **Storage**: Azure Blob Storage
- 🔑 **Secrets**: Azure Key Vault

### 🎯 Caratteristiche Principali

✅ **Zero-Trust Security**: Nessuna password salvata nel backend  
✅ **JWT Authentication**: Token validati con Azure AD public keys  
✅ **Role-Based Access Control (RBAC)**: MEDICO e ADMIN  
✅ **Automatic Email Extraction**: Email estratta automaticamente dal JWT  
✅ **Stateless REST API**: Scalabile e Cloud-ready  
✅ **Multi-Environment**: Profili per local, docker, azure  

---

## 📚 Documentazione

### 🚀 Quick Start
- **[QUICK_START.md](QUICK_START.md)** - Avvio rapido in 5 minuti

### 🔐 Security & JWT
- **[JWT_IMPLEMENTATION_SUMMARY.md](JWT_IMPLEMENTATION_SUMMARY.md)** - Riepilogo implementazione JWT
- **[AUTHORIZATION_STRATEGY.md](AUTHORIZATION_STRATEGY.md)** - 🆕 Gestione ruoli tramite Database
- **[AZURE_AD_SETUP.md](AZURE_AD_SETUP.md)** - Guida configurazione Azure AD passo-passo

### 🏗️ Architettura
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Diagrammi e spiegazioni architetturali

### 🎨 Frontend
- **[FRONTEND_INTEGRATION.md](FRONTEND_INTEGRATION.md)** - Integrazione Angular con MSAL

---

## 🚀 Avvio Rapido

### Prerequisiti

- ☕ Java 21+
- 🐋 Docker Desktop
- 📦 Maven (wrapper incluso)

### Sviluppo Locale (senza Azure AD)

```bash
# 1. Clona il repository
git clone <repository-url>
cd medsafe

# 2. Avvia Docker
docker-compose up -d

# 3. Avvia l'applicazione
./mvnw spring-boot:run

# 4. Apri Swagger
http://localhost:8080/swagger-ui.html
```

**✅ Pronto! Nessuna autenticazione richiesta in modalità local.**

---

## 🏗️ Struttura Progetto

```
medsafe/
├── src/main/java/it/unisa/project/medsafe/
│   ├── config/
│   │   ├── SecurityConfig.java          # Security con Azure AD (prod)
│   │   ├── SecurityConfigLocal.java     # Security disabilitata (dev)
│   │   ├── OpenApiConfig.java           # Swagger
│   │   └── BlobStorageConfig.java       # Azure Blob Storage
│   ├── entinty/
│   │   ├── Referto.java                 # Entity referti medici
│   │   ├── TipoEsame.java              # Enum tipi esame
│   │   ├── User.java                    # Entity utenti (JWT sync)
│   │   └── UserRole.java                # Enum MEDICO/ADMIN
│   ├── repository/
│   │   ├── RefertoRepository.java
│   │   └── UserRepository.java
│   ├── service/
│   │   ├── RefertoService.java
│   │   ├── UserService.java
│   │   ├── PdfService.java
│   │   └── BlobStorageService.java
│   ├── rest/
│   │   ├── RefertoController.java       # API referti
│   │   └── UserController.java          # API utenti
│   ├── utils/
│   │   ├── JwtHelper.java               # Helper JWT Azure AD
│   │   └── RefertoMapper.java           # MapStruct mapper
│   └── dto/
│       └── RefertoDTO.java
├── src/main/resources/
│   ├── application.properties
│   ├── application-local.properties
│   ├── application-docker.properties
│   └── application-azure.properties     # Configurazione Azure AD
├── docker-compose.yml                   # MySQL + Azurite
├── pom.xml
└── README.md
```

---

## 🔐 Autenticazione JWT

### Modalità Sviluppo Locale

```properties
# application.properties
spring.profiles.active=docker
```

**Comportamento:**
- ❌ Autenticazione disabilitata
- ✅ Tutti gli endpoint pubblici
- ✅ Email fallback: `test@medsafe.local`

### Modalità Produzione Azure

```properties
# application.properties
spring.profiles.active=azure
```

**Comportamento:**
- ✅ Autenticazione con Microsoft Entra ID
- ✅ JWT validato con Azure AD
- ✅ Email estratta automaticamente dal token
- ✅ RBAC attivo (MEDICO/ADMIN)

**Esempio JWT:**
```json
{
  "aud": "api://medsafe-backend",
  "email": "mario.rossi@hospital.com",
  "name": "Dr. Mario Rossi",
  "roles": ["MEDICO"],
  "oid": "uuid-univoco"
}
```

---

## 🌐 API Endpoints

### 📋 Referti

| Method | Endpoint | Ruolo | Descrizione |
|--------|----------|-------|-------------|
| `POST` | `/referti` | Autenticato | Carica nuovo referto (email da JWT) |
| `GET` | `/referti` | Autenticato | Lista tutti i referti |
| `GET` | `/referti/tipoEsame?value=TAC` | Autenticato | Filtra per tipo esame |
| `GET` | `/referti/codiceFiscale?value=...` | Autenticato | Cerca per paziente |
| `GET` | `/referti/email?value=...` | Autenticato | Referti per medico |
| `PUT` | `/referti` | Autenticato | Modifica referto |
| `DELETE` | `/referti/{id}` | **ADMIN** | Elimina referto |
| `GET` | `/referti/download/pdf/{id}` | Autenticato | Scarica PDF |
| `GET` | `/referti/download/immagine/{id}` | Autenticato | Scarica immagine |

### 👥 Utenti

| Method | Endpoint | Ruolo | Descrizione |
|--------|----------|-------|-------------|
| `GET` | `/users/me` | Autenticato | Info utente corrente |
| `GET` | `/users` | **ADMIN** | Lista tutti gli utenti |
| `PUT` | `/users/{id}/disable` | **ADMIN** | Disabilita account |
| `PUT` | `/users/{id}/enable` | **ADMIN** | Abilita account |

---

## 🧪 Testing

### Test Locali senza Autenticazione

```bash
# Avvia con profilo docker
./mvnw spring-boot:run

# Apri Swagger
http://localhost:8080/swagger-ui.html

# Testa endpoint senza token
```

### Test con JWT (Postman/Curl)

```bash
# 1. Ottieni token da Azure AD (vedi AZURE_AD_SETUP.md)

# 2. Testa API con token
curl -X GET http://localhost:8080/users/me \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Run Test Suite

```bash
# Esegui tutti i test
./mvnw test

# Test con coverage
./mvnw clean test jacoco:report

# Vedi report coverage
open target/site/jacoco/index.html
```

---

## 🚀 Deploy su Azure

### 1️⃣ Preparazione

1. Crea risorse Azure:
   - Azure App Service (Linux)
   - Azure SQL Database
   - Azure Storage Account
   - Azure Key Vault (opzionale)

2. Registra app in Azure AD (vedi `AZURE_AD_SETUP.md`)

3. Configura variabili d'ambiente nell'App Service:

```bash
AZURE_CLIENT_ID=<client-id>
AZURE_CLIENT_SECRET=<client-secret>
AZURE_TENANT_ID=<tenant-id>
AZURE_SQL_SERVER=<server-name>
AZURE_SQL_DATABASE=medsafe
AZURE_SQL_USERNAME=<admin>
AZURE_SQL_PASSWORD=<password>
AZURE_STORAGE_CONNECTION_STRING=<connection-string>
```

### 2️⃣ Deploy con Maven

```bash
# Configura pom.xml con i tuoi dettagli Azure
# Poi esegui:
./mvnw azure-webapp:deploy
```

### 3️⃣ Deploy con GitHub Actions

Vedi `.github/workflows/azure-deploy.yml` (esempio da creare)

---

## 📊 Database Schema

### Tabella: `referti`

| Campo | Tipo | Descrizione |
|-------|------|-------------|
| `id` | INT | Primary key auto-increment |
| `nome_paziente` | VARCHAR(255) | Nome del paziente |
| `codice_fiscale` | CHAR(16) | Codice fiscale |
| `tipo_esame` | ENUM | TAC, Radiografia, etc. |
| `testo_referto` | TEXT | Contenuto referto |
| `conclusioni` | TEXT | Conclusioni mediche |
| `file_url_immagine` | VARCHAR(1000) | URL immagine in Blob Storage |
| `url_pdf_generato` | VARCHAR(1000) | URL PDF generato |
| `nome_file` | VARCHAR(255) | Nome file (unique) |
| **`autore_email`** | VARCHAR(255) | **Email medico (da JWT)** |
| `data_caricamento` | TIMESTAMP | Data creazione automatica |

### Tabella: `users` (Nuova!)

| Campo | Tipo | Descrizione |
|-------|------|-------------|
| `id` | INT | Primary key auto-increment |
| `email` | VARCHAR(255) | Email univoca (da JWT) |
| `azure_oid` | VARCHAR(100) | Object ID Azure AD |
| `full_name` | VARCHAR(255) | Nome completo |
| **`role`** | ENUM | **MEDICO o ADMIN** |
| `enabled` | BOOLEAN | Account attivo |
| `created_at` | TIMESTAMP | Data creazione |
| `last_login` | TIMESTAMP | Ultimo accesso |

---

## 🔧 Configurazione

### Profili Spring

| Profilo | Uso | Security | Database |
|---------|-----|----------|----------|
| `local` | Sviluppo IDE | ❌ Disabilitata | H2 in-memory |
| `docker` | Sviluppo locale | ❌ Disabilitata | MySQL Docker |
| `azure`/`prod` | Produzione | ✅ Azure AD + JWT | Azure SQL |

### File di Configurazione

- `application-local.properties` - H2 database locale
- `application-docker.properties` - MySQL su Docker (porta 3307)
- `application-azure.properties` - Azure services con Entra ID

---

## 🤝 Contribuire

1. Fork del repository
2. Crea un branch feature (`git checkout -b feature/AmazingFeature`)
3. Commit delle modifiche (`git commit -m 'Add AmazingFeature'`)
4. Push al branch (`git push origin feature/AmazingFeature`)
5. Apri una Pull Request

---

## 📝 License

Distributed under the MIT License. See `LICENSE` for more information.

---

## 👥 Team

- **Backend**: Spring Boot REST API
- **Frontend**: Angular SPA (da implementare)
- **Cloud**: Microsoft Azure
- **Security**: Microsoft Entra ID

---

## 📞 Supporto

- 📖 **Documentazione completa**: Vedi cartella `/docs` o i file `*.md`
- 🐛 **Issue tracker**: GitHub Issues
- 💬 **Domande**: Apri una discussione su GitHub

---

## 🎓 Learning Resources

- [Microsoft Entra ID Documentation](https://learn.microsoft.com/azure/active-directory/)
- [Spring Security OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [Azure Spring Cloud](https://learn.microsoft.com/azure/developer/java/spring-framework/)
- [MSAL for Angular](https://github.com/AzureAD/microsoft-authentication-library-for-js)

---

## ✅ Checklist Implementazione

### Backend ✅
- [x] Entity Referto e User
- [x] Repository JPA
- [x] Service layer con business logic
- [x] REST Controllers
- [x] Azure Blob Storage integration
- [x] PDF generation
- [x] Security con Spring Security
- [x] JWT validation con Azure AD
- [x] JwtHelper per estrazione claims
- [x] Role-based authorization
- [x] Multi-environment configuration
- [x] Swagger/OpenAPI documentation
- [x] Unit tests con JUnit 5
- [x] Integration tests con Testcontainers

### Frontend 🔄
- [ ] Angular SPA setup
- [ ] MSAL integration
- [ ] Login/Logout components
- [ ] Referti management UI
- [ ] File upload component
- [ ] PDF viewer
- [ ] Admin dashboard

### DevOps 🔄
- [ ] GitHub Actions CI/CD
- [ ] Azure deployment automation
- [ ] Environment-specific configs
- [ ] Monitoring con Application Insights
- [ ] Logging strategy

---

**🎉 MedSafe è pronto per l'uso in sviluppo locale e deployabile su Azure!**

**Per iniziare subito:** Leggi [QUICK_START.md](QUICK_START.md)
