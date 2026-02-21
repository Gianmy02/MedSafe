import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RefertiService, RefertoDTO } from '../../services/referti.service';

import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-referti-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './referti-list.component.html',
  styleUrl: './referti-list.component.scss'
})
export class RefertiListComponent implements OnInit {
  private userEmail = '';

  referti: RefertoDTO[] = [];
  searchCodiceFiscale = '';
  searchTipoEsame = '';
  loading = false;
  errorMessage = '';
  searched = false;

  constructor(
    private refertiService: RefertiService,
    private userService: UserService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.authService.authInitialized$.subscribe(() => {
      this.userService.getCurrentUser().subscribe({
        next: (user) => {
          if (user) {
            this.userEmail = user.email;
          }
        }
      });
    });
  }

  searchReferti(): void {
    // Se nessun campo è compilato, mostra tutti i referti
    if (!this.searchCodiceFiscale.trim() && !this.searchTipoEsame) {
      this.loading = true;
      this.errorMessage = '';
      this.searched = true;

      this.refertiService.getAllReferti().subscribe({
        next: (data) => {
          this.referti = this.sortReferti(data);
          this.loading = false;
        },
        error: (error) => {
          this.errorMessage = 'Errore durante il caricamento dei referti';
          console.error('Errore nella richiesta');
          this.loading = false;
        }
      });
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.searched = true;
    this.referti = [];

    // Array per raccogliere i risultati da tutte le ricerche
    const searchResults: RefertoDTO[][] = [];
    let completedSearches = 0;
    let totalSearches = 0;

    // Conta quante ricerche fare
    if (this.searchCodiceFiscale.trim()) totalSearches++;
    if (this.searchTipoEsame) totalSearches++;

    const checkComplete = () => {
      completedSearches++;
      if (completedSearches === totalSearches) {
        // Combina i risultati e filtra duplicati
        if (searchResults.length === 0) {
          this.referti = [];
          this.loading = false;
          return;
        }

        // Intersezione dei risultati (solo referti presenti in tutti i risultati)
        if (searchResults.length === 1) {
          this.referti = searchResults[0];
        } else {
          // Trova referti comuni a tutti i set di risultati
          const allIds = searchResults.map(results =>
            new Set(results.map(r => r.id))
          );

          const commonIds = Array.from(allIds[0]).filter(id =>
            allIds.every(set => set.has(id))
          );

          this.referti = searchResults[0].filter(r => commonIds.includes(r.id));
        }

        // Ordina i referti mettendo quelli dell'utente loggato in cima
        this.referti = this.sortReferti(this.referti);

        this.loading = false;
      }
    };

    // Esegui ricerche in parallelo
    if (this.searchCodiceFiscale.trim()) {
      this.refertiService.getRefertoByCodiceFiscale(this.searchCodiceFiscale.toUpperCase()).subscribe({
        next: (data) => {
          searchResults.push(Array.isArray(data) ? data : [data]);
          checkComplete();
        },
        error: (error) => {
          this.errorMessage = 'Errore durante la ricerca per codice fiscale';
          console.error('Errore nella richiesta');
          this.loading = false;
        }
      });
    }

    if (this.searchTipoEsame) {
      this.refertiService.getRefertiByTipoEsame(this.searchTipoEsame).subscribe({
        next: (data) => {
          searchResults.push(data);
          checkComplete();
        },
        error: (error) => {
          this.errorMessage = 'Errore durante la ricerca per tipo esame';
          console.error('Errore nella richiesta');
          this.loading = false;
        }
      });
    }
  }

  resetSearch(): void {
    this.searchCodiceFiscale = '';
    this.searchTipoEsame = '';
    this.referti = [];
    this.errorMessage = '';
    this.searched = false;
  }

  isMyReferto(referto: RefertoDTO): boolean {
    // Se non abbiamo ancora l'email dell'utente, non possiamo dire che è suo
    if (!this.userEmail) return false;
    return referto.autoreEmail === this.userEmail;
  }

  sortReferti(referti: RefertoDTO[]): RefertoDTO[] {
    return referti.sort((a, b) => {
      const aIsMine = this.isMyReferto(a);
      const bIsMine = this.isMyReferto(b);

      if (aIsMine && !bIsMine) return -1;
      if (!aIsMine && bIsMine) return 1;
      return 0;
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
        // Tipo esame non riconosciuto, nessun badge specifico
        return '';
    }
  }

  formatTipoEsame(tipoEsame: string): string {
    const tipo = tipoEsame?.toUpperCase() || '';
    switch (tipo) {
      case 'ESAMI_LABORATORIO':
        return 'Esami di laboratorio';
      case 'RISONANZA':
        return 'Risonanza magnetica';
      default:
        return tipoEsame;
    }
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
        console.error('Errore durante il download del PDF');
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
        console.error('Errore durante il download dell\'immagine');
      }
    });
  }
}
