package com.vkr.validatorgen.domain.validation;

import com.vkr.validatorgen.domain.CompareFieldsRule;
import com.vkr.validatorgen.domain.CompareOp;
import com.vkr.validatorgen.domain.DtoSpec;
import com.vkr.validatorgen.domain.FieldMeta;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class CompareFieldsRuleValidator implements RuleValidator<CompareFieldsRule> {
    public static final String LEFT_FIELD_MISSING = "COMPARE_FIELDS.LEFT_FIELD_MISSING";
    public static final String RIGHT_FIELD_MISSING = "COMPARE_FIELDS.RIGHT_FIELD_MISSING";
    public static final String SAME_FIELDS = "COMPARE_FIELDS.SAME_FIELDS";
    public static final String TYPE_MISMATCH = "COMPARE_FIELDS.TYPE_MISMATCH";
    public static final String OPERATOR_NOT_ALLOWED = "COMPARE_FIELDS.OPERATOR_NOT_ALLOWED";

    private static final Set<CompareOp> EQUALITY_OPS = EnumSet.of(CompareOp.EQ, CompareOp.NE);
    private static final Set<CompareOp> ORDERING_OPS = EnumSet.of(CompareOp.GT, CompareOp.GE, CompareOp.LT, CompareOp.LE);

    @Override
    public List<ValidationDiagnostic> validate(CompareFieldsRule rule, DtoSpec dtoSpec) {
        List<ValidationDiagnostic> diagnostics = new ArrayList<>();
        FieldMeta left = dtoSpec.getField(rule.getLeft());
        FieldMeta right = dtoSpec.getField(rule.getRight());

        if (left == null) {
            diagnostics.add(error(LEFT_FIELD_MISSING, "Unknown left field in rule: " + rule.getLeft() + ". Refresh fields and recreate rule.", rule, rule.getLeft()));
        }
        if (right == null) {
            diagnostics.add(error(RIGHT_FIELD_MISSING, "Unknown right field in rule: " + rule.getRight() + ". Refresh fields and recreate rule.", rule, rule.getRight()));
        }
        if (left == null || right == null) {
            return diagnostics;
        }

        if (rule.getLeft().equals(rule.getRight())) {
            diagnostics.add(error(SAME_FIELDS, "A and B should be different.", rule, rule.getLeft()));
        }
        if (!isSameType(left, right)) {
            diagnostics.add(error(TYPE_MISMATCH, "Type mismatch in rule: " + rule.getLeft() + " (" + left.javaType() + ") vs " + rule.getRight() + " (" + right.javaType() + ").", rule, rule.getLeft()));
            return diagnostics;
        }
        if (!isOperatorAllowed(rule.getOp(), left)) {
            diagnostics.add(error(OPERATOR_NOT_ALLOWED, "Operator " + rule.getOp().getSymbol() + " is not allowed for type " + left.javaType() + ".", rule, rule.getLeft()));
        }

        return diagnostics;
    }

    private boolean isSameType(FieldMeta left, FieldMeta right) {
        return left.javaType().normalizedName().equals(right.javaType().normalizedName());
    }

    private boolean isOperatorAllowed(CompareOp op, FieldMeta field) {
        if (field.isNumericLike()) return EQUALITY_OPS.contains(op) || ORDERING_OPS.contains(op);
        if (field.isStringLike()) return EQUALITY_OPS.contains(op);
        if (field.isBooleanLike() || field.isEnumLike() || field.isReferenceLike()) return EQUALITY_OPS.contains(op);
        return EQUALITY_OPS.contains(op);
    }

    private ValidationDiagnostic error(String code, String message, CompareFieldsRule rule, String fieldName) {
        return ValidationDiagnostic.error(code, message, rule.getId(), fieldName);
    }
}