package com.vkr.validatorgen.presentation;

import com.vkr.validatorgen.domain.CompareOp;
import com.vkr.validatorgen.domain.ConditionOperator;
import com.vkr.validatorgen.domain.RuleKind;

public record RuleDraft(
        RuleKind kind,
        String left,
        CompareOp op,
        String right,
        ConditionOperator conditionOperator,
        String conditionLiteral,
        String target,
        String message
) {
    public RuleDraft(String left, CompareOp op, String right, String target, String message) {
        this(RuleKind.COMPARE_FIELDS, left, op, right, null, null, target, message);
    }

    public static RuleDraft requiredIf(String conditionField, ConditionOperator operator, String literal, String targetField, String message) {
        return new RuleDraft(RuleKind.REQUIRED_IF, conditionField, null, null, operator, literal, targetField, message);
    }
}
