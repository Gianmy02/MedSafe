import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { User } from '../../models/user.model';
import { UserService } from '../../services/user.service';

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

  constructor(private userService: UserService) { }

  ngOnInit() {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.user = user;
        this.loading = false;
      },
      error: (err) => {
        console.error('Errore nel caricamento utente dashboard:', err);
        this.loading = false;
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
      description: 'Modifica o elimina un referto esistente',
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
