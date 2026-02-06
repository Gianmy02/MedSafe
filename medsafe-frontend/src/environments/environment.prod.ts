export const environment = {
  production: true,
  
  // Backend API Configuration (Azure App Service)
  apiUrl: 'https://medsafe-backend.azurewebsites.net',  // Replace with your Azure App Service URL
  
  // Authentication Configuration (Azure Entra ID)
  auth: {
    enabled: true,   // Enable authentication in production
    clientId: 'YOUR_AZURE_AD_CLIENT_ID',  // Replace with your App Registration Client ID
    authority: 'https://login.microsoftonline.com/YOUR_TENANT_ID',  // Replace with your Tenant ID
    redirectUri: 'https://medsafe-frontend.azurestaticapps.net',  // Replace with your Static Web App URL
    scopes: ['api://YOUR_BACKEND_APP_ID/access_as_user']  // Replace with your Backend App ID
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