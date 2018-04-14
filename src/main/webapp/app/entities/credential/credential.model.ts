import { BaseEntity } from './../../shared';

export class Credential implements BaseEntity {
    constructor(
        public id?: number,
        public login?: string,
        public lastLoginDate?: any,
        public activationKey?: string,
        public resetKey?: string,
        public resetDate?: any,
        public activated?: boolean,
        public primary?: boolean,
        public userLogin?: string,
        public userId?: number,
    ) {
        this.activated = false;
        this.primary = false;
    }
}
