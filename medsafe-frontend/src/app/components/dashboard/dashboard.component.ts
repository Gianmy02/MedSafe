import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  user: User | null = null;

  ngOnInit() {
    // Simulazione utente loggato come admin
    // TODO: Rimuovere quando il backend gestirà l'autenticazione
    this.user = {
      email: 'admin@medsafe.local',
      fullName: 'Admin Test',
      role: 'ADMIN',
      genere: 'NON_SPECIFICATO',
      specializzazione: 'NESSUNA',
      createdAt: '2024-01-15T10:00:00'
    };
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
