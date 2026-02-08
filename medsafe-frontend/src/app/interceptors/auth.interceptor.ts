import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Escludi assets e chiamate di auth
    if (!req.url.startsWith(environment.apiUrl)) {
      return next.handle(req);
    }

    // Clona la richiesta per aggiungere header
    const authReq = req.clone({
      withCredentials: true, // Fondamentale per inviare i cookie di sessione di Easy Auth (se sullo stesso dominio)
      setHeaders: {
        'Content-Type': 'application/json',
        'X-App-Version': environment.appVersion
        // Nota: Con Easy Auth proxy/linked backend, il token viene iniettato da Azure.
        // Se backend e frontend sono separati e non linkati, servirebbe estrarre il token manualmente.
      }
    });

    return next.handle(authReq);
  }
}
