/* tslint:disable max-line-length */
import { ComponentFixture, TestBed, async } from '@angular/core/testing';
import { Observable } from 'rxjs/Observable';
import { HttpHeaders, HttpResponse } from '@angular/common/http';

import { AssistantGatewayTestModule } from '../../../test.module';
import { CredentialComponent } from '../../../../../../main/webapp/app/entities/credential/credential.component';
import { CredentialService } from '../../../../../../main/webapp/app/entities/credential/credential.service';
import { Credential } from '../../../../../../main/webapp/app/entities/credential/credential.model';

describe('Component Tests', () => {

    describe('Credential Management Component', () => {
        let comp: CredentialComponent;
        let fixture: ComponentFixture<CredentialComponent>;
        let service: CredentialService;

        beforeEach(async(() => {
            TestBed.configureTestingModule({
                imports: [AssistantGatewayTestModule],
                declarations: [CredentialComponent],
                providers: [
                    CredentialService
                ]
            })
            .overrideTemplate(CredentialComponent, '')
            .compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(CredentialComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(CredentialService);
        });

        describe('OnInit', () => {
            it('Should call load all on init', () => {
                // GIVEN
                const headers = new HttpHeaders().append('link', 'link;link');
                spyOn(service, 'query').and.returnValue(Observable.of(new HttpResponse({
                    body: [new Credential(123)],
                    headers
                })));

                // WHEN
                comp.ngOnInit();

                // THEN
                expect(service.query).toHaveBeenCalled();
                expect(comp.credentials[0]).toEqual(jasmine.objectContaining({id: 123}));
            });
        });
    });

});
