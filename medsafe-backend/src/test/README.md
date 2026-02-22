# 🧪 MedSafe Backend — Suite di Test

Documentazione completa della suite di test del backend MedSafe.  
**215 test** totali • **87% di coverage istruzioni** • Soglia minima JaCoCo: **80%**

---

## 📋 Indice

- [Esecuzione dei test](#esecuzione-dei-test)
- [Struttura della suite](#struttura-della-suite)
- [Descrizione delle classi di test](#descrizione-delle-classi-di-test)
- [Coverage](#coverage)
- [Profilo di test](#profilo-di-test)

---

## ▶️ Esecuzione dei test

```bash
# Esegui tutti i test con reportistica JaCoCo
./mvnw test -Dspring.profiles.active=test

# Solo verifica coverage (fallisce se < 80%)
./mvnw test -Dspring.profiles.active=test jacoco:check

# Report HTML navigabile (target/site/jacoco/index.html)
./mvnw test -Dspring.profiles.active=test jacoco:report
```

---

## 🗂️ Struttura della suite

```
src/test/java/it/unisa/project/medsafe/
│
├── MedsafeApplicationTests.java          # Smoke test del contesto Spring
├── BasePojoTest.java                     # Classe base per test POJO con OpenPOJO
│
├── controller/
│   ├── RefertoControllerTest.java        # Test API REST referti (MockMvc)
│   └── UserControllerTest.java           # Test API REST utenti (MockMvc)
│
├── dto/
│   └── RefertoDTOTest.java              # Test POJO RefertoDTO (builder, equals, toString)
│
├── entity/
│   ├── GenereTest.java                  # Test enum Genere
│   ├── RefertoTest.java                 # Test entity Referto (builder, costruttori)
│   ├── SpecializzazioneTest.java        # Test enum Specializzazione
│   └── TipoEsameTest.java              # Test enum TipoEsame
│
├── exception/
│   ├── GlobalExceptionHandlerTest.java  # Test handler centrale eccezioni HTTP
│   └── UnauthorizedExceptionTest.java   # Test eccezione 403 personalizzata
│
├── repository/
│   └── RefertoRepositoryTest.java      # Test repository JPA con H2 in-memory
│
├── service/
│   ├── AuthorizationServiceTest.java   # Test logica autorizzazione JWT/ruoli
│   ├── PdfServiceTest.java             # Test generazione PDF con OpenPDF
│   ├── RefertoServiceTest.java         # Test service gestione referti
│   └── UserServiceImplTest.java        # Test service utenti (sync Azure AD)
│
└── utils/
    └── JwtHelperTest.java              # Test helper estrazione claims JWT
```

---

## 📄 Descrizione delle classi di test

### 🏥 Controller

#### `RefertoControllerTest`
Testa i REST endpoint di `RefertoController` usando **MockMvc** con `@WebMvcTest`.  
Copre: upload referto, modifica, eliminazione, ricerca per codice fiscale, tipo esame, nome file, ID.  
Usa `@MockBean` per isolare il service layer.

#### `UserControllerTest`
Testa i REST endpoint di `UserController` usando **MockMvc** con `@WebMvcTest`.  
Copre: GET `/me` (utente corrente), aggiornamento profilo, gestione ruoli.  
Testa il comportamento di auto-creazione utente al primo login.

---

### 📦 DTO

#### `RefertoDTOTest`
Verifica la correttezza dei metodi generati da Lombok (`@Builder`, `@Data`, `@AllArgsConstructor`).  
Usa `BasePojoTest` con OpenPOJO per validare in modo automatico getter, setter, equals e hashCode.

---

### 🗃️ Entity

#### `GenereTest` / `SpecializzazioneTest` / `TipoEsameTest`
Verificano che tutti i valori degli enum siano presenti e che `valueOf()` funzioni correttamente.

#### `RefertoTest`
Verifica costruttori, builder pattern e getter/setter dell'entity `Referto`.

---

### ⚠️ Exception

#### `GlobalExceptionHandlerTest`
Testa direttamente tutti i metodi del `@ControllerAdvice` senza avviare il contesto Spring:

| Metodo testato | Scenario |
|---|---|
| `handleMaxSizeException` | File troppo grande → 413 |
| `handleTypeMismatch` | Tipo parametro errato → 400 |
| `handleMissingParams` | Parametro obbligatorio mancante → 400 |
| `handleUnauthorizedException` | Accesso negato → 403 |
| `handleValidationExceptions` | Validazione bean fallita → 400 (mappa errori) |
| `handleGeneralException` | Eccezione generica → 500 |

#### `UnauthorizedExceptionTest`
Verifica che `UnauthorizedException` estenda `RuntimeException` e propaghi correttamente il messaggio.

---

### 🗄️ Repository

#### `RefertoRepositoryTest`
Test di integrazione con database **H2 in-memory** e profilo `test`.  
Copre operazioni CRUD di base (save, findById, deleteById) e query personalizzate (findByCodiceFiscale, findByTipoEsame, findByAutoreEmail, findByNomeFile).

---

### ⚙️ Service

#### `AuthorizationServiceTest`
Testa la logica di autorizzazione basata su JWT. Usa `@ExtendWith(MockitoExtension.class)`.  
Copre:
- `checkCanModifyReferto` — proprietario vs. admin vs. non autorizzato
- `checkCanAddReferto` — utente abilitato/disabilitato
- `isAdmin` — lettura ruolo dal JWT (senza accesso al DB)
- `getCurrentUserEmail` — estrazione email dal token

#### `PdfServiceTest`
Testa la generazione PDF tramite **OpenPDF**.  
Copre: PDF per referto standard, gestione allegato immagine, allegato PDF (nota su pagina separata), campi null, eccezioni I/O.

#### `RefertoServiceTest`
Testa `RefertoServiceImpl` con mock di repository, mapper, PDF service e blob storage.  
Organizzato in classi annidate:

| Classe annidata | Contenuto |
|---|---|
| `Correct` | Flussi happy-path: add, edit (senza file), get, getAll |
| `Incorrect` | Flussi di errore: ID non trovato, non autorizzato |
| `ExtraCorrect` | Branch coverage: removeReferto, addReferto IOException, editReferto con nuovo file, query empty list |

#### `UserServiceImplTest`
Testa `UserServiceImpl` con mock del repository.  
Copre: sincronizzazione utente da Azure AD (nuovo utente / aggiornamento senza sovrascrivere il ruolo), aggiornamento profilo, abilitazione/disabilitazione.

---

### 🔑 Utils

#### `JwtHelperTest`
Testa `JwtHelper` mockando il `JwtDecoder` di Spring Security.  
Copre: estrazione email, nome completo, Azure OID, verifica ruoli (`hasRole`), gestione token assente/null.

---

## 📊 Coverage

La coverage viene misurata con **JaCoCo 0.8.13** ad ogni esecuzione di `mvn test`.

### Riepilogo

| Package | Istruzioni | Linee |
|---|:---:|:---:|
| `entity` | 99% | ~100% |
| `rest` (controllers) | 87% | 87% |
| `service` | 82% | 83% |
| `utils` | 76% | 76% |
| `exception` | 100% | 100% |
| **Totale** | **87%** | **~85%** |

### Classi escluse dal conteggio

Le seguenti classi sono escluse dalla coverage perché non sono codice applicativo scritto a mano:

| Classe | Motivo |
|---|---|
| `MedsafeApplication` | Entry point Spring Boot |
| `config/**` | Classi di configurazione infrastrutturale |
| `BlobStorageServiceImpl` | Richiede connessione Azure reale |
| `*MapperImpl` | Auto-generato da MapStruct a compile time |

### Soglia minima

Il build **fallisce automaticamente** se la coverage scende sotto l'**80% di linee** (configurato in `pom.xml` nel plugin JaCoCo).

---

## 🔧 Profilo di test

I test usano il profilo Spring `test` (`-Dspring.profiles.active=test`), che attiva `application-test.properties`:

```properties
# Database H2 in-memory (sostituisce Azure SQL)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Disabilita Azure Key Vault
spring.cloud.azure.keyvault.secret.enabled=false
```

Questo garantisce che i test siano completamente **isolati** dall'infrastruttura Azure e possano girare in CI/CD senza credenziali cloud.

---

## 🛠️ Dipendenze di test

| Libreria | Versione | Scopo |
|---|---|---|
| Spring Boot Test | 3.5.10 | MockMvc, SpringRunner |
| JUnit 5 | (BOM) | Framework di test |
| Mockito | (BOM) | Mock e stub |
| H2 Database | (BOM) | DB in-memory per test repository |
| OpenPOJO | 0.9.1 | Test automatico POJO conventions |
| Testcontainers | 1.19.3 | (Disponibile ma non attivo) |
