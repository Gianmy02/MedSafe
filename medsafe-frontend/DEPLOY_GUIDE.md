# 🚀 Deploy MedSafe Frontend su Azure App Service

## Guida Completa per il Deployment

### 📋 **Prerequisiti**

Prima di iniziare, assicurati di avere:
- ✅ Account Azure attivo
- ✅ Azure CLI installato e autenticato (`az login`)
- ✅ Repository GitHub con il codice del frontend
- ✅ Backend già deployato su Azure App Service
- ✅ App Registration su Microsoft Entra ID
- ✅ Subscription ID: `07c74816-ca51-49e9-a886-5dd6d2009523`
- ✅ Resource Group: `rg-medsafe-prod`

### ⚠️ **Nota Importante**
Azure Static Web Apps non è disponibile in `italynorth`. Usiamo **Azure App Service** che supporta EasyAuth e può servire SPA Angular in `italynorth`.

---

## 🔧 **STEP 1: Creare Azure App Service**

### Via PowerShell/CLI (Consigliato)

Esegui questi comandi in PowerShell:

```powershell
# 1. Login e imposta subscription
az login
az account set --subscription 07c74816-ca51-49e9-a886-5dd6d2009523

# 2. Crea App Service Plan (Linux, B1)
az appservice plan create `
  --name medsafe-frontend-plan `
  --resource-group rg-medsafe-prod `
  --location italynorth `
  --is-linux `
  --sku B1

# 3. Crea Web App per Node.js
az webapp create `
  --name medsafe-frontend `
  --resource-group rg-medsafe-prod `
  --plan medsafe-frontend-plan `
  --runtime "NODE:20-lts"

# 4. Abilita HTTPS only
az webapp update `
  --name medsafe-frontend `
  --resource-group rg-medsafe-prod `
  --https-only true

# 5. Ottieni l'URL
az webapp show --name medsafe-frontend --resource-group rg-medsafe-prod --query "defaultHostName" -o tsv
```

**URL**: `https://medsafe-frontend.azurewebsites.net`

---

## 🔐 **STEP 2: Configurare EasyAuth su App Service**

### Via Azure Portal (Più Semplice)

1. Azure Portal → **App Services** → **medsafe-frontend**
2. Menu laterale → **Authentication**
3. Clicca **Add identity provider**
4. Seleziona **Microsoft**

5. Configura:
   ```
   App registration type: Pick an existing app registration
   Application (client) ID: b05b2d51-457f-4ae1-81e5-add2bf7c3718
   Client secret: (creane uno nuovo nell'App Registration)
   Issuer URL: https://login.microsoftonline.com/c30767db-3dda-4dd4-8a4d-097d22cb99d3/v2.0
   ```

6. **Restrict access**: Require authentication
7. **Unauthenticated requests**: HTTP 302 Found redirect
8. Clicca **Add**

---

## � **STEP 3: Aggiornare Azure AD App Registration**

1. Azure Portal → **Microsoft Entra ID** → **App registrations**
2. Seleziona app: `b05b2d51-457f-4ae1-81e5-add2bf7c3718`
3. **Authentication** → **Add a platform** → **Web**
4. Aggiungi Redirect URI:
   ```
   https://medsafe-frontend.azurewebsites.net/.auth/login/aad/callback
   ```
5. Abilita **ID tokens**
6. **Certificates & secrets** → **New client secret** → salva il VALUE

---

## 🔄 **STEP 4: Setup GitHub Actions Deploy**

### 4.1 Ottieni Publish Profile

```powershell
az webapp deployment list-publishing-profiles `
  --name medsafe-frontend `
  --resource-group rg-medsafe-prod `
  --xml
```

Copia tutto l'output XML.

### 4.2 Aggiungi Secret su GitHub

1. GitHub → repo **MedSafe** → **Settings** → **Secrets and variables** → **Actions**
2. **New repository secret**
3. Name: `AZURE_WEBAPP_PUBLISH_PROFILE`
4. Value: incolla l'XML
5. **Add secret**

---

## � **STEP 5: Aggiornare environment.prod.ts**

Aggiorna con l'URL dell'App Service:

```typescript
redirectUri: 'https://medsafe-frontend.azurewebsites.net'
```

---

## 🚀 **STEP 6: Deploy**

```powershell
git add .
git commit -m "Configure Azure App Service deployment"
git push origin main
```

GitHub Actions builderà e deployerà automaticamente.
Monitora: **GitHub** → **Actions** tab

---

## ✅ **STEP 7: Test**

1. Vai su `https://medsafe-frontend.azurewebsites.net`
2. Dovresti essere reindirizzato al login Microsoft
3. Login con account del tenant `c30767db-3dda-4dd4-8a4d-097d22cb99d3`
4. Verifica:
   - ✅ Dashboard si carica
   - ✅ Chiamate API funzionano
   - ✅ Cookie `AppServiceAuthSession` presente (F12 → Application)

---

## 🛠️ **STEP 8: Configurare Backend CORS**

Aggiungi al backend:

```java
.allowedOrigins(
    "http://localhost:4200",
    "https://medsafe-frontend.azurewebsites.net"
)
.allowCredentials(true)
```

---

## � **Troubleshooting**

### Logs in tempo reale
```powershell
az webapp log tail --name medsafe-frontend --resource-group rg-medsafe-prod
```

### Restart App
```powershell
az webapp restart --name medsafe-frontend --resource-group rg-medsafe-prod
```

### Errori Comuni
- **401 loop**: Verifica Redirect URI e Client Secret
- **CORS**: Aggiungi URL frontend al backend
- **Build fallita**: Controlla GitHub Actions logs

---

## 🎯 **URL Finali**

- **Frontend**: `https://medsafe-frontend.azurewebsites.net`
- **Backend**: `https://medsafe-api-cucqc2bydbezfsfy.italynorth-01.azurewebsites.net`
- **Login**: `https://medsafe-frontend.azurewebsites.net/.auth/login/aad`
- **Logout**: `https://medsafe-frontend.azurewebsites.net/.auth/logout`

---

**Deploy completato su Azure App Service in Italy North! 🚀**
