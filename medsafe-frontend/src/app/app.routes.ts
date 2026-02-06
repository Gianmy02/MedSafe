import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { RefertiListComponent } from './components/referti-list/referti-list.component';
import { RefertiUploadComponent } from './components/referti-upload/referti-upload.component';
import { RefertiEditComponent } from './components/referti-edit/referti-edit.component';
import { UserProfileComponent } from './components/user-profile/user-profile.component';
import { UsersListComponent } from './components/users-list/users-list.component';

export const routes: Routes = [
  { path: '', component: DashboardComponent },
  { path: 'profilo', component: UserProfileComponent },
  { path: 'utenti', component: UsersListComponent },
  { path: 'referti', component: RefertiListComponent },
  { path: 'upload', component: RefertiUploadComponent },
  { path: 'edit', component: RefertiEditComponent }
];