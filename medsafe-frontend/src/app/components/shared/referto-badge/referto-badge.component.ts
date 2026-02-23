import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TipoEsame } from '../../../models/constants';

@Component({
  selector: 'app-referto-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span class="badge" [ngClass]="getBadgeClass()">
      {{ getLabel() }}
    </span>
  `,
  styles: [`
    .badge {
      display: inline-block;
      padding: var(--spacing-sm, 0.5rem) var(--spacing-md, 1rem); /* Fallback to 0.5rem 1rem if vars missing */
      border-radius: 8px; /* Matched from referti-upload.scss */
      font-size: 0.9rem; /* Matched from referti-upload.scss */
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.025em;
      transition: all 0.3s ease;
    }
    .badge:hover {
      transform: translateX(8px);
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
    }
    .badge-tac { background-color: #ede9fe; color: #5b21b6; }
    .badge-radiografia { background-color: #d1fae5; color: #065f46; }
    .badge-ecografia { background-color: #dbeafe; color: #1e40af; }
    .badge-risonanza { background-color: #e5e7eb; color: #374151; }
    .badge-laboratorio { background-color: #fee2e2; color: #991b1b; }
  `]
})
export class RefertoBadgeComponent {
  @Input({ required: true }) tipoEsame!: string;

  getBadgeClass(): string {
    const tipo = this.tipoEsame?.toUpperCase() || '';
    switch (tipo) {
      case TipoEsame.RADIOGRAFIA.toUpperCase(): return 'badge-radiografia';
      case TipoEsame.ECOGRAFIA.toUpperCase(): return 'badge-ecografia';
      case TipoEsame.TAC.toUpperCase(): return 'badge-tac';
      case TipoEsame.RISONANZA.toUpperCase(): return 'badge-risonanza';
      case TipoEsame.ESAMI_LABORATORIO.toUpperCase(): return 'badge-laboratorio';
      default: return '';
    }
  }

  getLabel(): string {
    const tipo = this.tipoEsame?.toUpperCase() || '';
    switch (tipo) {
      case TipoEsame.ESAMI_LABORATORIO.toUpperCase(): return 'Esami di laboratorio';
      case TipoEsame.RISONANZA.toUpperCase(): return 'Risonanza magnetica';
      default: return this.tipoEsame;
    }
  }
}
