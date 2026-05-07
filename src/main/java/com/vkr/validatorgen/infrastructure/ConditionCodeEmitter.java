package com.vkr.validatorgen.infrastructure;

import com.vkr.validatorgen.domain.ConditionOperator;
import com.vkr.validatorgen.domain.ConditionSpec;
import com.vkr.validatorgen.domain.FieldMeta;
import com.vkr.validatorgen.domain.ConditionLiteralRenderer;

public final class ConditionCodeEmitter {
    public String emit(String fieldAccess, FieldMeta fieldMeta, ConditionSpec condition) {
        String literal = ConditionLiteralRenderer.renderLiteral(condition.rawLiteral(), fieldMeta);
        String equalityExpr;
        if (fieldMeta.isStringLike()) {
            equalityExpr = "java.util.Objects.equals(" + fieldAccess + ", " + literal + ")";
        } else if (fieldMeta.isBooleanLike()) {
            equalityExpr = booleanEquality(fieldAccess, fieldMeta, literal);
        } else if (fieldMeta.isNumericLike()) {
            equalityExpr = numericEquality(fieldAccess, fieldMeta, literal);
        } else {
            throw new IllegalArgumentException("Condition type is not supported for Java code generation: " + fieldMeta.javaType());
        }
        return condition.operator() == ConditionOperator.NE ? negate(equalityExpr) : equalityExpr;
    }

    private String negate(String equalityExpr) {
        if (equalityExpr.contains(" == ")) {
            return "!(" + equalityExpr + ")";
        }
        return "!" + equalityExpr;
    }

    private String booleanEquality(String fieldAccess, FieldMeta fieldMeta, String literal) {
        if (ConditionLiteralRenderer.isPrimitiveBoolean(fieldMeta)) {
            return fieldAccess + " == " + literal;
        }
        if ("true".equals(literal)) {
            return "java.lang.Boolean.TRUE.equals(" + fieldAccess + ")";
        }
        return "java.lang.Boolean.FALSE.equals(" + fieldAccess + ")";
    }

    private String numericEquality(String fieldAccess, FieldMeta fieldMeta, String literal) {
        if (fieldMeta.isPrimitive()) {
            return fieldAccess + " == " + literal;
        }
        if (ConditionLiteralRenderer.isNumericWrapper(fieldMeta)) {
            return "java.util.Objects.equals(" + fieldAccess + ", " + literal + ")";
        }
        throw new IllegalArgumentException("Numeric condition type is not supported for Java code generation: " + fieldMeta.javaType());
    }
}