package com.vkr.validatorgen.prototype.model;

public record RuleSpec(String id,
                       RuleCategory category,
                       String expression,
                       String targetField,
                       String message) {
}
