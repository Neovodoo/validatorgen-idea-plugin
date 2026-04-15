package com.vkr.validatorgen.prototype.model;

public record RuleTemplate(String id,
                           RuleCategory category,
                           String title,
                           String description,
                           String expressionExample,
                           String defaultMessage) {
    @Override
    public String toString() {
        return title;
    }
}
