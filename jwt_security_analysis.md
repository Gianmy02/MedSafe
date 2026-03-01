# 🛡️ Report: JWT Authentication Flow (Frontend -> Backend)

Il flusso si divide in 4 macro-fasi: **Generazione & Invio (Frontend)**, **Intercettazione & Validazione (Backend)**, **Conversione & Autorizzazione (Backend)**, e infine **Utilizzo Pratico (Backend)**.

## FASE 1: Generazione e Invio (Frontend)
L'utente effettua il login (gestito da MSAL) e l'applicazione Angular deve allegare il token JWT alle chiamate API dirette al backend.

### 📄 `medsafe-frontend/src/app/interceptors/auth.interceptor.ts`
* **Cosa fa:** È il "postino" del frontend. Intercetta **tutte** le richieste HTTP in uscita dall'applicazione Angular.
* **Responsabilità Concrete:**
  1. Verifica che la richiesta sia diretta all'URL del backend MedSafe (evitando di inviare il token a server esterni).
  2. Recupera il token JWT salvato in memoria (tramite `AuthService`).
  3. Modifica l'header della richiesta HTTP originale aggiungendo `Authorization: Bearer <token_jwt>`.
  4. Gestisce dinamicamente il `Content-Type` (es. non forza `application/json` se si sta inviando un file tramite `FormData`).

---

## FASE 2: Intercettazione e Validazione Criptografica (Backend)
La richiesta HTTP arriva al backend Spring Boot. Prima di poter raggiungere qualsiasi logica di business, deve superare le barriere di sicurezza.

### 📄 `medsafe-backend/src/main/java/it/unisa/project/medsafe/config/SecurityConfig.java` (o `SecurityConfigLocal.java`)
* **Cosa fa:** È la "dogana" principale dell'applicazione (la Security Filter Chain). Intercetta la richiesta HTTP in ingresso prima che tocchi i Controller.
* **Responsabilità Concrete:**
  1. **Decodifica e validazione della firma:** Usa il `JwtDecoder` (configurato con l'URL `jwkSetUri` di Microsoft) per scaricare le chiavi pubbliche di Azure AD e verificare che il token non sia stato manomesso e non sia scaduto.
  2. **Validazione dell'Audience (`AudienceValidator`):** Controlla, tramite una classe custom interna, che il campo `aud` del token corrisponda al `backendClientId` o al `FRONTEND_CLIENT_ID`. Se il token era destinato a un'altra app, la richiesta viene respinta con errore `401 Unauthorized`.

---

## FASE 3: Conversione e Integrazione nel DB MedSafe (Backend)
Il token è valido, ma Spring Security non sa ancora chi è questo utente per il sistema MedSafe (es. che ruolo ha).

### 📄 `medsafe-backend/src/main/java/it/unisa/project/medsafe/config/CustomJwtAuthenticationConverter.java`
* **Cosa fa:** È il "traduttore". Converte il token JWT generico di Azure in un utente profilato per MedSafe. Viene invocato automaticamente da `SecurityConfig` (tramite `.jwtAuthenticationConverter()`).
* **Responsabilità Concrete:**
  1. Legge i "claims" del JWT per estrarre l'informazione identificativa principale: l'indirizzo **email** dell'utente (cercando i campi `email` o `preferred_username`).
  2. Effettua una **query al database** di MedSafe (usando `UserService`) per cercare quell'email.
  3. **Mappatura Ruoli:** 
     * Se l'utente è nel DB, recupera il suo vero ruolo (es. `ADMIN`, `PAZIENTE`) e crea una `GrantedAuthority` (es. `ROLE_ADMIN`).
     * Se non è nel DB, gli assegna un ruolo temporaneo di default (`ROLE_MEDICO`).
  4. Crea l'oggetto `JwtAuthenticationToken` (che contiene il token, l'email e i ruoli calcolati) e lo salva nel **Security Context** di Spring per la durata di questa specifica richiesta.
  5. Ora l'utente è ufficialmente autenticato e autorizzato, e la richiesta può entrare nei Controller.

---

## FASE 4: Utilizzo Pratico nei Servizi (Backend)
La richiesta è finalmente nei Service (es. `RefertoServiceImpl`). Qui serve sapere chi è l'utente per eseguire operazioni (es. "assegna questo nuovo referto al medico collegato").

### 📄 `medsafe-backend/src/main/java/it/unisa/project/medsafe/utils/JwtHelper.java`
* **Cosa fa:** È lo "strumento di consultazione". È un component di utilità usato dalla logica di business per accedere ai dati dell'utente senza dover rileggere la richiesta HTTP o re-interrogare il DB.
* **Responsabilità Concrete:**
  1. Legge in modo sicuro il `JwtAuthenticationToken` dal `SecurityContextHolder` di Spring (dove il Converter lo aveva precedentemente salvato).
  2. Fornisce metodi rapidi e puliti da richiamare ovunque nel codice:
     * `getCurrentUserEmail()`: Restituisce l'email.
     * `getCurrentUserFullName()`: Restituisce il nome dal claim `name`.
     * `getCurrentUserAzureOid()`: Restituisce l'ID univoco di Azure (claim `oid`).
     * `hasRole(String role)`: Controlla se l'utente possiede una determinata `GrantedAuthority` tra quelle caricate dal `CustomJwtAuthenticationConverter`.

---

### Riassunto in pillole per lo Sviluppatore:
1. Devi aggiungere un header o customizzare la chiamata in uscita? ➔ **`auth.interceptor.ts`**
2. Devi bloccare richieste non sicure o cambiare la validazione JWT di Spring? ➔ **`SecurityConfig.java`**
3. Devi cambiare come viene assegnato il ruolo di MedSafe a un utente Azure (o che dati leggere dal token alla prima validazione)? ➔ **`CustomJwtAuthenticationConverter.java`**
4. All'interno di un tuo Service, ti serve sapere l'email di chi sta chiamando il metodo? ➔ **`JwtHelper.java`**
