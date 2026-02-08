import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface ClientPrincipal {
    identityProvider: string;
    userId: string;
    userDetails: string;
    userRoles: string[];
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private authUrl = '/.auth';

    constructor(private http: HttpClient) { }

    /**
     * Recupera le informazioni dell'utente loggato dall'endpoint di Easy Auth.
     */
    getUserInfo(): Observable<ClientPrincipal | null> {
        if (!environment.auth.enabled) {
            // Mock per sviluppo locale se auth è disabilitata
            return of({
                identityProvider: 'local',
                userId: 'local-user',
                userDetails: 'dev@local',
                userRoles: ['anonymous', 'authenticated']
            });
        }

        return this.http.get<{ clientPrincipal: ClientPrincipal }>(`${this.authUrl}/me`).pipe(
            map(response => response.clientPrincipal),
            catchError(() => of(null))
        );
    }

    /**
     * Reindirizza al login di Azure AD.
     */
    login() {
        window.location.href = `${this.authUrl}/login/aad?post_login_redirect_uri=/`;
    }

    /**
     * Effettua il logout.
     */
    logout() {
        window.location.href = `${this.authUrl}/logout?post_logout_redirect_uri=/`;
    }
}
