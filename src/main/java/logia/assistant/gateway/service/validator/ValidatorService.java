package logia.assistant.gateway.service.validator;

import javax.inject.Inject;
import javax.validation.Validator;

import org.hibernate.validator.constraints.Email;
import org.springframework.stereotype.Component;

@Component
public final class ValidatorService {
    
    @Inject
    private Validator validator;
    
    public void validateEmail(String email) {
        this.validator.validate(new EmailValidatorWrapper(email));
    }
    
    public static class EmailValidatorWrapper {
        
        @Email
        private String email;

        public EmailValidatorWrapper(String email) {
            super();
            this.email = email;
        }

        /**
         * @return the email
         */
        public String getEmail() {
            return email;
        }

        /**
         * @param email the email to set
         */
        public void setEmail(String email) {
            this.email = email;
        }
        
    }
}
