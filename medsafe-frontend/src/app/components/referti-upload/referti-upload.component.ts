import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RefertiService } from '../../services/referti.service';

@Component({
  selector: 'app-referti-upload',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './referti-upload.component.html',
  styleUrl: './referti-upload.component.scss'
})
export class RefertiUploadComponent {
  // TODO: Recuperare count reale quando si implementa l'autenticazione
  refertiCount = 0;
  
  formData = {
    nomePaziente: '',
    codiceFiscale: '',
    tipoEsame: '',
    testoReferto: '',
    conclusioni: '',
    nomeFile: ''
  };

  selectedFile: File | null = null;
  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(private refertiService: RefertiService) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      const validExtensions = ['.png', '.jpg', '.jpeg', '.pdf'];
      const fileName = file.name.toLowerCase();
      
      if (!validExtensions.some(ext => fileName.endsWith(ext))) {
        this.errorMessage = 'Formato file non supportato. Estensioni consentite: PNG, JPG, JPEG, PDF';
        this.selectedFile = null;
        input.value = '';
        return;
      }

      this.selectedFile = file;
      this.errorMessage = '';
    }
  }

  onSubmit(): void {
    if (!this.selectedFile) {
      this.errorMessage = 'Selezionare un file';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Simulazione utente loggato come admin
    // TODO: Rimuovere quando il backend gestirà l'autenticazione
    const userEmail = 'admin@medsafe.local';

    const formDataToSend = new FormData();
    formDataToSend.append('nomePaziente', this.formData.nomePaziente);
    formDataToSend.append('codiceFiscale', this.formData.codiceFiscale.toUpperCase());
    formDataToSend.append('tipoEsame', this.formData.tipoEsame);
    formDataToSend.append('testoReferto', this.formData.testoReferto || '');
    formDataToSend.append('conclusioni', this.formData.conclusioni || '');
    formDataToSend.append('autoreEmail', userEmail); // Email presa automaticamente dall'utente loggato
    formDataToSend.append('nomeFile', this.formData.nomeFile);
    formDataToSend.append('file', this.selectedFile);

    this.refertiService.addReferto(formDataToSend).subscribe({
      next: () => {
        this.successMessage = 'Referto caricato con successo!';
        this.loading = false;
        setTimeout(() => {
          this.resetForm();
        }, 2000);
      },
      error: (error) => {
        this.errorMessage = 'Errore durante il caricamento del referto';
        console.error('Error:', error);
        this.loading = false;
      }
    });
  }

  resetForm(form?: any): void {
    if (form) {
      form.resetForm();
    }
    this.formData = {
      nomePaziente: '',
      codiceFiscale: '',
      tipoEsame: '',
      testoReferto: '',
      conclusioni: '',
      nomeFile: ''
    };
    this.selectedFile = null;
    this.errorMessage = '';
    this.successMessage = '';
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  }
}
