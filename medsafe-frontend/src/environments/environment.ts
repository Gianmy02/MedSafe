export const environment = {
  production: false,

  // Backend API Configuration
  apiUrl: 'http://localhost:8080',

  // Authentication Configuration (will be used in Azure)
  auth: {
    enabled: false,  // Set to true when moving to Azure with Entra ID
    clientId: '',    // Azure AD App Registration Client ID
    authority: '',   // https://login.microsoftonline.com/{tenant-id}
    redirectUri: 'http://localhost:4200',
    scopes: ['api://your-backend-app-id/access_as_user']
  },

  // Feature flags
  features: {
    enableFileUpload: true,
    enablePdfDownload: true,
    enableImageDownload: true
  },

  // App Settings
  appName: 'MedSafe - Local Development',
  appVersion: '1.0.0-local'
};