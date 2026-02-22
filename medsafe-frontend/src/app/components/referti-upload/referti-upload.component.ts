import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { RefertiService } from '../../services/referti.service';
import { TipoEsame } from '../../models/constants';

import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-referti-upload',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './referti-upload.component.html',
  styleUrl: './referti-upload.component.scss'
})
export class RefertiUploadComponent implements OnInit {
  refertiCount = 0;

  formData = {
    nomePaziente: '',
    codiceFiscale: '',
    tipoEsame: '' as TipoEsame | '',
    testoReferto: '',
    conclusioni: '',
    nomeFile: ''
  };

  selectedFile: File | null = null;
  filePreviewUrl: string | null = null;
  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private refertiService: RefertiService,
    private userService: UserService,
    private authService: AuthService
  ) { }

  ngOnInit() {
    this.authService.authInitialized$.subscribe(() => {
      this.updateRefertiCount();
    });
  }

  updateRefertiCount() {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        if (user) {
          this.refertiService.getRefertiByAutoreEmail(user.email).subscribe({
            next: (referti) => {
              this.refertiCount = referti.length;
            }
          });
        }
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      const validExtensions = ['.png', '.jpg', '.jpeg', '.pdf'];
      const fileName = file.name.toLowerCase();

      if (!validExtensions.some(ext => fileName.endsWith(ext))) {
        this.errorMessage = 'Formato file non supportato. Estensioni consentite: PNG, JPG, JPEG, PDF';
        this.selectedFile = null;
        this.filePreviewUrl = null;
        input.value = '';
        return;
      }

      this.selectedFile = file;
      this.errorMessage = '';

      // Generate preview
      if (this.filePreviewUrl) {
        URL.revokeObjectURL(this.filePreviewUrl);
      }
      if (file.type.startsWith('image/')) {
        this.filePreviewUrl = URL.createObjectURL(file);
      } else {
        this.filePreviewUrl = null; // PDF - no image preview
      }
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

    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        if (!user) {
          this.errorMessage = 'Utente non autenticato';
          this.loading = false;
          return;
        }

        const userEmail = user.email;

        const formDataToSend = new FormData();
        formDataToSend.append('nomePaziente', this.formData.nomePaziente);
        formDataToSend.append('codiceFiscale', this.formData.codiceFiscale.toUpperCase());
        formDataToSend.append('tipoEsame', this.formData.tipoEsame);
        formDataToSend.append('testoReferto', this.formData.testoReferto || '');
        formDataToSend.append('conclusioni', this.formData.conclusioni || '');
        formDataToSend.append('autoreEmail', userEmail);
        formDataToSend.append('nomeFile', this.formData.nomeFile);
        formDataToSend.append('file', this.selectedFile!);

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
            console.error('Errore durante il caricamento del referto');
            this.loading = false;
          }
        });
      },
      error: (err) => {
        this.errorMessage = 'Errore nel recupero dati utente';
        this.loading = false;
        console.error('Errore nel recupero dati utente');
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
    if (this.filePreviewUrl) {
      URL.revokeObjectURL(this.filePreviewUrl);
      this.filePreviewUrl = null;
    }
    this.errorMessage = '';
    this.successMessage = '';
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
}
