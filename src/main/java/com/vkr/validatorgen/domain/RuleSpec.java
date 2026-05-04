package com.vkr.validatorgen.domain;

import java.util.List;

public sealed interface RuleSpec permits CompareFieldsRule, CompareRule {
    String getId();
    RuleKind getKind();
    String getViolationTarget();
    String getMessage();
    List<String> getInvolvedFields();
}