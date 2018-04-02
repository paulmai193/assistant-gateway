import { Routes } from '@angular/router';

import { UserRouteAccessService } from '../../shared';
import { CredentialComponent } from './credential.component';
import { CredentialDetailComponent } from './credential-detail.component';
import { CredentialPopupComponent } from './credential-dialog.component';
import { CredentialDeletePopupComponent } from './credential-delete-dialog.component';

export const credentialRoute: Routes = [
    {
        path: 'credential',
        component: CredentialComponent,
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'assistantGatewayApp.credential.home.title'
        },
        canActivate: [UserRouteAccessService]
    }, {
        path: 'credential/:id',
        component: CredentialDetailComponent,
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'assistantGatewayApp.credential.home.title'
        },
        canActivate: [UserRouteAccessService]
    }
];

export const credentialPopupRoute: Routes = [
    {
        path: 'credential-new',
        component: CredentialPopupComponent,
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'assistantGatewayApp.credential.home.title'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
        path: 'credential/:id/edit',
        component: CredentialPopupComponent,
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'assistantGatewayApp.credential.home.title'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
        path: 'credential/:id/delete',
        component: CredentialDeletePopupComponent,
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'assistantGatewayApp.credential.home.title'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    }
];
