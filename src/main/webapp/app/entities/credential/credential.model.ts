import { BaseEntity } from './../../shared';

export class Credential implements BaseEntity {
    constructor(
        public id?: number,
        public login?: string,
        public passwordHash?: string,
        public lastLoginDate?: any,
        public activation_key?: string,
        public reset_key?: string,
        public reset_date?: any,
        public activated?: boolean,
        public userLogin?: string,
        public userId?: number,
    ) {
        this.activated = false;
    }
}
