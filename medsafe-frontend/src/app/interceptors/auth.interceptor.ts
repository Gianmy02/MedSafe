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
      }
    });

    // TODO: Quando abiliti auth in Azure, aggiungi qui il token JWT
    // if (environment.auth.enabled) {
    //   const token = this.getAccessToken(); // Da implementare con MSAL
    //   if (token) {
    //     modifiedReq = modifiedReq.clone({
    //       setHeaders: {
    //         'Authorization': `Bearer ${token}`
    //       }
    //     });
    //   }
    // }

    return next.handle(modifiedReq);
  }

  // Placeholder per futuro MSAL integration
  // private getAccessToken(): string | null {
  //   // Ottieni token da MSAL service
  //   return null;
  // }
}