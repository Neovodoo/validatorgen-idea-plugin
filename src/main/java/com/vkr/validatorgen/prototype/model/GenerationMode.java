package com.vkr.validatorgen.prototype.model;

public enum GenerationMode {
    JAKARTA_BEAN_VALIDATION("Jakarta Validation artifacts"),
    EXPLICIT_VALIDATOR("Explicit validator method");

    private final String label;

    GenerationMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
