# 🚀 Deploy MedSafe Frontend su Azure Static Web Apps

## Guida Completa per il Deployment

### 📋 **Prerequisiti**

Prima di iniziare, assicurati di avere:
- ✅ Account Azure attivo
- ✅ Azure CLI installato e autenticato (`az login`)
- ✅ Repository GitHub con il codice del frontend
- ✅ Backend già deployato su Azure App Service
- ✅ App Registration su Microsoft Entra ID

---

## 🔧 **STEP 1: Creare Azure Static Web App**

### Opzione A: Via Azure Portal (Consigliata per prima volta)

1. **Vai al [portale Azure](https://portal.azure.com)**

2. **Cerca "Static Web Apps"** e clicca su "Create"

3. **Configura il progetto:**
   ```
   Subscription: [La tua subscription]
   Resource Group: rg-medsafe-prod
   Name: medsafe
   Region: Italy North
   ```

4. **Deployment details:**
   ```
   Source: GitHub
   Organization: [Il tuo username GitHub]
   Repository: [Nome del tuo repo]
   Branch: main
   ```

5. **Build Details:**
   ```
   Build Presets: Angular
   App location: /medsafe-frontend
   Api location: (lascia vuoto)
   Output location: dist/medsafe-frontend
   ```

6. Clicca **"Review + create"** poi **"Create"**

7. **Salva il Deployment Token**: Azure creerà automaticamente un GitHub Secret chiamato `AZURE_STATIC_WEB_APPS_API_TOKEN` nel tuo repository

---

### Opzione B: Via Azure CLI

```bash
# Login ad Azure
az login

# Imposta la subscription corretta
az account set --subscription "YOUR_SUBSCRIPTION_ID"

# Crea lo Static Web App con integrazione GitHub
az staticwebapp create \
  --name medsafe \
  --resource-group rg-medsafe-prod \
  --location italynorth \
  --source https://github.com/YOUR_USERNAME/YOUR_REPO \
  --branch main \
  --app-location "/medsafe-frontend" \
  --output-location "dist/medsafe-frontend" \
  --login-with-github
```

---

## 🔐 **STEP 2: Configurare Microsoft Entra ID (Azure AD)**

### 2.1 Aggiornare App Registration

1. Vai su **Azure Portal** → **Microsoft Entra ID** → **App registrations**
2. Seleziona la tua app: `b05b2d51-457f-4ae1-81e5-add2bf7c3718`
3. Vai su **Authentication** → **Platform configurations** → **Add a platform** → **Web**
4. Aggiungi questi **Redirect URIs**:
   ```
   https://medsafe.azurestaticapps.net/.auth/login/aad/callback
   https://medsafe-RANDOM.italynorth-01.azurestaticapps.net/.auth/login/aad/callback
   ```
   ⚠️ Sostituisci `RANDOM` con il codice generato da Azure nel tuo URL

5. In **Implicit grant and hybrid flows**, abilita:
   - ✅ ID tokens (used for implicit and hybrid flows)

6. Clicca **Save**

### 2.2 Creare Client Secret

1. Nella stessa App Registration, vai su **Certificates & secrets**
2. Clicca **New client secret**
3. Description: `Static Web App Secret`
4. Expires: 24 months (o come preferisci)
5. **Salva il VALUE del secret** (lo vedrai solo ora!)

---

## ⚙️ **STEP 3: Configurare Application Settings su Azure**

1. Vai su **Azure Portal** → **Static Web Apps** → **medsafe**
2. Nel menu laterale, clicca su **Configuration**
3. Aggiungi queste **Application settings**:

   | Name | Value |
   |------|-------|
   | `AZURE_AD_CLIENT_ID` | `b05b2d51-457f-4ae1-81e5-add2bf7c3718` |
   | `AZURE_AD_CLIENT_SECRET` | `[Il secret che hai salvato prima]` |

4. Clicca **Save**

---

## 🔄 **STEP 4: Aggiornare environment.prod.ts**

Dopo che Azure ha creato lo Static Web App, **aggiorna il file**:

```typescript
// src/environments/environment.prod.ts
redirectUri: 'https://medsafe.azurestaticapps.net',  // Aggiorna con l'URL effettivo
```

Se l'URL generato è diverso (es. contiene un codice random), usalo al posto di quello sopra.

---

## 🚀 **STEP 5: Fare il Deploy**

### Metodo 1: Push su GitHub (Automatico) ✅ CONSIGLIATO

```bash
# Commit e push delle modifiche
git add .
git commit -m "Configure Azure Static Web Apps deployment"
git push origin main
```

GitHub Actions si attiverà automaticamente e deployerà l'app. 
Monitora il progresso in: **GitHub** → **Actions** tab

### Metodo 2: Deploy Manuale con Azure CLI

```bash
# Build di produzione
npm run build -- --configuration production

# Deploy manuale
az staticwebapp deploy \
  --name medsafe \
  --resource-group rg-medsafe-prod \
  --app-location "./dist/medsafe-frontend" \
  --no-use-keyring
```

---

## ✅ **STEP 6: Verificare il Deployment**

1. **Vai all'URL dello Static Web App**: `https://medsafe.azurestaticapps.net`

2. **Test del flusso di autenticazione:**
   - Dovresti essere reindirizzato automaticamente a Microsoft Login
   - Fai login con un account Azure AD del tenant corretto
   - Dopo il login, dovresti vedere la dashboard dell'app

3. **Verifica chiamate API:**
   - Apri Developer Tools (F12) → Network tab
   - Controlla che le chiamate a `https://medsafe-api-cucqc2bydbezfsfy.italynorth-01.azurewebsites.net` funzionino
   - Verifica che il cookie `AppServiceAuthSession` sia presente

---

## 🔧 **Configurazione Backend (Se non già fatto)**

Il tuo backend Spring Boot deve:

1. **Accettare CORS dallo Static Web App:**

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins(
                        "http://localhost:4200",
                        "https://medsafe.azurestaticapps.net",
                        "https://medsafe-*.italynorth-01.azurestaticapps.net"
                    )
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

2. **Validare il token JWT di Azure AD:**

Il token JWT arriva nell'header `X-MS-TOKEN-AAD-ACCESS-TOKEN` da EasyAuth.
Configura il tuo Spring Security per validarlo:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(jwtAuthenticationConverter());
        
        return http.build();
    }
}
```

---

## 📊 **Monitoraggio e Troubleshooting**

### Visualizzare i logs:

```bash
# Logs dello Static Web App
az staticwebapp logs show \
  --name medsafe \
  --resource-group rg-medsafe-prod
