package com.vkr.validatorgen.prototype.stubs;

import com.vkr.validatorgen.prototype.application.SpecificationValidationService;
import com.vkr.validatorgen.prototype.model.*;

import java.util.ArrayList;
import java.util.List;

public final class SpecificationValidationServiceStub implements SpecificationValidationService {
    @Override
    public ValidationResult validate(DtoDescriptor dto, List<RuleSpec> rules, GenerationMode mode) {
        List<ValidationIssue> issues = new ArrayList<>();

        if (dto == null) {
            issues.add(new ValidationIssue(Severity.ERROR, "DTO is not selected."));
        }
        if (rules.isEmpty()) {
            issues.add(new ValidationIssue(Severity.ERROR, "At least one rule is required for generation."));
        }
        if (rules.size() > 200) {
            issues.add(new ValidationIssue(Severity.WARNING, "Prototype target is up to 200 rules per session."));
        }

        if (mode == GenerationMode.JAKARTA_BEAN_VALIDATION) {
            issues.add(new ValidationIssue(Severity.INFO, "Jakarta mode will emit annotation-compatible stub artifacts."));
        } else {
            issues.add(new ValidationIssue(Severity.INFO, "Explicit mode will emit validator method stubs."));
        }

        boolean valid = issues.stream().noneMatch(issue -> issue.severity() == Severity.ERROR);
        return new ValidationResult(valid, List.copyOf(issues));
    }
}
