# Medsafe Frontend

Applicazione Angular per la gestione dei referti medici, integrata con un backend Spring Boot.

## 🚀 Deploy su Azure

**Per deployare l'applicazione su Azure App Service, consulta la guida completa:**

👉 **[DEPLOY_GUIDE.md](./DEPLOY_GUIDE.md)** 👈

La guida include:
- ✅ Setup Azure App Service con GitHub Actions (in Italy North)
- ✅ Configurazione Microsoft Entra ID (Azure AD)  
- ✅ EasyAuth (autenticazione gestita da Azure)
- ✅ Integrazione con il backend Spring Boot
- ✅ Troubleshooting e monitoraggio

**Nota**: Azure Static Web Apps non è disponibile nelle regioni consentite, quindi usiamo Azure App Service.

---

## Caratteristiche

- **Lista Referti**: Visualizza e cerca referti per codice fiscale, email medico o tipo esame
- **Carica Referto**: Form completo per caricare nuovi referti con file (PDF, JPG, PNG)
- **Download**: Scarica PDF generati e immagini diagnostiche
- **Responsive**: UI moderna e responsive con navigazione semplice

## Tecnologie

- Angular 18+ (Standalone Components)
- TypeScript
- RxJS
- SCSS
- HttpClient per chiamate API REST

## Prerequisiti

- Node.js (v20+)
- npm (v10+)
- Backend Spring Boot attivo su `http://localhost:8080`

## Installazione

```bash
npm install
```

## Avvio Locale

```bash
npm start
# oppure
ng serve
```

Apri il browser su `http://localhost:4200`

## Build per Produzione

```bash
ng build --configuration production
```

I file compilati saranno in `dist/medsafe-frontend/`

## Configurazione Backend

- **Locale**: `src/environments/environment.ts` → `http://localhost:8080`
- **Produzione (Azure)**: `src/environments/environment.prod.ts` → URL Azure

## Struttura Progetto

```
src/
├── app/
│   ├── components/
│   │   ├── referti-list/       # Lista e ricerca referti
│   │   └── referti-upload/     # Form caricamento referto
│   ├── services/
│   │   └── referti.service.ts  # API service per backend
│   ├── app.component.ts        # Component principale con navbar
│   ├── app.routes.ts           # Routing configurazione
│   └── app.config.ts           # App configuration
├── environments/               # Configurazioni ambiente
└── styles.scss                # Stili globali
```

## API Endpoint (Backend Spring Boot)

- `GET /referti/codiceFiscale?value=CF` - Cerca per codice fiscale
- `GET /referti/email?value=EMAIL` - Cerca per email medico
- `GET /referti/tipoEsame?value=TIPO` - Cerca per tipo esame
- `POST /referti` - Carica nuovo referto (multipart/form-data)
- `GET /referti/download/pdf/{id}` - Scarica PDF
- `GET /referti/download/immagine/{id}` - Scarica immagine

## Deploy su Azure

1. Build produzione:
   ```bash
   ng build --configuration production
   ```

2. Deploy su Azure Static Web Apps:
   - Crea risorsa Azure Static Web App
   - Collega repository GitHub o carica `dist/` manualmente
   - Configura `environment.prod.ts` con URL backend Azure

3. Backend su Azure App Service:
   - Spring Boot su Azure App Service
   - Abilita CORS per permettere richieste dal frontend

## CORS Configuration (Backend)

Aggiungi al controller Spring Boot:

```java
@CrossOrigin(origins = "http://localhost:4200")
// Per produzione:
@CrossOrigin(origins = "https://your-azure-app.azurewebsites.net")
```

## Comandi Utili

- `npm start` - Avvia dev server
- `ng build` - Build progetto
- `ng test` - Esegui test
- `ng generate component nome` - Genera nuovo componente
- `ng help` - Aiuto Angular CLI

## Licenza

Progetto universitario - UNISA - Cloud Computing 2026