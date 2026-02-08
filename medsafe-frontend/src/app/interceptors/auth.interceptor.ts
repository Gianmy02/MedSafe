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
    let headers = req.headers
      .set('Content-Type', 'application/json')
      .set('X-App-Version', environment.appVersion);

    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }

    // Clona la richiesta per aggiungere header
    const authReq = req.clone({
      withCredentials: true,
      headers: headers
    });

    return next.handle(authReq);
  }
}
