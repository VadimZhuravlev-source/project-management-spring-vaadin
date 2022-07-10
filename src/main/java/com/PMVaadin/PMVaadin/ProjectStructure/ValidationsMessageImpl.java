package com.PMVaadin.PMVaadin.ProjectStructure;

public class ValidationsMessageImpl implements ValidationsMessage {

    private boolean validationPassed = true;
    private String message = "";

    public ValidationsMessageImpl() {

    }

    public ValidationsMessageImpl(boolean validationPassed, String message) {

        this.validationPassed = validationPassed;
        this.message = message;

    }

    @Override
    public boolean validationPassed() {
        return validationPassed;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
