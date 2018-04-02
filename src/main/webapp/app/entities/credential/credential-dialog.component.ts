import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { Credential } from './credential.model';
import { CredentialPopupService } from './credential-popup.service';
import { CredentialService } from './credential.service';
import { User, UserService } from '../../shared';

@Component({
    selector: 'jhi-credential-dialog',
    templateUrl: './credential-dialog.component.html'
})
export class CredentialDialogComponent implements OnInit {

    credential: Credential;
    isSaving: boolean;

    users: User[];

    constructor(
        public activeModal: NgbActiveModal,
        private jhiAlertService: JhiAlertService,
        private credentialService: CredentialService,
        private userService: UserService,
        private eventManager: JhiEventManager
    ) {
    }

    ngOnInit() {
        this.isSaving = false;
        this.userService.query()
            .subscribe((res: HttpResponse<User[]>) => { this.users = res.body; }, (res: HttpErrorResponse) => this.onError(res.message));
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.credential.id !== undefined) {
            this.subscribeToSaveResponse(
                this.credentialService.update(this.credential));
        } else {
            this.subscribeToSaveResponse(
                this.credentialService.create(this.credential));
        }
    }

    private subscribeToSaveResponse(result: Observable<HttpResponse<Credential>>) {
        result.subscribe((res: HttpResponse<Credential>) =>
            this.onSaveSuccess(res.body), (res: HttpErrorResponse) => this.onSaveError());
    }

    private onSaveSuccess(result: Credential) {
        this.eventManager.broadcast({ name: 'credentialListModification', content: 'OK'});
        this.isSaving = false;
        this.activeModal.dismiss(result);
    }

    private onSaveError() {
        this.isSaving = false;
    }

    private onError(error: any) {
        this.jhiAlertService.error(error.message, null, null);
    }

    trackUserById(index: number, item: User) {
        return item.id;
    }
}

@Component({
    selector: 'jhi-credential-popup',
    template: ''
})
export class CredentialPopupComponent implements OnInit, OnDestroy {

    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private credentialPopupService: CredentialPopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            if ( params['id'] ) {
                this.credentialPopupService
                    .open(CredentialDialogComponent as Component, params['id']);
            } else {
                this.credentialPopupService
                    .open(CredentialDialogComponent as Component);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
