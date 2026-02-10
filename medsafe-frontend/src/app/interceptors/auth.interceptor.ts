import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Escludi assets e chiamate di auth
    if (!req.url.startsWith(environment.apiUrl)) {
      return next.handle(req);
    }

    const token = this.authService.getToken();
    const token = this.authService.getToken();
    let headers = req.headers
      .set('X-App-Version', environment.appVersion);

    // NON forzare application/json se stiamo inviando FormData (upload file)
    // Il browser deve settare automaticamente il boundary per multipart/form-data
    if (!(req.body instanceof FormData)) {
      headers = headers.set('Content-Type', 'application/json');
    }

    if (token) {
      console.log('🔐 AuthInterceptor: Adding Bearer token to request', req.url);
      // console.log('🔑 Token snippet:', token.substring(0, 20) + '...'); // Decommenta se vuoi vedere un pezzo del token
      headers = headers.set('Authorization', `Bearer ${token}`);
    } else {
      console.warn('⚠️ AuthInterceptor: No token found for request', req.url);
    }

    // Clona la richiesta per aggiungere header
    const authReq = req.clone({
      withCredentials: true,
      headers: headers
    });

    return next.handle(authReq);
  }
}
