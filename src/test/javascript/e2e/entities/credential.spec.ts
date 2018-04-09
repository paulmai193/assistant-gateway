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
        credentialDialogPage.setActivation_keyInput('activation_key');
        expect(credentialDialogPage.getActivation_keyInput()).toMatch('activation_key');
        credentialDialogPage.setReset_keyInput('reset_key');
        expect(credentialDialogPage.getReset_keyInput()).toMatch('reset_key');
        credentialDialogPage.setReset_dateInput(12310020012301);
        expect(credentialDialogPage.getReset_dateInput()).toMatch('2001-12-31T02:30');
        credentialDialogPage.getActivatedInput().isSelected().then((selected) => {
            if (selected) {
                credentialDialogPage.getActivatedInput().click();
                expect(credentialDialogPage.getActivatedInput().isSelected()).toBeFalsy();
            } else {
                credentialDialogPage.getActivatedInput().click();
                expect(credentialDialogPage.getActivatedInput().isSelected()).toBeTruthy();
            }
        });
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
    activation_keyInput = element(by.css('input#field_activation_key'));
    reset_keyInput = element(by.css('input#field_reset_key'));
    reset_dateInput = element(by.css('input#field_reset_date'));
    activatedInput = element(by.css('input#field_activated'));
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

    setActivation_keyInput = function(activation_key) {
        this.activation_keyInput.sendKeys(activation_key);
    };

    getActivation_keyInput = function() {
        return this.activation_keyInput.getAttribute('value');
    };

    setReset_keyInput = function(reset_key) {
        this.reset_keyInput.sendKeys(reset_key);
    };

    getReset_keyInput = function() {
        return this.reset_keyInput.getAttribute('value');
    };

    setReset_dateInput = function(reset_date) {
        this.reset_dateInput.sendKeys(reset_date);
    };

    getReset_dateInput = function() {
        return this.reset_dateInput.getAttribute('value');
    };

    getActivatedInput = function() {
        return this.activatedInput;
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
