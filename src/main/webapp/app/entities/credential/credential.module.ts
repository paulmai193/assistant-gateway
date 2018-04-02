import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

import { AssistantGatewaySharedModule } from '../../shared';
import { AssistantGatewayAdminModule } from '../../admin/admin.module';
import {
    CredentialService,
    CredentialPopupService,
    CredentialComponent,
    CredentialDetailComponent,
    CredentialDialogComponent,
    CredentialPopupComponent,
    CredentialDeletePopupComponent,
    CredentialDeleteDialogComponent,
    credentialRoute,
    credentialPopupRoute,
} from './';

const ENTITY_STATES = [
    ...credentialRoute,
    ...credentialPopupRoute,
];

@NgModule({
    imports: [
        AssistantGatewaySharedModule,
        AssistantGatewayAdminModule,
        RouterModule.forChild(ENTITY_STATES)
    ],
    declarations: [
        CredentialComponent,
        CredentialDetailComponent,
        CredentialDialogComponent,
        CredentialDeleteDialogComponent,
        CredentialPopupComponent,
        CredentialDeletePopupComponent,
    ],
    entryComponents: [
        CredentialComponent,
        CredentialDialogComponent,
        CredentialPopupComponent,
        CredentialDeleteDialogComponent,
        CredentialDeletePopupComponent,
    ],
    providers: [
        CredentialService,
        CredentialPopupService,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class AssistantGatewayCredentialModule {}
