import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { User } from '../../models/user.model';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  user: User | null = null;
  loading = true;

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit() {
    // Aspetta che l'AuthService abbia finito il check iniziale (token pronto)
    this.authService.authInitialized$.subscribe(() => {
      this.userService.getCurrentUser().subscribe({
        next: (user) => {
          this.user = user;
          this.loading = false;

          // Se l'utente non è abilitato, rimuovi la carta "Nuovo Referto"
          if (this.user && !this.user.enabled) {
            this.cards = this.cards.filter(card => card.route !== '/upload');
          }

          // Check primo accesso
          if (this.user && !this.user.specializzazione) {
            alert("Benvenuto! Al primo accesso è necessario completare il profilo selezionando Genere e Specializzazione.");
            this.router.navigate(['/profilo']);
          }
        },
        error: (err) => {
          console.error('Errore nel caricamento utente dashboard');
          this.loading = false;
        }
      });
    });
  }

  getDoctorEmoji(): string {
    if (!this.user?.genere) return '🩺';
    switch (this.user.genere) {
      case 'MASCHIO': return '👨🏻‍⚕️';
      case 'FEMMINA': return '👩🏻‍⚕️';
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

  formatSpecialization(spec?: string): string {
    if (!spec) return '';
    return spec.replace(/_/g, ' ');
  }

  cards = [
    {
      title: 'Nuovo Referto',
      description: 'Carica un nuovo referto medico con i relativi file',
      icon: '📋',
      route: '/upload',
      color: 'primary'
    },
    {
      title: 'I miei Referti',
      description: 'Gestisci e modifica i tuoi referti',
      icon: '✏️',
      route: '/edit',
      color: 'info'
    },
    {
      title: 'Cerca Referti',
      description: 'Cerca e visualizza referti per codice fiscale o tipo esame',
      icon: '🔍',
      route: '/referti',
      color: 'info'
    }
  ];
}
