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
        credentialDialogPage.setLastLoginDateInput(12310020012301);
        expect(credentialDialogPage.getLastLoginDateInput()).toMatch('2001-12-31T02:30');
        credentialDialogPage.setActivationKeyInput('activationKey');
        expect(credentialDialogPage.getActivationKeyInput()).toMatch('activationKey');
        credentialDialogPage.setResetKeyInput('resetKey');
        expect(credentialDialogPage.getResetKeyInput()).toMatch('resetKey');
        credentialDialogPage.setResetDateInput(12310020012301);
        expect(credentialDialogPage.getResetDateInput()).toMatch('2001-12-31T02:30');
        credentialDialogPage.getActivatedInput().isSelected().then((selected) => {
            if (selected) {
                credentialDialogPage.getActivatedInput().click();
                expect(credentialDialogPage.getActivatedInput().isSelected()).toBeFalsy();
            } else {
                credentialDialogPage.getActivatedInput().click();
                expect(credentialDialogPage.getActivatedInput().isSelected()).toBeTruthy();
            }
        });
        credentialDialogPage.getPrimaryInput().isSelected().then((selected) => {
            if (selected) {
                credentialDialogPage.getPrimaryInput().click();
                expect(credentialDialogPage.getPrimaryInput().isSelected()).toBeFalsy();
            } else {
                credentialDialogPage.getPrimaryInput().click();
                expect(credentialDialogPage.getPrimaryInput().isSelected()).toBeTruthy();
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
    lastLoginDateInput = element(by.css('input#field_lastLoginDate'));
    activationKeyInput = element(by.css('input#field_activationKey'));
    resetKeyInput = element(by.css('input#field_resetKey'));
    resetDateInput = element(by.css('input#field_resetDate'));
    activatedInput = element(by.css('input#field_activated'));
    primaryInput = element(by.css('input#field_primary'));
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

    setLastLoginDateInput = function(lastLoginDate) {
        this.lastLoginDateInput.sendKeys(lastLoginDate);
    };

    getLastLoginDateInput = function() {
        return this.lastLoginDateInput.getAttribute('value');
    };

    setActivationKeyInput = function(activationKey) {
        this.activationKeyInput.sendKeys(activationKey);
    };

    getActivationKeyInput = function() {
        return this.activationKeyInput.getAttribute('value');
    };

    setResetKeyInput = function(resetKey) {
        this.resetKeyInput.sendKeys(resetKey);
    };

    getResetKeyInput = function() {
        return this.resetKeyInput.getAttribute('value');
    };

    setResetDateInput = function(resetDate) {
        this.resetDateInput.sendKeys(resetDate);
    };

    getResetDateInput = function() {
        return this.resetDateInput.getAttribute('value');
    };

    getActivatedInput = function() {
        return this.activatedInput;
    };
    getPrimaryInput = function() {
        return this.primaryInput;
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
