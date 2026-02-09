export const environment = {
  production: true,

  // Backend API Configuration (Azure App Service)
  apiUrl: 'https://medsafe-api-cucqc2bydbezfsfy.italynorth-01.azurewebsites.net',

  // Authentication Configuration (EasyAuth via App Service)
  auth: {
    enabled: true,   // Authentication managed by Azure App Service
    clientId: '5c911c10-3fe4-4569-b466-e79f78cd436f',
    authority: 'https://login.microsoftonline.com/common',
    redirectUri: 'https://medsafe-frontend-bcf5cvfpcah2geh8.italynorth-01.azurewebsites.net',
    scopes: ['api://07416fbe-03ed-47c9-a0e8-ea0235166f3b/user_impersonation']  // Scope corretto per il backend
  },

  // Feature flags
  features: {
    enableFileUpload: true,
    enablePdfDownload: true,
    enableImageDownload: true
  },

  // App Settings
  appName: 'MedSafe - Azure Production',
  appVersion: '1.0.0'
};