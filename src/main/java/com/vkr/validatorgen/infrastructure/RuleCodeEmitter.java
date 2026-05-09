package com.vkr.validatorgen.infrastructure;

import com.vkr.validatorgen.domain.DtoSpec;
import com.vkr.validatorgen.domain.RuleKind;
import com.vkr.validatorgen.domain.RuleSpec;

public interface RuleCodeEmitter<T extends RuleSpec> {
    RuleKind getKind();

    Class<T> getRuleType();

    RuleCode emit(DtoSpec dto, T rule, int ruleIndex, JavaRuleCodeContext context);
}