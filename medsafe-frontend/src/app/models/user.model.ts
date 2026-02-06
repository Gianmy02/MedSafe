export interface User {
  id?: number;
  email: string;
  fullName: string;
  role: 'ADMIN' | 'MEDICO';
  genere?: 'MASCHIO' | 'FEMMINA' | 'NON_SPECIFICATO';
  specializzazione?: string;
  enabled?: boolean;
  createdAt: string;
}
