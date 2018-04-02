import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { Credential } from './credential.model';
import { CredentialPopupService } from './credential-popup.service';
import { CredentialService } from './credential.service';

@Component({
    selector: 'jhi-credential-delete-dialog',
    templateUrl: './credential-delete-dialog.component.html'
})
export class CredentialDeleteDialogComponent {

    credential: Credential;

    constructor(
        private credentialService: CredentialService,
        public activeModal: NgbActiveModal,
        private eventManager: JhiEventManager
    ) {
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(id: number) {
        this.credentialService.delete(id).subscribe((response) => {
            this.eventManager.broadcast({
                name: 'credentialListModification',
                content: 'Deleted an credential'
            });
            this.activeModal.dismiss(true);
        });
    }
}

@Component({
    selector: 'jhi-credential-delete-popup',
    template: ''
})
export class CredentialDeletePopupComponent implements OnInit, OnDestroy {

    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private credentialPopupService: CredentialPopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.credentialPopupService
                .open(CredentialDeleteDialogComponent as Component, params['id']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
