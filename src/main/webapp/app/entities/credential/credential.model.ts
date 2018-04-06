import { BaseEntity } from './../../shared';

export class Credential implements BaseEntity {
    constructor(
        public id?: number,
        public login?: string,
        public passwordHash?: string,
        public lastLoginDate?: any,
        public userLogin?: string,
        public userId?: number,
    ) {
    }
}
