import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  /**
   * Recupera le informazioni dell'utente corrente autenticato
   */
  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/me`);
  }

  /**
   * Recupera l'elenco di tutti gli utenti (solo per ADMIN)
   */
  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl);
  }

  /**
   * Disabilita un utente (solo per ADMIN)
   */
  disableUser(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/disable`, {});
  }

  /**
   * Abilita un utente (solo per ADMIN)
   */
  enableUser(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/enable`, {});
  }

  /**
   * Aggiorna il profilo dell'utente corrente (genere e specializzazione)
   */
  updateProfile(user: User): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/profile`, user);
  }

  /**
   * Recupera l'elenco di tutti i generi disponibili
   */
  getGeneri(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/generi`);
  }

  /**
   * Recupera l'elenco di tutte le specializzazioni disponibili
   */
  getSpecializzazioni(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/specializzazioni`);
  }
}
