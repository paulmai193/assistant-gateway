import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs/Subscription';
import { JhiEventManager } from 'ng-jhipster';

import { Credential } from './credential.model';
import { CredentialService } from './credential.service';

@Component({
    selector: 'jhi-credential-detail',
    templateUrl: './credential-detail.component.html'
})
export class CredentialDetailComponent implements OnInit, OnDestroy {

    credential: Credential;
    private subscription: Subscription;
    private eventSubscriber: Subscription;

    constructor(
        private eventManager: JhiEventManager,
        private credentialService: CredentialService,
        private route: ActivatedRoute
    ) {
    }

    ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.load(params['id']);
        });
        this.registerChangeInCredentials();
    }

    load(id) {
        this.credentialService.find(id)
            .subscribe((credentialResponse: HttpResponse<Credential>) => {
                this.credential = credentialResponse.body;
            });
    }
    previousState() {
        window.history.back();
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
        this.eventManager.destroy(this.eventSubscriber);
    }

    registerChangeInCredentials() {
        this.eventSubscriber = this.eventManager.subscribe(
            'credentialListModification',
            (response) => this.load(this.credential.id)
        );
    }
}
