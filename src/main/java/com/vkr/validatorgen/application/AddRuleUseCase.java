package com.vkr.validatorgen.application;

import com.vkr.validatorgen.domain.*;
import com.vkr.validatorgen.domain.validation.RuleValidatorRegistry;
import com.vkr.validatorgen.domain.validation.ValidationDiagnostic;
import com.vkr.validatorgen.presentation.RuleDraft;

import java.util.UUID;

public final class AddRuleUseCase {
    private final RuleRepository repo;
    private final DtoParser parser;
    private final RuleValidatorRegistry validatorRegistry;

    public AddRuleUseCase(RuleRepository repo, DtoParser parser, RuleValidatorRegistry validatorRegistry) {
        this.repo = repo;
        this.parser = parser;
        this.validatorRegistry = validatorRegistry;
    }

    public AddRuleUseCase(RuleRepository repo, DtoParser parser) {
        this(repo, parser, RuleValidatorRegistry.defaults());
    }

    public Result execute(RuleDraft d, String dtoText) {
        RuleKind kind = d.kind() == null ? RuleKind.COMPARE_FIELDS : d.kind();
        String message = d.message() == null ? "" : d.message().trim();

        if (message.isBlank()) {
            return Result.error("Message must not be empty.");
        }

        DtoSpec dto = parser.parse(dtoText);
        if (dto == null) return Result.error("Could not parse DTO class from editor text.");

        RuleSpec rule = switch (kind) {
            case COMPARE_FIELDS -> buildCompareFieldsRule(d, message);
            case REQUIRED_IF -> buildRequiredIfRule(d, message);
        };
        if (rule == null) {
            return Result.error(kind == RuleKind.REQUIRED_IF
                    ? "Please select condition field, operator, literal and target field."
                    : "Please select A, B, Target.");
        }

        var diagnostics = validatorRegistry.validate(rule, dto);
        if (!diagnostics.isEmpty()) {
            return Result.error(format(diagnostics.get(0)));
        }

        repo.add(rule);
        return Result.success(successMessage(rule));
    }

    private RuleSpec buildCompareFieldsRule(RuleDraft d, String message) {
        String left = d.left();
        String right = d.right();
        String target = d.target();
        var op = d.op();
        if (left == null || left.isBlank() || right == null || right.isBlank() || target == null || target.isBlank() || op == null) {
            return null;
        }
        return new CompareFieldsRule(UUID.randomUUID().toString(), left, op, right, target, message);
    }

    private RuleSpec buildRequiredIfRule(RuleDraft d, String message) {
        String conditionField = d.left();
        String target = d.target();
        ConditionOperator op = d.conditionOperator();
        String literal = d.conditionLiteral();
        if (conditionField == null || conditionField.isBlank() || target == null || target.isBlank() || op == null || literal == null || literal.isBlank()) {
            return null;
        }
        return new RequiredIfRule(UUID.randomUUID().toString(), new ConditionSpec(conditionField, op, literal), target, target, message);
    }

    private String successMessage(RuleSpec rule) {
        if (rule instanceof RequiredIfRule requiredIf) {
            return "Added condition: " + requiredIf.getTargetField() + " required if "
                    + requiredIf.getCondition().fieldName() + " " + requiredIf.getCondition().operator().getSymbol()
                    + " " + requiredIf.getCondition().rawLiteral();
        }
        CompareFieldsRule compare = (CompareFieldsRule) rule;
        return "Added condition: " + compare.getLeft() + " " + compare.getOp().getSymbol() + " " + compare.getRight()
                + " (target=" + compare.getViolationTarget() + ")";
    }

    private String format(ValidationDiagnostic diagnostic) {
        return diagnostic.message();
    }

    public sealed interface Result permits Result.Success, Result.Error {
        record Success(String message) implements Result {}
        record Error(String message) implements Result {}
        static Success success(String message) { return new Success(message); }
        static Error error(String message) { return new Error(message); }
    }
}
