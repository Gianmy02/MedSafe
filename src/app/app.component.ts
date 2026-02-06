import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { UserService } from './services/user.service';
import { User } from './models/user.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="app-container">
      <nav class="navbar">
        <a routerLink="/" class="navbar-brand">
          <div class="logo">
            <img src="assets/logo.png" alt="MedSafe Logo">
          </div>
          <h1>MedSafe</h1>
        </a>
        <div class="nav-links">
          <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}">Dashboard</a>
          <a routerLink="/upload" routerLinkActive="active">Nuovo Referto</a>
          <a routerLink="/edit" routerLinkActive="active">I miei Referti</a>
          <a routerLink="/referti" routerLinkActive="active">Cerca Referti</a>
          <a *ngIf="user?.role === 'ADMIN'" routerLink="/utenti" routerLinkActive="active">Elenco Utenti</a>
          <a *ngIf="user" routerLink="/profilo" class="welcome-user" routerLinkActive="active">{{ getDoctorEmoji() }} {{ getDoctorTitle() }} {{ user.fullName }}</a>
          <span *ngIf="!user && !isLoading" class="welcome-user">👤 Login</span>
          <button *ngIf="user" class="btn-logout" (click)="logout()">Logout</button>
        </div>
      </nav>
      
      <main class="content">
        <router-outlet></router-outlet>
      </main>

      <footer>
        <p>&copy; 2026 MedSafe - Sistema di Gestione Referti Medici</p>
      </footer>
    </div>
  `,
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  title = 'medsafe-frontend';
  user: User | null = null;
  isLoading = true;
  
  constructor(private userService: UserService) {
    console.log('AppComponent initialized!');
  }

  ngOnInit() {
    this.loadUser();
  }

  getDoctorEmoji(): string {
    if (!this.user?.genere) return '🩺';
    switch(this.user.genere) {
      case 'MASCHIO': return '👨‍⚕️';
      case 'FEMMINA': return '👩‍⚕️';
      case 'NON_SPECIFICATO': return '🩺';
      default: return '🩺';
    }
  }

  getDoctorTitle(): string {
    if (!this.user?.genere) return 'Dr.';
    switch(this.user.genere) {
      case 'MASCHIO': return 'Dr.';
      case 'FEMMINA': return 'Dott.ssa';
      case 'NON_SPECIFICATO': return 'Dr.';
      default: return 'Dr.';
    }
  }

  loadUser() {
    // Simulazione temporanea per testare con admin
    // TODO: Rimuovere quando il backend sarà pronto
    this.user = {
      email: 'admin@medsafe.local',
      fullName: 'Admin Test',
      role: 'ADMIN',
      genere: 'NON_SPECIFICATO',
      specializzazione: 'NESSUNA',
      createdAt: '2024-01-15T10:00:00'
    };
    this.isLoading = false;

    // Codice originale da riabilitare quando il backend è pronto:
    /*
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.user = user;
        this.isLoading = false;
      },
      error: (err) => {
        console.log('Utente non autenticato, simulazione con Login');
        this.isLoading = false;
      }
    });
    */
  }

  logout(): void {
    // TODO: Implementare logout con MSAL/Azure AD
    console.log('Logout richiesto');
    alert('Funzionalità di logout sarà implementata con Azure AD');
  }
}