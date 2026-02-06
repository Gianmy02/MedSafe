import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/user.service';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-users-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './users-list.component.html',
  styleUrl: './users-list.component.scss'
})
export class UsersListComponent implements OnInit {
  users: User[] = [];
  loading = true;
  error: string | null = null;

  constructor(private userService: UserService) {}

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Errore nel caricamento degli utenti';
        this.loading = false;
        console.error('Errore caricamento utenti:', err);
      }
    });
  }

  getRoleBadgeClass(role: string): string {
    return role === 'ADMIN' ? 'badge-admin' : 'badge-medico';
  }

  getRoleLabel(role: string): string {
    return role === 'ADMIN' ? 'Amministratore' : 'Medico';
  }

  getDoctorTitle(genere?: string): string {
    if (genere === 'FEMMINA') {
      return 'Dott.ssa';
    }
    return 'Dr.';
  }

  formatSpecializzazione(spec?: string): string {
    if (!spec) return 'NESSUNA';
    return spec.replace(/_/g, ' ');
  }

  toggleUserStatus(user: User, event: Event): void {
    event.preventDefault(); // Previene il cambio automatico del checkbox
    
    if (!user.id) return;

    const action = user.enabled ? 'disabilitare' : 'abilitare';
    if (!confirm(`Sei sicuro di voler ${action} l'utente ${user.fullName}?`)) {
      return; // L'utente ha annullato, non fare nulla
    }

    const observable = user.enabled 
      ? this.userService.disableUser(user.id)
      : this.userService.enableUser(user.id);

    observable.subscribe({
      next: () => {
        user.enabled = !user.enabled; // Aggiorna lo stato solo dopo il successo
        console.log(`Utente ${user.fullName} ${user.enabled ? 'abilitato' : 'disabilitato'}`);
      },
      error: (err) => {
        console.error('Errore nel cambio stato utente:', err);
        alert('Errore nel cambio stato dell\'utente');
      }
    });
  }
}
