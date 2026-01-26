package com.vkr.validatorgen.presentation;

import com.vkr.validatorgen.domain.CompareOp;

public record RuleDraft(
        String left,
        CompareOp op,
        String right,
        String target,
        String message
) {}
