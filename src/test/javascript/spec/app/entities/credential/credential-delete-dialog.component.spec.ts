/* tslint:disable max-line-length */
import { ComponentFixture, TestBed, async, inject, fakeAsync, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs/Observable';
import { JhiEventManager } from 'ng-jhipster';

import { AssistantGatewayTestModule } from '../../../test.module';
import { CredentialDeleteDialogComponent } from '../../../../../../main/webapp/app/entities/credential/credential-delete-dialog.component';
import { CredentialService } from '../../../../../../main/webapp/app/entities/credential/credential.service';

describe('Component Tests', () => {

    describe('Credential Management Delete Component', () => {
        let comp: CredentialDeleteDialogComponent;
        let fixture: ComponentFixture<CredentialDeleteDialogComponent>;
        let service: CredentialService;
        let mockEventManager: any;
        let mockActiveModal: any;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                imports: [AssistantGatewayTestModule],
                declarations: [CredentialDeleteDialogComponent],
                providers: [
                    CredentialService
                ]
            })
            .overrideTemplate(CredentialDeleteDialogComponent, '')
            .compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(CredentialDeleteDialogComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(CredentialService);
            mockEventManager = fixture.debugElement.injector.get(JhiEventManager);
            mockActiveModal = fixture.debugElement.injector.get(NgbActiveModal);
        });

        describe('confirmDelete', () => {
            it('Should call delete service on confirmDelete',
                inject([],
                    fakeAsync(() => {
                        // GIVEN
                        spyOn(service, 'delete').and.returnValue(Observable.of({}));

                        // WHEN
                        comp.confirmDelete(123);
                        tick();

                        // THEN
                        expect(service.delete).toHaveBeenCalledWith(123);
                        expect(mockActiveModal.dismissSpy).toHaveBeenCalled();
                        expect(mockEventManager.broadcastSpy).toHaveBeenCalled();
                    })
                )
            );
        });
    });

});
