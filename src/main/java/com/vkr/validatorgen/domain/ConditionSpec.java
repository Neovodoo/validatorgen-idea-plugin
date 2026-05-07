package com.vkr.validatorgen.domain;

import java.util.Objects;

public record ConditionSpec(
        String fieldName,
        ConditionOperator operator,
        String rawLiteral
) {
    public ConditionSpec {
        fieldName = Objects.requireNonNullElse(fieldName, "");
        operator = Objects.requireNonNull(operator);
        rawLiteral = Objects.requireNonNullElse(rawLiteral, "");
    }
}