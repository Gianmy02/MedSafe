import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RefertiService, RefertoDTO } from '../../services/referti.service';
import { UserService } from '../../services/user.service';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-referti-edit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './referti-edit.component.html',
  styleUrl: './referti-edit.component.scss'
})
export class RefertiEditComponent implements OnInit {
  private userEmail = '';
  currentUser: User | null = null;

  // Results
  referti: RefertoDTO[] = [];
  selectedReferto: RefertoDTO | null = null;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  // Edit mode
  isEditMode = false;
  editData = {
    codiceFiscalePaziente: '',
    tipoEsame: '',
    dataEsame: '',
    nomeFile: '',
    testoReferto: '',
    conclusioni: ''
  };
  selectedFile: File | null = null;

  constructor(
    private refertiService: RefertiService,
    private userService: UserService
  ) { }

  ngOnInit() {
    this.isLoading = true;
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        if (user) {
          this.currentUser = user;
          this.userEmail = user.email;
          this.loadMyReferti();
        } else {
          this.isLoading = false;
          this.errorMessage = 'Utente non autenticato';
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Errore nel recupero utente';
        console.error(err);
      }
    });
  }

  loadMyReferti() {
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.userEmail) {
      this.isLoading = false;
      return;
    }

    this.refertiService.getRefertiByAutoreEmail(this.userEmail).subscribe({
      next: (data) => {
        this.isLoading = false;
        this.referti = data;
        if (this.referti.length === 0) {
          this.errorMessage = 'Non hai ancora creato nessun referto';
        }
      },
      error: (error: any) => {
        this.isLoading = false;
        // Se errore 500, probabilmente non ci sono referti
        if (error.status === 500) {
          this.errorMessage = 'Non hai ancora creato nessun referto';
        } else {
          this.errorMessage = 'Errore durante il caricamento dei referti: ' + (error.message || 'Errore sconosciuto');
        }
      }
    });
  }

  selectReferto(referto: RefertoDTO) {
    this.selectedReferto = referto;
    this.isEditMode = false;
    this.successMessage = '';
    this.errorMessage = '';
  }

  enterEditMode() {
    if (!this.selectedReferto) return;

    this.isEditMode = true;
    this.selectedFile = null;
    this.editData = {
      codiceFiscalePaziente: this.selectedReferto.codiceFiscale,
      tipoEsame: this.selectedReferto.tipoEsame,
      dataEsame: this.selectedReferto.dataCaricamento || '',
      nomeFile: this.selectedReferto.nomeFile,
      testoReferto: this.selectedReferto.testoReferto || '',
      conclusioni: this.selectedReferto.conclusioni || ''
    };
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  cancelEdit() {
    this.isEditMode = false;
    this.errorMessage = '';
    this.successMessage = '';
  }

  saveEdit() {
    if (!this.selectedReferto) return;

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const updatedReferto: RefertoDTO = {
      ...this.selectedReferto,
      autoreEmail: this.userEmail,
      codiceFiscale: this.editData.codiceFiscalePaziente,
      tipoEsame: this.editData.tipoEsame as any,
      nomeFile: this.editData.nomeFile,
      nomePaziente: this.selectedReferto.nomePaziente,
      testoReferto: this.editData.testoReferto,
      conclusioni: this.editData.conclusioni
    };

    // TODO: Se c'è un nuovo file, inviare anche il file
    // Per ora aggiorniamo solo i dati testuali
    this.refertiService.editReferto(updatedReferto).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Referto modificato con successo';
        this.isEditMode = false;
        // Update the selected referto with new data
        this.selectedReferto = updatedReferto;
        // Update in the list
        const index = this.referti.findIndex(r => r.id === this.selectedReferto!.id);
        if (index !== -1) {
          this.referti[index] = this.selectedReferto;
        }
      },
      error: (error: any) => {
        this.isLoading = false;
        this.errorMessage = 'Errore durante la modifica: ' + (error.message || 'Errore sconosciuto');
      }
    });
  }

  deleteReferto() {
    if (!this.selectedReferto || !this.selectedReferto.id) return;

    if (!confirm(`Sei sicuro di voler eliminare il referto "${this.selectedReferto.nomeFile}"?`)) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.refertiService.removeReferto(this.selectedReferto.id).subscribe({
      next: () => {
        this.successMessage = 'Referto eliminato con successo';
        // Rimuovi il referto dalla lista locale immediatamente
        this.referti = this.referti.filter(r => r.id !== this.selectedReferto?.id);
        this.selectedReferto = null;
        this.isEditMode = false;
        this.isLoading = false;

        // Se la lista è vuota, mostra messaggio appropriato dopo un attimo
        if (this.referti.length === 0) {
          setTimeout(() => {
            this.successMessage = '';
            this.errorMessage = 'Non hai ancora creato nessun referto';
          }, 1500);
        }
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = 'Errore durante l\'eliminazione: ' + (error.message || 'Errore sconosciuto');
      }
    });
  }

  getBadgeClass(tipoEsame: string): string {
    const tipo = tipoEsame?.toUpperCase() || '';
    switch (tipo) {
      case 'RADIOGRAFIA':
      case 'Radiografia'.toUpperCase():
        return 'badge-radiografia';
      case 'ECOGRAFIA':
      case 'Ecografia'.toUpperCase():
        return 'badge-ecografia';
      case 'TAC':
        return 'badge-tac';
      case 'RISONANZA':
      case 'RISONANZA MAGNETICA':
      case 'Risonanza'.toUpperCase():
        return 'badge-risonanza';
      case 'ESAMI_LABORATORIO':
      case 'ESAMI DI LABORATORIO':
      case 'Esami_Laboratorio'.toUpperCase():
        return 'badge-laboratorio';
      default:
        return '';
    }
  }

  getImageFileName(url?: string): string {
    const fileUrl = url || this.selectedReferto?.fileUrlImmagine;
    if (!fileUrl) {
      return 'Nessun file caricato';
    }
    const parts = fileUrl.split('/');
    return parts[parts.length - 1] || 'File non disponibile';
  }

  downloadPdf(id: number): void {
    this.refertiService.downloadPdf(id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `referto_${id}.pdf`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
      },
      error: (error) => {
        this.errorMessage = 'Errore durante il download del PDF';
        console.error('Error:', error);
      }
    });
  }

  downloadImmagine(id: number): void {
    this.refertiService.downloadImmagine(id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `immagine_${id}`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
      },
      error: (error) => {
        this.errorMessage = 'Errore durante il download dell\'immagine';
        console.error('Error:', error);
      }
    });
  }
}
