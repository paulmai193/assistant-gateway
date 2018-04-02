/* tslint:disable max-line-length */
import { ComponentFixture, TestBed, async } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { AssistantGatewayTestModule } from '../../../test.module';
import { CredentialDetailComponent } from '../../../../../../main/webapp/app/entities/credential/credential-detail.component';
import { CredentialService } from '../../../../../../main/webapp/app/entities/credential/credential.service';
import { Credential } from '../../../../../../main/webapp/app/entities/credential/credential.model';

describe('Component Tests', () => {

    describe('Credential Management Detail Component', () => {
        let comp: CredentialDetailComponent;
        let fixture: ComponentFixture<CredentialDetailComponent>;
        let service: CredentialService;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                imports: [AssistantGatewayTestModule],
                declarations: [CredentialDetailComponent],
                providers: [
                    CredentialService
                ]
            })
            .overrideTemplate(CredentialDetailComponent, '')
            .compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(CredentialDetailComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(CredentialService);
        });

        describe('OnInit', () => {
            it('Should call load all on init', () => {
                // GIVEN

                spyOn(service, 'find').and.returnValue(Observable.of(new HttpResponse({
                    body: new Credential(123)
                })));

                // WHEN
                comp.ngOnInit();

                // THEN
                expect(service.find).toHaveBeenCalledWith(123);
                expect(comp.credential).toEqual(jasmine.objectContaining({id: 123}));
            });
        });
    });

});
