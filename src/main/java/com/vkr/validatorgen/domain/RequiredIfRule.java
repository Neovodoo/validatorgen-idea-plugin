package com.vkr.validatorgen.domain;

import java.util.List;
import java.util.Objects;

public final class RequiredIfRule implements RuleSpec {
    private final String id;
    private final ConditionSpec condition;
    private final String targetField;
    private final String violationTarget;
    private final String message;

    public RequiredIfRule(String id, ConditionSpec condition, String targetField, String violationTarget, String message) {
        this.id = Objects.requireNonNull(id);
        this.condition = Objects.requireNonNull(condition);
        this.targetField = Objects.requireNonNull(targetField);
        this.violationTarget = Objects.requireNonNullElse(violationTarget, targetField);
        this.message = Objects.requireNonNull(message);
    }

    public String getRuleId() { return id; }
    public ConditionSpec getCondition() { return condition; }
    public String getTargetField() { return targetField; }

    @Override
    public String getId() { return id; }

    @Override
    public RuleKind getKind() { return RuleKind.REQUIRED_IF; }

    @Override
    public String getViolationTarget() { return violationTarget; }

    @Override
    public String getMessage() { return message; }

    @Override
    public List<String> getInvolvedFields() {
        if (condition.fieldName().equals(targetField)) {
            return List.of(targetField);
        }
        return List.of(condition.fieldName(), targetField);
    }
}