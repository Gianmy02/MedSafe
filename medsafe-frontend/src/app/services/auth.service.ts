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
        // 1. Controlla se il token è nell'URL (Implicit Flow / Hash Fragment)
        // Esempio URL dopo login: https://.../#id_token=eyJ...&access_token=...
        const fragment = window.location.hash.substring(1); // Rimuove il #
        const params = new URLSearchParams(fragment);
        const idToken = params.get('id_token');
        const accessToken = params.get('access_token');

        if (idToken || accessToken) {
            console.log('✅ AuthService: Token trovato nell\'URL (Implicit Flow)');
            this.currentToken = idToken || accessToken;

            // Pulisci l'hash dall'URL per non lasciarlo visibile
            window.history.replaceState({}, document.title, window.location.pathname);

            // Costruisci un principal temporaneo dal token (decodifica basic se necessario, o usa dati minimi)
            // Per ora usiamo il token come "prova" di auth e chiediamo i dettagli a /.auth/me se possibile,
            // altrimenti usiamo un principal fittizio basato sul token.
            // MA COMPLETIAMO PRIMA LA CHIAMATA A /.auth/me PER AVERE I RUOLI COMPLETI SE IL COOKIE C'È.
        }

        if (!environment.auth.enabled) {
            return of({
                identityProvider: 'local',
                userId: 'local-user',
                userDetails: 'dev@local',
                userRoles: ['anonymous', 'authenticated']
            });
        }

        return this.http.get<any[]>(`${this.authUrl}/me`, { withCredentials: true }).pipe(
            map(response => {
                const payload = Array.isArray(response) && response.length > 0 ? response[0] : response;
                const possibleToken = payload.id_token || payload.access_token || (payload.clientPrincipal as any)?.id_token;

                if (possibleToken) {
                    this.currentToken = possibleToken;
                }

                return payload.user_claims ?
                    this.normalizeClaims(payload) :
                    (payload.clientPrincipal || payload);
            }),
            catchError((error) => {
                console.error('❌ AuthService: /.auth/me fallito', error);

                // FALLBACK: Se abbiamo trovato il token nell'URL, usiamolo!
                if (this.currentToken) {
                    console.warn('⚠️ Usa il token da URL come fallback, ma mancano i ruoli completi da /.auth/me');
                    return of({
                        identityProvider: 'aad',
                        userId: 'user@implicit.flow',
                        userDetails: 'Utente (Implicit)',
                        userRoles: ['anonymous', 'authenticated'], // Ruoli minimi se /.auth/me fallisce
                        id_token: this.currentToken
                    });
                }

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
        // Rimosso post_login_redirect_uri che causava problemi con alcune configurazioni Azure
        window.location.href = `${this.authUrl}/login/aad`;
    }

    /**
     * Effettua il logout.
     */
    logout() {
        window.location.href = `${this.authUrl}/logout`;
    }

    private normalizeClaims(payload: any): ClientPrincipal {
        const claims = payload.user_claims || [];
        const roles = claims
            .filter((c: any) => c.typ === 'roles')
            .map((c: any) => c.val);

        return {
            identityProvider: payload.provider_name || 'aad',
            userId: payload.user_id,
            userDetails: payload.user_id,
            userRoles: roles.length > 0 ? roles : ['anonymous', 'authenticated'],
            access_token: payload.access_token,
            id_token: payload.id_token
        };
    }
}
