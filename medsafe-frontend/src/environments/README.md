# Configurazione Environment Files

## File di Ambiente

Il progetto utilizza due file di configurazione per gestire le impostazioni in base all'ambiente:

### 📁 `environment.ts` - Sviluppo Locale
- Backend: `http://localhost:8080`
- Autenticazione: **Disabilitata** (`auth.enabled: false`)
- Usato quando esegui `ng serve` in locale

### 📁 `environment.prod.ts` - Azure Production
- Backend: `https://medsafe-api-cucqc2bydbezfsfy.italynorth-01.azurewebsites.net`
- Autenticazione: **Abilitata** (`auth.enabled: true`)
- Usato quando esegui build di produzione: `ng build --configuration production`

## 🔧 Valori Configurati

### Azure AD (Microsoft Entra ID)
```typescript
clientId: 'b05b2d51-457f-4ae1-81e5-add2bf7c3718'
authority: 'https://login.microsoftonline.com/c30767db-3dda-4dd4-8a4d-097d22cb99d3'
```

### Backend API
```typescript
apiUrl: 'https://medsafe-api-cucqc2bydbezfsfy.italynorth-01.azurewebsites.net'
```

## ⚠️ Note Importanti

1. **Non committare mai credenziali sensibili** (client secrets, password) nei file environment
2. I **secrets** devono essere configurati come **Application Settings** su Azure Static Web Apps
3. Il `redirectUri` in `environment.prod.ts` deve corrispondere all'URL dello Static Web App dopo il deploy
4. EasyAuth gestisce automaticamente l'autenticazione - non serve codice MSAL nel frontend

## 🔄 Come Cambiare Ambiente

Angular seleziona automaticamente l'environment corretto in base al comando:

- **Sviluppo**: `ng serve` → usa `environment.ts`
- **Produzione**: `ng build --configuration production` → usa `environment.prod.ts`

## 📝 Aggiornamento Post-Deploy

Dopo aver creato lo Static Web App su Azure, aggiorna:

```typescript
// src/environments/environment.prod.ts
redirectUri: 'https://YOUR-ACTUAL-URL.azurestaticapps.net'
```

Poi fai commit e push per triggherare un nuovo deploy.
