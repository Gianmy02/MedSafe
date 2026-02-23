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
      padding: 0.25rem 0.6rem;
      border-radius: 9999px;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.025em;
    }
    .badge-tac { background-color: #fee2e2; color: #991b1b; }
    .badge-radiografia { background-color: #e0f2fe; color: #075985; }
    .badge-ecografia { background-color: #dcfce7; color: #166534; }
    .badge-risonanza { background-color: #f3e8ff; color: #6b21a8; }
    .badge-laboratorio { background-color: #fef3c7; color: #92400e; }
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
