import { UserRole, UserGenere } from './constants';

export interface User {
  id?: number;
  email: string;
  fullName: string;
  role: UserRole;
  genere?: UserGenere;
  specializzazione?: string;
  enabled?: boolean;
  createdAt: string;
}
