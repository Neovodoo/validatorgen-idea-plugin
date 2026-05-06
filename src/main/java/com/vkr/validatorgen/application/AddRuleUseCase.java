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
        String left = d.left();
        String right = d.right();
        String target = d.target();
        var op = d.op();
        String message = d.message() == null ? "" : d.message().trim();

        if (left == null || left.isBlank() || right == null || right.isBlank() || target == null || target.isBlank() || op == null) {
            return Result.error("Please select A, B, Target.");
        }
        if (message.isBlank()) {
            return Result.error("Message must not be empty.");
        }

        DtoSpec dto = parser.parse(dtoText);
        if (dto == null) return Result.error("Could not parse DTO class from editor text.");

        var rule = new CompareFieldsRule(UUID.randomUUID().toString(), left, op, right, target, message);
        var diagnostics = validatorRegistry.validate(rule, dto);
        if (!diagnostics.isEmpty()) {
            return Result.error(format(diagnostics.get(0)));
        }

        repo.add(rule);
        return Result.success("Added condition: " + left + " " + op.getSymbol() + " " + right + " (target=" + target + ")");
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
