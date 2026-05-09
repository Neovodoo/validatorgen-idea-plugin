package com.vkr.validatorgen.domain.validation;

import com.vkr.validatorgen.domain.DtoSpec;
import com.vkr.validatorgen.domain.FieldMeta;
import com.vkr.validatorgen.domain.RequiredIfRule;
import com.vkr.validatorgen.domain.ValuePresencePolicy;

import java.util.ArrayList;
import java.util.List;

public final class RequiredIfRuleValidator implements RuleValidator<RequiredIfRule> {
    public static final String TARGET_FIELD_MISSING = "REQUIRED_IF.TARGET_FIELD_MISSING";

    private final ConditionValidator conditionValidator;
    private final ValuePresencePolicy presencePolicy;

    public RequiredIfRuleValidator() {
        this(new ConditionValidator(), new ValuePresencePolicy());
    }

    public RequiredIfRuleValidator(ConditionValidator conditionValidator, ValuePresencePolicy presencePolicy) {
        this.conditionValidator = conditionValidator == null ? new ConditionValidator() : conditionValidator;
        this.presencePolicy = presencePolicy == null ? new ValuePresencePolicy() : presencePolicy;
    }

    @Override
    public List<ValidationDiagnostic> validate(RequiredIfRule rule, DtoSpec dtoSpec) {
        List<ValidationDiagnostic> diagnostics = new ArrayList<>(conditionValidator.validate(rule.getCondition(), dtoSpec, rule.getId()));
        FieldMeta target = dtoSpec.getField(rule.getTargetField());
        if (target == null) {
            diagnostics.add(ValidationDiagnostic.error(
                    TARGET_FIELD_MISSING,
                    "Unknown target field in REQUIRED_IF rule: " + rule.getTargetField() + ". Refresh fields and recreate rule.",
                    rule.getId(),
                    rule.getTargetField()
            ));
            return diagnostics;
        }
        presencePolicy.validateTargetField(target, rule.getId()).ifPresent(diagnostics::add);
        return diagnostics;
    }
}