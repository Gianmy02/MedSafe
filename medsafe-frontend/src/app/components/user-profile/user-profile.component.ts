import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { User } from '../../models/user.model';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { UserGenere } from '../../models/constants';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-profile.component.html',
  styleUrl: './user-profile.component.scss'
})
export class UserProfileComponent implements OnInit {
  user: User | null = null;
  loading = true;
  error: string | null = null;
  successMessage: string | null = null;
  isEditMode = false;

  // Dati per l'edit
  editData = {
    genere: '',
    specializzazione: ''
  };

  // Liste per dropdown
  generi: UserGenere[] = [];
  specializzazioni: string[] = [];
  filteredSpecializzazioni: string[] = [];
  showSpecializzazioniDropdown = false;

  constructor(
    private userService: UserService,
    private authService: AuthService
  ) { }

  ngOnInit() {
    this.authService.authInitialized$.subscribe(() => {
      this.loadUserProfile();
      this.loadGeneri();
      this.loadSpecializzazioni();
    });
  }

  loadUserProfile() {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.user = user;
        this.loading = false;

        // Se non abbiamo ancora caricato specializzazioni, ricarichiamole per sicurezza
        if (this.specializzazioni.length === 0) {
          this.loadSpecializzazioni();
        }
      },
      error: (err) => {
        this.error = 'Errore nel caricamento del profilo';
        this.loading = false;
        console.error('Errore caricamento profilo');
      }
    });
  }

  loadGeneri() {
    this.userService.getGeneri().subscribe({
      next: (generi) => {
        this.generi = generi;
      },
      error: (err) => {
        console.error('Errore caricamento generi');
      }
    });
  }

  loadSpecializzazioni() {
    this.userService.getSpecializzazioni().subscribe({
      next: (specializzazioni) => {
        this.specializzazioni = specializzazioni;
        this.filteredSpecializzazioni = specializzazioni;
      },
      error: (err) => {
        console.error('Errore caricamento specializzazioni');
      }
    });
  }

  filterSpecializzazioni() {
    const searchTerm = this.editData.specializzazione.toLowerCase();
    this.filteredSpecializzazioni = this.specializzazioni.filter(spec =>
      spec.toLowerCase().startsWith(searchTerm)
    );
    this.showSpecializzazioniDropdown = true;
  }

  selectSpecializzazione(spec: string) {
    this.editData.specializzazione = spec;
    this.showSpecializzazioniDropdown = false;
  }

  onSpecializzazioneFocus() {
    this.filteredSpecializzazioni = this.specializzazioni;
    this.showSpecializzazioniDropdown = true;
  }

  onSpecializzazioneBlur() {
    // Ritarda la chiusura per permettere il click su un'opzione
    setTimeout(() => {
      this.showSpecializzazioniDropdown = false;
    }, 200);
  }

  isFormValid(): boolean {
    return this.editData.genere.trim() !== '' &&
      this.editData.specializzazione.trim() !== '';
  }

  getValidationError(): string | null {
    if (this.editData.genere.trim() === '') {
      return 'Il genere è obbligatorio';
    }
    if (this.editData.specializzazione.trim() === '') {
      return 'La specializzazione è obbligatoria';
    }
    return null;
  }

  enableEditMode() {
    this.isEditMode = true;
    this.editData.genere = this.user?.genere || UserGenere.NON_SPECIFICATO;
    this.editData.specializzazione = this.user?.specializzazione || '';
    this.error = null;
    this.successMessage = null;
  }

  cancelEdit() {
    this.isEditMode = false;
    this.error = null;
    this.successMessage = null;
  }

  saveProfile() {
    // Validazione
    const validationError = this.getValidationError();
    if (validationError) {
      this.error = validationError;
      return;
    }

    if (!this.user) return;

    this.loading = true;
    this.error = null;
    this.successMessage = null;

    // Creo un oggetto User completo con le modifiche
    const updatedUser: User = {
      ...this.user,
      genere: this.editData.genere as UserGenere,
      specializzazione: this.editData.specializzazione
    };

    this.userService.updateProfile(updatedUser).subscribe({
      next: (updatedUser) => {
        this.user = updatedUser;
        this.isEditMode = false;
        this.loading = false;
        this.successMessage = 'Profilo aggiornato con successo!';
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (err) => {
        this.error = 'Errore nell\'aggiornamento del profilo';
        this.loading = false;
        console.error('Errore aggiornamento profilo');
      }
    });
  }

  getGenereLabel(genere?: UserGenere): string {
    if (!genere) return 'Non specificato';
    switch (genere) {
      case UserGenere.MASCHIO: return 'Maschio';
      case UserGenere.FEMMINA: return 'Femmina';
      case UserGenere.NON_SPECIFICATO: return 'Non specificato';
      default: return 'Non specificato';
    }
  }

  formatSpecializzazione(spec: string): string {
    if (!spec) return '';
    return spec.replace(/_/g, ' ');
  }

  getGenereEmoji(genere?: UserGenere): string {
    if (!genere) return '🩺';
    switch (genere) {
      case UserGenere.MASCHIO: return '👨🏻‍⚕️';
      case UserGenere.FEMMINA: return '👩🏻‍⚕️';
      default: return '🩺';
    }
  }
}
