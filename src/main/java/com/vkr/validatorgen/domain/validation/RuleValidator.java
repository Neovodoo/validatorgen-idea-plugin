package com.vkr.validatorgen.domain.validation;

import com.vkr.validatorgen.domain.DtoSpec;
import com.vkr.validatorgen.domain.RuleSpec;

import java.util.List;

public interface RuleValidator<T extends RuleSpec> {
    List<ValidationDiagnostic> validate(T rule, DtoSpec dtoSpec);
}