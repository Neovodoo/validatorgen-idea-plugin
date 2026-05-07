package com.vkr.validatorgen.domain.validation;

import com.vkr.validatorgen.domain.ConditionOperator;
import com.vkr.validatorgen.domain.ConditionSpec;
import com.vkr.validatorgen.domain.DtoSpec;
import com.vkr.validatorgen.domain.FieldMeta;
import com.vkr.validatorgen.domain.ConditionLiteralRenderer;

import java.util.ArrayList;
import java.util.List;

public final class ConditionValidator {
    public static final String FIELD_MISSING = "CONDITION.FIELD_MISSING";
    public static final String OPERATOR_NOT_ALLOWED = "CONDITION.OPERATOR_NOT_ALLOWED";
    public static final String LITERAL_NOT_SUPPORTED = "CONDITION.LITERAL_NOT_SUPPORTED";

    public List<ValidationDiagnostic> validate(ConditionSpec condition, DtoSpec dtoSpec, String ruleId) {
        List<ValidationDiagnostic> diagnostics = new ArrayList<>();
        FieldMeta field = dtoSpec.getField(condition.fieldName());
        if (field == null) {
            diagnostics.add(ValidationDiagnostic.error(
                    FIELD_MISSING,
                    "Unknown condition field: " + condition.fieldName() + ". Refresh fields and recreate rule.",
                    ruleId,
                    condition.fieldName()
            ));
            return diagnostics;
        }

        if (!isOperatorAllowed(condition.operator(), field)) {
            diagnostics.add(ValidationDiagnostic.error(
                    OPERATOR_NOT_ALLOWED,
                    "Operator " + condition.operator().getSymbol() + " is not allowed for condition field type " + field.javaType() + ".",
                    ruleId,
                    condition.fieldName()
            ));
        }

        try {
            ConditionLiteralRenderer.renderLiteral(condition.rawLiteral(), field);
        } catch (IllegalArgumentException ex) {
            diagnostics.add(ValidationDiagnostic.error(
                    LITERAL_NOT_SUPPORTED,
                    ex.getMessage(),
                    ruleId,
                    condition.fieldName()
            ));
        }

        return diagnostics;
    }

    private boolean isOperatorAllowed(ConditionOperator operator, FieldMeta field) {
        if (operator != ConditionOperator.EQ && operator != ConditionOperator.NE) {
            return false;
        }
        return field.isStringLike() || field.isBooleanLike() || field.isNumericLike();
    }
}