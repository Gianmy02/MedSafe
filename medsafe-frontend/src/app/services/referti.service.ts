import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TipoEsame } from '../models/constants';

export interface RefertoDTO {
  id?: number;
  nomePaziente: string;
  codiceFiscale: string;
  tipoEsame: TipoEsame;
  testoReferto?: string;
  conclusioni?: string;
  fileUrlImmagine?: string;
  urlPdfGenerato?: string;
  nomeFile: string;
  autoreEmail: string;
  dataCaricamento?: string;
}

@Injectable({
  providedIn: 'root'
})
export class RefertiService {
  private apiUrl = `${environment.apiUrl}/referti`;

  constructor(private http: HttpClient) { }

  getRefertoByCodiceFiscale(codiceFiscale: string): Observable<RefertoDTO[]> {
    const params = new HttpParams().set('value', codiceFiscale);
    return this.http.get<RefertoDTO[]>(`${this.apiUrl}/codiceFiscale`, { params });
  }

  getRefertoByNomeFile(nomeFile: string): Observable<RefertoDTO> {
    const params = new HttpParams().set('value', nomeFile);
    return this.http.get<RefertoDTO>(`${this.apiUrl}/nomeFile`, { params });
  }

  getRefertiByTipoEsame(tipoEsame: string): Observable<RefertoDTO[]> {
    const params = new HttpParams().set('value', tipoEsame);
    return this.http.get<RefertoDTO[]>(`${this.apiUrl}/tipoEsame`, { params });
  }

  getRefertiByAutoreEmail(email: string): Observable<RefertoDTO[]> {
    const params = new HttpParams().set('value', email);
    return this.http.get<RefertoDTO[]>(`${this.apiUrl}/email`, { params });
  }

  getAllReferti(): Observable<RefertoDTO[]> {
    return this.http.get<RefertoDTO[]>(this.apiUrl);
  }

  addReferto(formData: FormData): Observable<void> {
    return this.http.post<void>(this.apiUrl, formData);
  }

  editReferto(formData: FormData): Observable<void> {
    return this.http.put<void>(this.apiUrl, formData);
  }

  removeReferto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  downloadPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/download/pdf/${id}`, { responseType: 'blob' });
  }

  downloadImmagine(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/download/immagine/${id}`, { responseType: 'blob' });
  }
}