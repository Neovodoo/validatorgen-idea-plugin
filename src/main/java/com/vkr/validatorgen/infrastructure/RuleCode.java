package com.vkr.validatorgen.infrastructure;

public record RuleCode(
        String conditionExpression,
        String ruleId,
        String comment
) {
    public RuleCode {
        conditionExpression = conditionExpression == null ? "" : conditionExpression;
        ruleId = ruleId == null ? "" : ruleId;
        comment = comment == null ? "" : comment;
    }
}