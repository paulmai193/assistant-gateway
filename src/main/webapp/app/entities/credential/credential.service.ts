import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { SERVER_API_URL } from '../../app.constants';

import { JhiDateUtils } from 'ng-jhipster';

import { Credential } from './credential.model';
import { createRequestOption } from '../../shared';

export type EntityResponseType = HttpResponse<Credential>;

@Injectable()
export class CredentialService {

    private resourceUrl =  SERVER_API_URL + 'api/credentials';
    private resourceSearchUrl = SERVER_API_URL + 'api/_search/credentials';

    constructor(private http: HttpClient, private dateUtils: JhiDateUtils) { }

    create(credential: Credential): Observable<EntityResponseType> {
        const copy = this.convert(credential);
        return this.http.post<Credential>(this.resourceUrl, copy, { observe: 'response' })
            .map((res: EntityResponseType) => this.convertResponse(res));
    }

    update(credential: Credential): Observable<EntityResponseType> {
        const copy = this.convert(credential);
        return this.http.put<Credential>(this.resourceUrl, copy, { observe: 'response' })
            .map((res: EntityResponseType) => this.convertResponse(res));
    }

    find(id: number): Observable<EntityResponseType> {
        return this.http.get<Credential>(`${this.resourceUrl}/${id}`, { observe: 'response'})
            .map((res: EntityResponseType) => this.convertResponse(res));
    }

    query(req?: any): Observable<HttpResponse<Credential[]>> {
        const options = createRequestOption(req);
        return this.http.get<Credential[]>(this.resourceUrl, { params: options, observe: 'response' })
            .map((res: HttpResponse<Credential[]>) => this.convertArrayResponse(res));
    }

    delete(id: number): Observable<HttpResponse<any>> {
        return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response'});
    }

    search(req?: any): Observable<HttpResponse<Credential[]>> {
        const options = createRequestOption(req);
        return this.http.get<Credential[]>(this.resourceSearchUrl, { params: options, observe: 'response' })
            .map((res: HttpResponse<Credential[]>) => this.convertArrayResponse(res));
    }

    private convertResponse(res: EntityResponseType): EntityResponseType {
        const body: Credential = this.convertItemFromServer(res.body);
        return res.clone({body});
    }

    private convertArrayResponse(res: HttpResponse<Credential[]>): HttpResponse<Credential[]> {
        const jsonResponse: Credential[] = res.body;
        const body: Credential[] = [];
        for (let i = 0; i < jsonResponse.length; i++) {
            body.push(this.convertItemFromServer(jsonResponse[i]));
        }
        return res.clone({body});
    }

    /**
     * Convert a returned JSON object to Credential.
     */
    private convertItemFromServer(credential: Credential): Credential {
        const copy: Credential = Object.assign({}, credential);
        copy.lastLoginDate = this.dateUtils
            .convertDateTimeFromServer(credential.lastLoginDate);
        copy.resetDate = this.dateUtils
            .convertDateTimeFromServer(credential.resetDate);
        return copy;
    }

    /**
     * Convert a Credential to a JSON which can be sent to the server.
     */
    private convert(credential: Credential): Credential {
        const copy: Credential = Object.assign({}, credential);

        copy.lastLoginDate = this.dateUtils.toDate(credential.lastLoginDate);

        copy.resetDate = this.dateUtils.toDate(credential.resetDate);
        return copy;
    }
}
