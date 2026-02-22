import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { UserService } from './services/user.service';
import { AuthService } from './services/auth.service';
import { User } from './models/user.model';
import { UserRole, UserGenere } from './models/constants';

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
          <svg class="navbar-heartbeat" viewBox="0 0 120 40" width="80" height="28">
            <polyline points="0,20 20,20 28,6 36,34 44,20 55,20 65,6 73,34 81,20 120,20" fill="none" stroke="#10b981" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </a>
        <!-- Mobile controls: logout + hamburger -->
        <div class="mobile-controls">
          <button *ngIf="user" class="btn-logout mobile-logout" (click)="logout()">Logout</button>
          <button class="hamburger" (click)="toggleMenu()" [class.open]="menuOpen" aria-label="Menu">
            <span></span><span></span><span></span>
          </button>
        </div>
        <div class="nav-links" [class.nav-open]="menuOpen">
          <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}" (click)="closeMenu()">Dashboard</a>
          <a *ngIf="user?.enabled" routerLink="/upload" routerLinkActive="active" (click)="closeMenu()">Nuovo Referto</a>
          <a routerLink="/edit" routerLinkActive="active" (click)="closeMenu()">I miei Referti</a>
          <a routerLink="/referti" routerLinkActive="active" (click)="closeMenu()">Cerca Referti</a>
          <a *ngIf="user?.role === UserRole.ADMIN" routerLink="/utenti" routerLinkActive="active" (click)="closeMenu()">Elenco Utenti</a>
          <a *ngIf="user" routerLink="/profilo" class="welcome-user" routerLinkActive="active" (click)="closeMenu()">
            <span class="user-main">{{ getDoctorEmoji() }} {{ getDoctorTitle() }} {{ user.fullName }}</span>
            <span *ngIf="user.specializzazione" class="user-spec">Specializzato in: {{ formatSpecialization(user.specializzazione) }}</span>
          </a>
          <span *ngIf="!user && !isLoading" class="welcome-user" (click)="login(); closeMenu()" style="cursor: pointer;">👤 Login</span>
          <button *ngIf="user" class="btn-logout desktop-logout" (click)="logout()">Logout</button>
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
  menuOpen = false;

  constructor(private userService: UserService, private authService: AuthService) {

  }

  ngOnInit() {
    // 1. Prima recupera il token da Azure EasyAuth
    this.authService.getUserInfo().subscribe({
      next: (principal) => {
        if (principal) {
          // 2. Se siamo loggati su Azure, carica l'utente dal backend
          this.loadUser();
        } else {
          this.isLoading = false;
        }
      },
      error: (err) => {
        console.warn('⚠️ Impossibile recuperare info Azure');
        this.isLoading = false;
      }
    });
  }

  getDoctorEmoji(): string {
    if (!this.user?.genere) return '🩺';
    switch (this.user.genere) {
      case UserGenere.MASCHIO: return '👨🏻‍⚕️';
      case UserGenere.FEMMINA: return '👩🏻‍⚕️';
      case UserGenere.NON_SPECIFICATO: return '🩺';
      default: return '🩺';
    }
  }

  getDoctorTitle(): string {
    if (!this.user?.genere) return 'Dr.';
    switch (this.user.genere) {
      case UserGenere.MASCHIO: return 'Dr.';
      case UserGenere.FEMMINA: return 'Dott.ssa';
      case UserGenere.NON_SPECIFICATO: return 'Dr.';
      default: return 'Dr.';
    }
  }

  formatSpecialization(spec?: string): string {
    if (!spec) return '';
    return spec.replace(/_/g, ' ');
  }

  loadUser() {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.user = user;
        this.isLoading = false;

      },
      error: (err) => {
        console.warn('Utente non autenticato o errore nel caricamento');
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

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  closeMenu(): void {
    this.menuOpen = false;
  }
}