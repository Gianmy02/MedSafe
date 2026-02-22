import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, ReplaySubject } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface ClientPrincipal {
    identityProvider: string;
    userId: string;
    userDetails: string;
    userRoles: string[];
    access_token?: string;
    id_token?: string;
}

export interface EasyAuthClaim {
    typ: string;
    val: string;
}

export interface EasyAuthPayload {
    provider_name: string;
    user_id: string;
    user_claims?: EasyAuthClaim[];
    access_token?: string;
    id_token?: string;
    clientPrincipal?: ClientPrincipal;
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private authUrl = '/.auth';
    private currentToken: string | null = null;
    public authInitialized$ = new ReplaySubject<boolean>(1);

    constructor(private http: HttpClient) { }

    /**
     * Recupera le informazioni dell'utente loggato dall'endpoint di Easy Auth.
     */
    getUserInfo(): Observable<ClientPrincipal | null> {
        return this.http.get<EasyAuthPayload | EasyAuthPayload[]>(`${this.authUrl}/me`, { withCredentials: true }).pipe(
            map(response => {
                const payload = Array.isArray(response) && response.length > 0 ? response[0] : response as EasyAuthPayload;
                // FIX: Torniamo a dare priorità all'ID_TOKEN. 
                // L'access_token di EasyAuth per Graph è spesso cifrato/opaco e il backend lo vede come "Malformed".
                // L'ID Token è un JWT standard che identifica l'utente e che abbiamo abilitato nel backend (Audience Frontend).
                const possibleToken = payload.id_token || payload.access_token || payload.clientPrincipal?.id_token;

                if (possibleToken) {
                    this.currentToken = possibleToken;
                }

                return payload.user_claims ?
                    this.normalizeClaims(payload) :
                    (payload.clientPrincipal || (payload as unknown as ClientPrincipal));
            }),
            tap(() => this.authInitialized$.next(true)),
            catchError((error) => {
                console.error('❌ AuthService: /.auth/me fallito');
                if (error.status === 401) {
                    console.warn('⚠️ Utente non autenticato su Azure (401). Cookie mancante o scaduto.');
                }
                this.currentToken = null;
                this.authInitialized$.next(true); // Emit even on error so app doesn't hang
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
        const redirectUrl = window.location.origin;
        const scopes = environment.auth.scopes.join(' ');
        // Aggiunge lo scope alla richiesta di login per ottenere un Access Token valido per il backend
        const loginUrl = `${this.authUrl}/login/aad?post_login_redirect_url=${encodeURIComponent(redirectUrl)}&scope=${encodeURIComponent(scopes)}`;
        window.location.href = loginUrl;
    }

    /**
     * Effettua il logout.
     */
    logout() {
        window.location.href = `${this.authUrl}/logout?post_logout_redirect_uri=/`;
    }

    private normalizeClaims(payload: EasyAuthPayload): ClientPrincipal {
        const claims = payload.user_claims || [];
        const roles = claims
            .filter((c: EasyAuthClaim) => c.typ === 'roles')
            .map((c: EasyAuthClaim) => c.val);

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
