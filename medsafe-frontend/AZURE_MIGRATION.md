# Passaggio da Locale ad Azure - Guida Step-by-Step

## 📋 Configurazione Attuale (Locale)

Il frontend è configurato per funzionare in locale con:
- Backend Spring Boot su `http://localhost:8080`
- Nessuna autenticazione (auth.enabled = false)
- Hot reload per sviluppo rapido

## 🚀 Step per Deploy su Azure

### 1. Prerequisiti Azure
- [ ] Account Azure attivo
- [ ] Azure CLI installato (`az login`)
- [ ] Subscription attiva

### 2. Crea Risorse Azure

#### A. Azure Static Web Apps (Frontend)
```bash
# Crea resource group
az group create --name medsafe-rg --location westeurope

# Crea Static Web App
az staticwebapp create \
  --name medsafe-frontend \
  --resource-group medsafe-rg \
  --location westeurope \
  --source https://github.com/YOUR_USERNAME/medsafe-frontend \
  --branch main \
  --app-location "/" \
  --output-location "dist/medsafe-frontend"
```

#### B. Azure App Service (Backend Spring Boot)
```bash
# Crea App Service Plan Linux
az appservice plan create \
  --name medsafe-plan \
  --resource-group medsafe-rg \
  --is-linux \
  --sku B1

# Crea Web App per Java
az webapp create \
  --name medsafe-backend \
  --resource-group medsafe-rg \
  --plan medsafe-plan \
  --runtime "JAVA:17-java17"
```

#### C. Azure Entra ID (Authentication)
1. Vai su portal.azure.com > Azure Active Directory > App registrations
2. Clicca "New registration":
   - Name: `MedSafe Frontend`
   - Supported account types: Single tenant
   - Redirect URI: `https://medsafe-frontend.azurestaticapps.net`
3. Salva il **Client ID** e **Tenant ID**
4. Vai su "Certificates & secrets" > Crea Client Secret (salva il valore)
5. Vai su "API permissions" > Aggiungi permesso per Backend API

### 3. Aggiorna Frontend per Azure

#### A. Modifica `environment.prod.ts`
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://medsafe-backend.azurewebsites.net',
  auth: {
    enabled: true,  // ⚠️ ABILITA AUTH
    clientId: 'IL_TUO_CLIENT_ID_DA_AZURE_AD',
    authority: 'https://login.microsoftonline.com/IL_TUO_TENANT_ID',
    redirectUri: 'https://medsafe-frontend.azurestaticapps.net',
    scopes: ['api://medsafe-backend/access_as_user']
  },
  // ... resto invariato
};
```

#### B. Installa MSAL Angular
```bash
npm install @azure/msal-browser @azure/msal-angular
```

#### C. Implementa Authentication Service
(File già preparato in `src/app/services/auth.service.ts` - da completare)

#### D. Abilita Auth Interceptor
Decommenta il codice in `src/app/interceptors/auth.interceptor.ts` per aggiungere token JWT.

### 4. Configura Backend Spring Boot per Azure

#### A. Abilita CORS
```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("https://medsafe-frontend.azurestaticapps.net")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

#### B. Valida JWT Token
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .oauth2ResourceServer(oauth2 -> oauth2.jwt())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

### 5. Deploy

#### A. Build Frontend
```bash
ng build --configuration production
```

#### B. Deploy Frontend su Azure Static Web Apps
```bash
# Automatico con GitHub Actions (creato da Azure CLI)
# Oppure manualmente:
az staticwebapp upload \
  --name medsafe-frontend \
  --resource-group medsafe-rg \
  --app-location dist/medsafe-frontend
```

#### C. Deploy Backend su App Service
```bash
# Build Spring Boot JAR
mvn clean package

# Deploy
az webapp deploy \
  --resource-group medsafe-rg \
  --name medsafe-backend \
  --src-path target/medsafe-backend.jar \
  --type jar
```

### 6. Test in Produzione

1. Apri `https://medsafe-frontend.azurestaticapps.net`
2. Verifica redirect a Microsoft login
3. Autentica con account Azure AD
4. Testa upload/download referti
5. Verifica che il token JWT venga inviato nelle richieste API

## 📝 Checklist Finale

- [ ] Frontend deployato su Azure Static Web Apps
- [ ] Backend deployato su Azure App Service
- [ ] Azure Entra ID configurato
- [ ] CORS abilitato nel backend
- [ ] JWT validation attiva nel backend
- [ ] MSAL integrato nel frontend
- [ ] Test end-to-end su produzione
- [ ] Monitoraggio e logging attivi

## 🔧 Troubleshooting

### Errore CORS
- Verifica che `allowedOrigins` nel backend includa l'URL del frontend Azure
- Controlla che `allowCredentials(true)` sia impostato

### Token JWT non valido
- Verifica che `clientId` e `authority` in `environment.prod.ts` siano corretti
- Controlla che gli scope API siano configurati in Azure AD

### 404 su API
- Verifica che `apiUrl` in `environment.prod.ts` corrisponda all'URL App Service
- Controlla che il backend sia running su Azure

## 📚 Documentazione Utile

- [Azure Static Web Apps](https://docs.microsoft.com/azure/static-web-apps/)
- [MSAL Angular](https://github.com/AzureAD/microsoft-authentication-library-for-js/tree/dev/lib/msal-angular)
- [Spring Boot + Azure AD](https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-boot-starter-java-app-with-azure-active-directory)