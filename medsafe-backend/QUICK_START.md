# 🚀 Quick Start Guide - MedSafe con JWT

## 📋 Panoramica Rapida

Questa guida ti aiuta a testare subito il progetto **MedSafe** in modalità sviluppo locale.

---

## ⚡ Avvio Rapido (Sviluppo Locale)

### 1️⃣ Avvia i servizi Docker

```bash
cd "C:\Users\gianm\Desktop\UNIVERSITÀ\CLOUD\medsafe"
docker-compose up -d
```

**Questo avvia:**
- MySQL su `localhost:3307`
- Azurite (Azure Blob Storage emulator) su `localhost:10000`

### 2️⃣ Verifica che i container siano attivi

```bash
docker ps
```

Dovresti vedere:
- `medsafe-mysql`
- `medsafe-azurite`

### 3️⃣ Avvia l'applicazione Spring Boot

```bash
./mvnw.cmd spring-boot:run
```

**Oppure con profilo esplicito:**
```bash
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=docker
```

### 4️⃣ Verifica che l'app sia avviata

Cerca nel log:
```
Started MedsafeApplication in X.XXX seconds
⚠️  Security DISABILITATA - Ambiente: LOCAL/DOCKER/TEST
```

### 5️⃣ Accedi a Swagger

Apri il browser:
```
http://localhost:8080/swagger-ui/index.html
```

---

## 🧪 Test degli Endpoint

### ✅ Test 1: Verifica Info Utente (Nuovo Endpoint)

**Endpoint:** `GET /users/me`

**In Swagger:**
1. Espandi `Utenti` → `GET /users/me`
2. Clicca **Try it out** → **Execute**

**Risultato atteso (modalità local):**
```json
401 Unauthorized
```
Oppure se hai implementato un fallback:
```json
{
  "email": "test@medsafe.local",
  "fullName": null,
  "azureOid": null
}
```

### ✅ Test 2: Carica un Referto con autoreEmail Automatico

**Endpoint:** `POST /referti`

**In Swagger:**
1. Espandi `Referti` → `POST /referti`
2. Clicca **Try it out**
3. Compila:
   - `nomePaziente`: Mario Rossi
   - `codiceFiscale`: RSSMRA80A01H501Z
   - `tipoEsame`: TAC
   - `testoReferto`: Test referto
   - `conclusioni`: Tutto ok
   - **`autoreEmail`**: **LASCIA VUOTO** (verrà usato il fallback)
   - `nomeFile`: test_referto
   - `file`: Carica un'immagine qualsiasi (JPG/PNG/PDF)
4. Clicca **Execute**

**Risultato atteso:**
```
201 Created
```

Nel log dovresti vedere:
```
⚠️  Nessuna email trovata nel JWT, usando email di default per testing
Autore Email: test@medsafe.local
```

### ✅ Test 3: Verifica che il referto sia stato salvato

**Endpoint:** `GET /referti`

1. Espandi `Referti` → `GET /referti`
2. Clicca **Try it out** → **Execute**

**Risultato atteso:**
```json
[
  {
    "id": 1,
    "nomePaziente": "Mario Rossi",
    "codiceFiscale": "RSSMRA80A01H501Z",
    "tipoEsame": "TAC",
    "autoreEmail": "test@medsafe.local",
    ...
  }
]
```

---

## 📊 Verifica Database

### Connettiti a MySQL

```bash
docker exec -it medsafe-mysql mysql -u root -proot medsafe
```

### Verifica tabelle create

```sql
SHOW TABLES;
```

Dovresti vedere:
- `referto`
- `users` (nuova tabella!)

### Verifica contenuto tabella Users

```sql
SELECT * FROM users;
```

Se vuoi inserire utenti di test:
```sql
INSERT INTO users (email, full_name, role, enabled) VALUES
('admin@medsafe.local', 'Admin Test', 'ADMIN', TRUE),
('medico1@medsafe.local', 'Dr. Mario Rossi', 'MEDICO', TRUE);
```

---

## 🔍 Struttura File Aggiunta

### Nuovi File Creati:

```
medsafe/
├── src/main/java/it/unisa/project/medsafe/
│   ├── config/
│   │   ├── SecurityConfig.java          # ← NUOVO (Azure AD prod)
│   │   └── SecurityConfigLocal.java     # ← NUOVO (Local no auth)
│   ├── entinty/
│   │   ├── User.java                    # ← NUOVO
│   │   └── UserRole.java                # ← NUOVO (Enum)
│   ├── repository/
│   │   └── UserRepository.java          # ← NUOVO
│   ├── rest/
│   │   ├── RefertoController.java       # ← MODIFICATO (JWT helper)
│   │   └── UserController.java          # ← NUOVO
│   ├── service/
│   │   ├── UserService.java             # ← NUOVO
│   │   └── UserServiceImpl.java         # ← NUOVO
│   └── utils/
│       └── JwtHelper.java               # ← NUOVO
├── src/main/resources/
│   ├── application-azure.properties     # ← NUOVO (Prod Azure)
│   └── db/
│       └── init-users.sql               # ← NUOVO
├── AZURE_AD_SETUP.md                    # ← NUOVO (Guida Azure AD)
└── JWT_IMPLEMENTATION_SUMMARY.md        # ← NUOVO (Questo riepilogo)
```

---

## 🎯 Prossimi Passi

### 🧪 Fase 1: Testing Locale (ORA)
✅ Testa tutti gli endpoint in Swagger senza autenticazione

### 🔐 Fase 2: Setup Azure AD (QUANDO MIGRI SU AZURE)
1. Leggi `AZURE_AD_SETUP.md`
2. Registra le app in Azure AD
3. Configura ruoli e permessi
4. Assegna utenti ai ruoli

### 🚀 Fase 3: Deploy su Azure
1. Configura variabili d'ambiente nell'App Service
2. Cambia profilo da `docker` a `azure`
3. Deploy con Maven o GitHub Actions

---

## ⚠️ Troubleshooting

### Errore: "Cannot resolve symbol 'security'" nell'IDE

**Soluzione:** L'IDE deve ricaricare le dipendenze Maven.

**In IntelliJ:**
1. Clicca destro su `pom.xml`
2. **Maven** → **Reload Project**

Oppure:
```bash
./mvnw.cmd clean install -DskipTests
```

### Errore: "Port 8080 already in use"

**Soluzione:** Uccidi il processo o cambia porta:

```properties
# application.properties
server.port=8081
```

### Errore: "Could not connect to MySQL"

**Soluzione:** Verifica che Docker sia avviato:
```bash
docker-compose ps
```

Se i container non sono attivi:
```bash
docker-compose down
docker-compose up -d
```

---

## 📚 Documentazione Completa

- **`JWT_IMPLEMENTATION_SUMMARY.md`** - Riepilogo completo implementazione
- **`AZURE_AD_SETUP.md`** - Guida configurazione Azure AD
- **`HELP.md`** - Documentazione generale Spring Boot

---

## 🎉 Tutto Pronto!

Ora puoi:
1. ✅ Testare l'app in locale senza autenticazione
2. ✅ Sviluppare nuove funzionalità
3. ✅ Preparare la migrazione su Azure quando sei pronto

**Quando migri su Azure**, segui la guida `AZURE_AD_SETUP.md` per attivare l'autenticazione con Microsoft Entra ID!

---

**Buon lavoro! 🚀**
