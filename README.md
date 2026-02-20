# 🏥 MedSafe — Secure Cloud Health Portal

**Piattaforma cloud-native per la gestione sicura di referti medici su Microsoft Azure.**

[![Spring Boot](https://img.shields.io/badge/Backend-Spring%20Boot%203.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Frontend-Angular%2018-DD0031.svg)](https://angular.io/)
[![Azure](https://img.shields.io/badge/Cloud-Microsoft%20Azure-0078D4.svg)](https://azure.microsoft.com/)
[![Security](https://img.shields.io/badge/Auth-Microsoft%20Entra%20ID-red.svg)](https://learn.microsoft.com/entra/identity/)

---

## 📋 Descrizione

MedSafe è un'applicazione web per medici che permette di caricare, gestire e consultare referti medici in modo sicuro. Tutti i dati sono archiviati su servizi Azure e l'accesso è protetto tramite Microsoft Entra ID (Azure Active Directory).

### Funzionalità principali

- 📋 **Gestione referti**: Upload, modifica, eliminazione e ricerca referti medici
- 📄 **Generazione PDF**: Creazione automatica di PDF professionali per ogni referto
- 🖼️ **Immagini diagnostiche**: Upload e download di immagini (PNG, JPG, PDF)
- 👥 **Gestione utenti**: Profili medici con genere e specializzazione
- 🔐 **Autenticazione SSO**: Login con account Microsoft aziendale/universitario
- 🛡️ **RBAC**: Ruoli MEDICO e ADMIN con permessi differenziati
- ☁️ **Cloud-native**: Interamente deployato su Microsoft Azure

---

## 🏗️ Architettura

```
┌─────────────────────┐     ┌──────────────────────┐
│   Angular SPA       │────▶│   Spring Boot API    │
│   Azure App Service │     │   Azure App Service  │
│   (EasyAuth)        │     │   (JWT Validation)   │
└─────────────────────┘     └──────────┬───────────┘
         │                             │
         │ /.auth/me                   ├──▶ Azure SQL Database
         │                             ├──▶ Azure Blob Storage
         ▼                             └──▶ Azure Key Vault
┌─────────────────────┐
│ Microsoft Entra ID  │
│ (Azure AD)          │
└─────────────────────┘
```

| Componente | Tecnologia | Regione Azure |
|------------|------------|---------------|
| Frontend | Angular 18 su Azure App Service | Italy North |
| Backend | Spring Boot 3.5 su Azure App Service (Linux) | Italy North |
| Database | Azure SQL Database | Italy North |
| Storage | Azure Blob Storage (immagini + PDF) | Italy North |
| Secrets | Azure Key Vault | Italy North |
| Auth | Microsoft Entra ID (multi-tenant) | Global |

---

## 🔐 Sicurezza e Azure

### Autenticazione — Microsoft Entra ID

L'autenticazione è gestita con un modello a **doppio livello**:

1. **Frontend (EasyAuth)**: Azure App Service Authentication gestisce il login OAuth2 con Microsoft Entra ID. L'utente viene reindirizzato alla pagina di login Microsoft e, dopo l'autenticazione, l'App Service crea una sessione con cookie e rende disponibile l'**id_token** all'endpoint `/.auth/me`.

2. **Backend (Spring Security + JWT)**: Il frontend invia l'`id_token` come `Bearer` token nelle richieste API. Spring Security lo valida verificando:
   - La **firma** tramite le chiavi pubbliche di Microsoft (`/common/discovery/v2.0/keys`)
   - L'**audience** (accetta sia il client ID del frontend che del backend)
   - Il `CustomJwtAuthenticationConverter` estrae l'email dal token e carica i ruoli dal database

### Flusso di autenticazione completo

```
Utente → Frontend → EasyAuth (login Microsoft) → /.auth/me (id_token)
                                                       │
Frontend → Authorization: Bearer <id_token> → Backend (Spring Security)
                                                       │
                                               CustomJwtAuthenticationConverter
                                                       │
                                               Estrae email → Carica ruoli dal DB
```

### Zero-Trust Security

- ❌ **Nessuna password** salvata nel backend (gestite da Azure AD)
- ❌ **Nessun dato sensibile** nei log (PII, token, email, URL interni)
- ✅ **JWT stateless**: Ogni richiesta è autenticata indipendentemente
- ✅ **RBAC**: Autorizzazione basata su ruoli (MEDICO/ADMIN)
- ✅ **CORS**: Configurato per accettare solo origini autorizzate

### Azure Key Vault

I segreti (connection string DB, storage account, client ID) sono gestiti tramite **Azure Key Vault** e iniettati come variabili d'ambiente nell'App Service. Nessun segreto è presente nel codice sorgente.

---

## 📁 Struttura Repository

```
MedSafe/
├── medsafe-backend/          # Spring Boot REST API
│   ├── src/main/java/        # Codice sorgente
│   ├── src/test/java/        # Test JUnit + Testcontainers
│   ├── src/main/resources/   # Configurazioni (local, docker, azure)
│   ├── docker-compose.yml    # MySQL + Azurite per sviluppo locale
│   ├── pom.xml
│   └── README.md             # Documentazione backend
├── medsafe-frontend/         # Angular SPA
│   ├── src/app/              # Componenti, servizi, interceptor
│   ├── src/environments/     # Configurazioni ambiente
│   ├── package.json
│   └── README.md             # Documentazione frontend
├── .github/workflows/        # CI/CD con GitHub Actions
└── README.md                 # Questa documentazione
```

> Per la documentazione dettagliata di ogni modulo, consulta i README nei rispettivi folder.

---

## 🚀 Avvio Locale

### Prerequisiti

- Java 21+
- Node.js 20+ e npm 10+
- Docker Desktop (opzionale, per MySQL locale)

### Backend

```bash
cd medsafe-backend

# Con Docker (MySQL + Azurite)
docker-compose up -d
./mvnw spring-boot:run

# Oppure con H2 in-memory
./mvnw spring-boot:run -Dspring.profiles.active=local

# Swagger: http://localhost:8080/swagger-ui.html
```

### Frontend

```bash
cd medsafe-frontend
npm install
npm start

# Apri http://localhost:4200
```

> ⚠️ In locale la security è disabilitata: tutte le API sono accessibili senza token.

---

## ☁️ Servizi Azure Utilizzati

| Servizio | Scopo | Tier |
|----------|-------|------|
| **App Service** (x2) | Hosting frontend + backend | F1 (Free) |
| **Azure SQL Database** | Persistenza dati |  |
| **Azure Blob Storage** | Archiviazione immagini e PDF | Standard |
| **Azure Key Vault** | Gestione segreti e connection string | Standard |
| **Microsoft Entra ID** | Autenticazione SSO, multi-tenant | Free |

### Peculiarità della configurazione Azure

- **Italy North**: Tutti i servizi sono nella regione Italy North per conformità dati
- **App Service per il frontend** (non Static Web Apps): Azure Static Web Apps non è disponibile in Italy North, quindi il frontend usa un App Service dedicato
- **EasyAuth**: L'autenticazione del frontend è gestita completamente dalla configurazione Authentication dell'App Service, riducendo il codice Angular necessario
- **Multi-tenant**: L'Issuer URL è configurato su `common` per accettare utenti da qualsiasi tenant Microsoft (es. `@studenti.unisa.it`, `@unisa.it`)
- **Doppia audience**: Il backend accetta token JWT con audience sia del frontend che del backend, per supportare l'uso dell'`id_token` da EasyAuth
- **Key Vault Integration**: Spring Cloud Azure si connette automaticamente a Key Vault per risolvere i segreti tramite `${kv-*}` nei file di properties

---

## 🧪 Testing

```bash
# Backend: JUnit 5 + Testcontainers
cd medsafe-backend
./mvnw test

# Coverage report (JaCoCo, minimo 60%)
./mvnw clean test jacoco:report

# Frontend: Karma + Jasmine
cd medsafe-frontend
npm test
```

---

## 🚀 Deploy

### Backend

```bash
cd medsafe-backend
./mvnw clean package -DskipTests
./mvnw azure-webapp:deploy
```

### Frontend

Il deploy avviene automaticamente tramite **GitHub Actions** al push su `main`:
- Workflow: `.github/workflows/main_medsafe-frontend.yml`
- Build: `ng build --configuration production`
- Deploy: Azure App Service

---

## 👤 Autore

**Gianmarco Riviello** — Università degli Studi di Salerno

Progetto per il corso di **Cloud Computing** — Anno Accademico 2025/2026

---

## 📝 Licenza

Progetto universitario — Università degli Studi di Salerno