```

### Controllare lo status:

```bash
az staticwebapp show \
  --name medsafe \
  --resource-group rg-medsafe-prod \
  --query "{name:name,defaultHostname:defaultHostname,repositoryUrl:repositoryUrl}"
```

### Errori comuni:

1. **401 Unauthorized loop:**
   - Verifica che i Redirect URIs in Azure AD siano corretti
   - Controlla che `AZURE_AD_CLIENT_ID` e `AZURE_AD_CLIENT_SECRET` siano impostati correttamente

2. **CORS errors:**
   - Aggiungi l'URL dello Static Web App al CORS config del backend
   - Assicurati che `allowCredentials(true)` sia abilitato

3. **Build fallita su GitHub Actions:**
   - Verifica che `package.json` abbia tutti i dependencies
   - Controlla i logs in GitHub → Actions tab

---

## 🎯 **URL Finali del Progetto**

Dopo il deployment, avrai:

- **Frontend**: `https://medsafe.azurestaticapps.net`
- **Backend**: `https://medsafe-api-cucqc2bydbezfsfy.italynorth-01.azurewebsites.net`
- **Login**: `https://medsafe.azurestaticapps.net/.auth/login/aad`
- **Logout**: `https://medsafe.azurestaticapps.net/.auth/logout`

---

## 🔄 **Deploy Continuo**

Ogni push su `main` trigghera automaticamente:
1. ✅ Build dell'applicazione Angular
2. ✅ Deploy su Azure Static Web Apps
3. ✅ Attivazione del dominio production

Per fare rollback a una versione precedente, usa il portale Azure o il CLI:

```bash
az staticwebapp deployment list \
  --name medsafe \
  --resource-group rg-medsafe-prod
```

---

## 📞 **Supporto**

- [Documentazione Azure Static Web Apps](https://docs.microsoft.com/en-us/azure/static-web-apps/)
- [Troubleshooting EasyAuth](https://docs.microsoft.com/en-us/azure/static-web-apps/authentication-authorization)
- [Angular on Azure](https://docs.microsoft.com/en-us/azure/static-web-apps/deploy-angular)

---

**Il progetto è pronto per il deploy! 🚀**
