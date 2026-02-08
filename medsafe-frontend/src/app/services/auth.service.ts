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
    access_token?: string;
    id_token?: string;
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private authUrl = '/.auth';
    private currentToken: string | null = null;

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

        return this.http.get<{ clientPrincipal: ClientPrincipal, access_token?: string, id_token?: string }>(`${this.authUrl}/me`).pipe(
            map(response => {
                // EasyAuth a volte restituisce il token nella root del JSON, a volte dentro clientPrincipal
                // Dipende dalla configurazione specifica, ma spesso è 'id_token' per OpenID Connect
                const possibleToken = response.id_token || response.access_token || (response.clientPrincipal as any)?.id_token;

                if (possibleToken) {
                    this.currentToken = possibleToken;
                }

                return response.clientPrincipal;
            }),
            catchError(() => {
                this.currentToken = null;
                return of(null);
            })
        );
    }

    getToken(): string | null {
        return this.currentToken;
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
