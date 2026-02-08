export const environment = {
  production: true,
  
  // Backend API Configuration (Azure App Service)
  apiUrl: 'https://medsafe-api-cucqc2bydbezfsfy.italynorth-01.azurewebsites.net',
  
  // Authentication Configuration (EasyAuth via App Service)
  auth: {
    enabled: true,   // Authentication managed by Azure App Service
    clientId: 'b05b2d51-457f-4ae1-81e5-add2bf7c3718',
    authority: 'https://login.microsoftonline.com/c30767db-3dda-4dd4-8a4d-097d22cb99d3',
    redirectUri: 'https://medsafe-frontend.azurewebsites.net',
    scopes: []  // EasyAuth handles scopes automatically
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