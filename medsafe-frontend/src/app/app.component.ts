import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { UserService } from './services/user.service';
import { AuthService } from './services/auth.service';
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
          <span *ngIf="!user && !isLoading" class="welcome-user" (click)="login()" style="cursor: pointer;">👤 Login</span>
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

  constructor(private userService: UserService, private authService: AuthService) {
    console.log('AppComponent initialized!');
  }

  ngOnInit() {
    // 1. Prima recupera il token da Azure EasyAuth
    this.authService.getUserInfo().subscribe({
      next: (principal) => {
        console.log('🔹 Azure Auth info:', principal);
        if (principal) {
          // 2. Se siamo loggati su Azure, carica l'utente dal backend
          this.loadUser();
        } else {
          this.isLoading = false;
        }
      },
      error: (err) => {
        console.warn('⚠️ Impossibile recuperare info Azure:', err);
        this.isLoading = false;
      }
    });
  }

  getDoctorEmoji(): string {
    if (!this.user?.genere) return '🩺';
    switch (this.user.genere) {
      case 'MASCHIO': return '👨‍⚕️';
      case 'FEMMINA': return '👩‍⚕️';
      case 'NON_SPECIFICATO': return '🩺';
      default: return '🩺';
    }
  }

  getDoctorTitle(): string {
    if (!this.user?.genere) return 'Dr.';
    switch (this.user.genere) {
      case 'MASCHIO': return 'Dr.';
      case 'FEMMINA': return 'Dott.ssa';
      case 'NON_SPECIFICATO': return 'Dr.';
      default: return 'Dr.';
    }
  }

  loadUser() {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.user = user;
        this.isLoading = false;
        console.log('Utente caricato:', user);
      },
      error: (err) => {
        console.log('Utente non autenticato o errore nel caricamento:', err);
        this.user = null;
        this.isLoading = false;
      }
    });
  }

  login(): void {
    this.authService.login();
  }

  logout(): void {
    this.authService.logout();
  }
}