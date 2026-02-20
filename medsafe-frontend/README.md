# 🎨 MedSafe Frontend

SPA Angular per la gestione di referti medici, con autenticazione tramite Azure EasyAuth e Microsoft Entra ID.

[![Angular](https://img.shields.io/badge/Angular-18-DD0031.svg)](https://angular.io/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.4-3178C6.svg)](https://www.typescriptlang.org/)

---

## 📋 Stack Tecnologico

| Tecnologia | Versione | Utilizzo |
|------------|----------|----------|
| Angular | 18.x | Framework SPA |
| TypeScript | 5.4 | Linguaggio |
| RxJS | 7.8 | Programmazione reattiva |
| SCSS | — | Stili |
| Angular Material | 18.x | Componenti UI |
| MSAL Angular | 5.x | Libreria Azure (installata, non usata direttamente) |

---

## 🏗️ Struttura Progetto

```
src/app/
├── components/
│   ├── dashboard/              # Homepage con card navigazione
│   ├── referti-upload/         # Form caricamento nuovo referto
│   ├── referti-list/           # Ricerca e lista referti
│   ├── referti-edit/           # Modifica/eliminazione referti propri
│   ├── user-profile/           # Profilo utente (genere, specializzazione)
│   ├── users-list/             # Gestione utenti (solo ADMIN)
│   └── pazienti-search/        # Ricerca pazienti
├── services/
│   ├── auth.service.ts         # Gestione sessione Azure EasyAuth
│   ├── user.service.ts         # API utenti (/users/*)
│   └── referti.service.ts      # API referti (/referti/*)
├── interceptors/
│   └── auth.interceptor.ts     # Aggiunge Bearer token alle richieste API
├── models/
│   └── user.model.ts           # Interfaccia User
├── app.component.ts            # Root component con navbar e routing
├── app.routes.ts               # Configurazione routes
└── app.config.ts               # Configurazione app (standalone)

src/environments/
├── environment.ts              # Sviluppo locale (http://localhost:8080)
└── environment.prod.ts         # Produzione Azure (URL App Service)
```

---

## 🔐 Autenticazione

L'autenticazione è gestita interamente da **Azure EasyAuth** (Authentication gestita dall'App Service), non da MSAL nel codice Angular.

### Flusso

1. L'utente accede al sito → Azure EasyAuth controlla la sessione
2. Se non autenticato → reindirizzamento al login Microsoft Entra ID
3. Dopo il login → `AuthService.getUserInfo()` chiama `/.auth/me`
4. L'`id_token` viene estratto dalla risposta e salvato come token corrente
5. L'`AuthInterceptor` aggiunge `Authorization: Bearer <id_token>` a ogni richiesta verso il backend

### Componenti chiave

| File | Ruolo |
|------|-------|
| `auth.service.ts` | Chiama `/.auth/me`, gestisce token, login/logout |
| `auth.interceptor.ts` | Inietta il token JWT in tutte le richieste al backend |
| `app.component.ts` | Controlla se l'utente è autenticato, mostra Login o Navbar |

---

## 📱 Pagine

| Route | Componente | Descrizione |
|-------|-----------|-------------|
| `/` | `DashboardComponent` | Homepage con card di navigazione |
| `/upload` | `RefertiUploadComponent` | Caricamento nuovo referto (file + dati) |
| `/referti` | `RefertiListComponent` | Ricerca referti per codice fiscale o tipo esame |
| `/edit` | `RefertiEditComponent` | Modifica/elimina i propri referti |
| `/profilo` | `UserProfileComponent` | Modifica genere e specializzazione |
| `/utenti` | `UsersListComponent` | Lista utenti con toggle abilita/disabilita (ADMIN) |

---

## 🚀 Avvio Locale

### Prerequisiti

- Node.js 20+
- npm 10+
- Backend Spring Boot attivo su `http://localhost:8080`

### Installazione e avvio

```bash
# Installa dipendenze
npm install

# Avvia dev server
npm start
# oppure
ng serve

# Apri browser
http://localhost:4200
```

---

## 🏭 Build Produzione

```bash
ng build --configuration production
```

I file compilati vengono generati in `dist/medsafe-frontend/browser/`.

---

## 🔧 Configurazione Ambienti

### Sviluppo (`environment.ts`)

```typescript
apiUrl: 'http://localhost:8080'
auth: { enabled: false }
```

### Produzione (`environment.prod.ts`)

```typescript
apiUrl: 'https://medsafe-api-<id>.italynorth-01.azurewebsites.net'
auth: {
  enabled: true,
  clientId: '<frontend-client-id>',
  scopes: ['api://<backend-client-id>/user_impersonation']
}
```

---

## 🚀 Deploy su Azure

Il frontend è deployato su **Azure App Service** (non Static Web Apps, perché non disponibile nella regione Italy North).

1. Build produzione: `ng build --configuration production`
2. Il deploy avviene tramite **GitHub Actions** (`.github/workflows/main_medsafe-frontend.yml`)
3. L'autenticazione è gestita dalla configurazione **Authentication** dell'App Service (EasyAuth)

---

## 📝 Licenza

Progetto universitario — Università degli Studi di Salerno — Cloud Computing 2026