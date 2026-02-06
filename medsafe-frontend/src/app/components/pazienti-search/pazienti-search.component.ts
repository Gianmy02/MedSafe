import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RefertiService, RefertoDTO } from '../../services/referti.service';

interface Paziente {
  codiceFiscale: string;
  nome?: string;
  cognome?: string;
  numeroReferti: number;
  ultimoReferto?: string;
}

@Component({
  selector: 'app-pazienti-search',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pazienti-search.component.html',
  styleUrl: './pazienti-search.component.scss'
})
export class PazientiSearchComponent {
  searchTerm = '';
  pazienti: Paziente[] = [];
  isLoading = false;
  errorMessage = '';

  constructor(private refertiService: RefertiService) {}

  searchPazienti() {
    if (!this.searchTerm.trim()) {
      this.errorMessage = 'Inserisci un codice fiscale';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.pazienti = [];

    // Search by codice fiscale
    this.refertiService.getRefertoByCodiceFiscale(this.searchTerm).subscribe({
      next: (referti: RefertoDTO[] | any) => {
        this.isLoading = false;
        
        if (Array.isArray(referti) && referti.length > 0) {
          // Group referti by paziente
          const pazienteMap = new Map<string, any[]>();
          
          referti.forEach(referto => {
            const cf = referto.codiceFiscale;
            if (!pazienteMap.has(cf)) {
              pazienteMap.set(cf, []);
            }
            pazienteMap.get(cf)!.push(referto);
          });

          // Create paziente objects
          this.pazienti = Array.from(pazienteMap.entries()).map(([cf, refertiList]) => {
            const sortedReferti = refertiList.sort((a, b) => {
              const dateA = a.dataCaricamento ? new Date(a.dataCaricamento).getTime() : 0;
              const dateB = b.dataCaricamento ? new Date(b.dataCaricamento).getTime() : 0;
              return dateB - dateA;
            });

            return {
              codiceFiscale: cf,
              numeroReferti: refertiList.length,
              ultimoReferto: sortedReferti[0]?.dataCaricamento
            };
          });
        } else if (referti) {
          // Single referto found (treat as object)
          const singleReferto = Array.isArray(referti) ? referti[0] : referti;
          if (singleReferto) {
            this.pazienti = [{
              codiceFiscale: singleReferto.codiceFiscale,
              numeroReferti: 1,
              ultimoReferto: singleReferto.dataCaricamento
            }];
          } else {
            this.errorMessage = 'Nessun paziente trovato con questo codice fiscale';
          }
        } else {
          this.errorMessage = 'Nessun paziente trovato con questo codice fiscale';
        }
      },
      error: (error: any) => {
        this.isLoading = false;
        this.errorMessage = 'Errore durante la ricerca: ' + (error.message || 'Errore sconosciuto');
      }
    });
  }

  viewReferti(codiceFiscale: string) {
    // This would navigate to referti list with pre-filled search
    // For now, just show alert
    alert(`Visualizza referti per paziente: ${codiceFiscale}\n\nNaviga alla pagina "Cerca Referti" e inserisci questo codice fiscale.`);
  }

  resetSearch() {
    this.searchTerm = '';
    this.pazienti = [];
    this.errorMessage = '';
  }
}
