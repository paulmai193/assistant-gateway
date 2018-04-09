package logia.assistant.gateway.web.rest.errors;

import java.io.Serializable;

/**
 * The Class FieldErrorVM.
 *
 * @author Dai Mai
 */
public class FieldErrorVM implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The object name. */
    private final String objectName;

    /** The field. */
    private final String field;

    /** The message. */
    private final String message;

    /**
     * Instantiates a new field error VM.
     *
     * @param dto the dto
     * @param field the field
     * @param message the message
     */
    public FieldErrorVM(String dto, String field, String message) {
        this.objectName = dto;
        this.field = field;
        this.message = message;
    }

    /**
     * Gets the object name.
     *
     * @return the object name
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * Gets the field.
     *
     * @return the field
     */
    public String getField() {
        return field;
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

}
