import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

/**
 * HTTP Interceptor per aggiungere header alle richieste.
 * In locale: aggiunge header standard.
 * In Azure: aggiunge token JWT di autenticazione da MSAL.
 */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Skip interception per richieste non-API (es. assets)
    if (!req.url.startsWith(environment.apiUrl)) {
      return next.handle(req);
    }

    let modifiedReq = req.clone({
      setHeaders: {
        'Content-Type': 'application/json',
        'X-App-Version': environment.appVersion
      },
      withCredentials: true  // Required for EasyAuth to send authentication cookies
    });

    // Azure Static Web Apps EasyAuth automatically adds the user token
    // via cookies and the X-MS-TOKEN-AAD-ACCESS-TOKEN header.
    // The backend can validate this token to authorize requests.

    return next.handle(modifiedReq);
  }

  // Placeholder per futuro MSAL integration
  // private getAccessToken(): string | null {
  //   // Ottieni token da MSAL service
  //   return null;
  // }
}