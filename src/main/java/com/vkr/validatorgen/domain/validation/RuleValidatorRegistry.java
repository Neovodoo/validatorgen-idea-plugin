package com.vkr.validatorgen.domain.validation;

import com.vkr.validatorgen.domain.CompareFieldsRule;
import com.vkr.validatorgen.domain.DtoSpec;
import com.vkr.validatorgen.domain.RuleKind;
import com.vkr.validatorgen.domain.RuleSpec;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class RuleValidatorRegistry {
    public static final String UNSUPPORTED_RULE_KIND = "RULE.UNSUPPORTED_KIND";

    private final Map<RuleKind, RuleValidator<? extends RuleSpec>> validators;

    public RuleValidatorRegistry(Map<RuleKind, RuleValidator<? extends RuleSpec>> validators) {
        this.validators = new EnumMap<>(RuleKind.class);
        this.validators.putAll(validators);
    }

    public static RuleValidatorRegistry defaults() {
        return new RuleValidatorRegistry(Map.of(
                RuleKind.COMPARE_FIELDS, new CompareFieldsRuleValidator()
        ));
    }

    public List<ValidationDiagnostic> validate(RuleSpec rule, DtoSpec dtoSpec) {
        RuleValidator<? extends RuleSpec> validator = validators.get(rule.getKind());
        if (validator == null) {
            return List.of(ValidationDiagnostic.error(
                    UNSUPPORTED_RULE_KIND,
                    "Unsupported rule kind: " + rule.getKind(),
                    rule.getId(),
                    null
            ));
        }
        return validateWithTypedValidator(validator, rule, dtoSpec);
    }

    @SuppressWarnings("unchecked")
    private <T extends RuleSpec> List<ValidationDiagnostic> validateWithTypedValidator(RuleValidator<T> validator, RuleSpec rule, DtoSpec dtoSpec) {
        return validator.validate((T) normalize(rule), dtoSpec);
    }

    private RuleSpec normalize(RuleSpec rule) {
        if (rule instanceof com.vkr.validatorgen.domain.CompareRule compareRule) {
            return compareRule.toCompareFieldsRule();
        }
        if (rule instanceof CompareFieldsRule) {
            return rule;
        }
        return rule;
    }
}