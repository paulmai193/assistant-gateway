import { browser, element, by } from 'protractor';
import { NavBarPage } from './../page-objects/jhi-page-objects';

describe('Credential e2e test', () => {

    let navBarPage: NavBarPage;
    let credentialDialogPage: CredentialDialogPage;
    let credentialComponentsPage: CredentialComponentsPage;

    beforeAll(() => {
        browser.get('/');
        browser.waitForAngular();
        navBarPage = new NavBarPage();
        navBarPage.getSignInPage().autoSignInUsing('admin', 'admin');
        browser.waitForAngular();
    });

    it('should load Credentials', () => {
        navBarPage.goToEntity('credential');
        credentialComponentsPage = new CredentialComponentsPage();
        expect(credentialComponentsPage.getTitle())
            .toMatch(/assistantGatewayApp.credential.home.title/);

    });

    it('should load create Credential dialog', () => {
        credentialComponentsPage.clickOnCreateButton();
        credentialDialogPage = new CredentialDialogPage();
        expect(credentialDialogPage.getModalTitle())
            .toMatch(/assistantGatewayApp.credential.home.createOrEditLabel/);
        credentialDialogPage.close();
    });

   /* it('should create and save Credentials', () => {
        credentialComponentsPage.clickOnCreateButton();
        credentialDialogPage.setLoginInput('login');
        expect(credentialDialogPage.getLoginInput()).toMatch('login');
        credentialDialogPage.setPasswordHashInput('passwordHash');
        expect(credentialDialogPage.getPasswordHashInput()).toMatch('passwordHash');
        credentialDialogPage.setLastLoginDateInput(12310020012301);
        expect(credentialDialogPage.getLastLoginDateInput()).toMatch('2001-12-31T02:30');
        credentialDialogPage.userSelectLastOption();
        credentialDialogPage.save();
        expect(credentialDialogPage.getSaveButton().isPresent()).toBeFalsy();
    });*/

    afterAll(() => {
        navBarPage.autoSignOut();
    });
});

export class CredentialComponentsPage {
    createButton = element(by.css('.jh-create-entity'));
    title = element.all(by.css('jhi-credential div h2 span')).first();

    clickOnCreateButton() {
        return this.createButton.click();
    }

    getTitle() {
        return this.title.getAttribute('jhiTranslate');
    }
}

export class CredentialDialogPage {
    modalTitle = element(by.css('h4#myCredentialLabel'));
    saveButton = element(by.css('.modal-footer .btn.btn-primary'));
    closeButton = element(by.css('button.close'));
    loginInput = element(by.css('input#field_login'));
    passwordHashInput = element(by.css('input#field_passwordHash'));
    lastLoginDateInput = element(by.css('input#field_lastLoginDate'));
    userSelect = element(by.css('select#field_user'));

    getModalTitle() {
        return this.modalTitle.getAttribute('jhiTranslate');
    }

    setLoginInput = function(login) {
        this.loginInput.sendKeys(login);
    };

    getLoginInput = function() {
        return this.loginInput.getAttribute('value');
    };

    setPasswordHashInput = function(passwordHash) {
        this.passwordHashInput.sendKeys(passwordHash);
    };

    getPasswordHashInput = function() {
        return this.passwordHashInput.getAttribute('value');
    };

    setLastLoginDateInput = function(lastLoginDate) {
        this.lastLoginDateInput.sendKeys(lastLoginDate);
    };

    getLastLoginDateInput = function() {
        return this.lastLoginDateInput.getAttribute('value');
    };

    userSelectLastOption = function() {
        this.userSelect.all(by.tagName('option')).last().click();
    };

    userSelectOption = function(option) {
        this.userSelect.sendKeys(option);
    };

    getUserSelect = function() {
        return this.userSelect;
    };

    getUserSelectedOption = function() {
        return this.userSelect.element(by.css('option:checked')).getText();
    };

    save() {
        this.saveButton.click();
    }

    close() {
        this.closeButton.click();
    }

    getSaveButton() {
        return this.saveButton;
    }
}
