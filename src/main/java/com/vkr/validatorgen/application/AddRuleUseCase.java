package com.vkr.validatorgen.application;

import com.vkr.validatorgen.domain.CompareRule;
import com.vkr.validatorgen.domain.RuleRepository;
import com.vkr.validatorgen.presentation.RuleDraft;

public final class AddRuleUseCase {
    private final RuleRepository repo;

    public AddRuleUseCase(RuleRepository repo) {
        this.repo = repo;
    }

    public Result execute(RuleDraft d) {
        String left = d.left();
        String right = d.right();
        String target = d.target();
        var op = d.op();
        String message = d.message() == null ? "" : d.message().trim();

        if (left == null || left.isBlank() || right == null || right.isBlank() || target == null || target.isBlank() || op == null) {
            return Result.error("Please select A, B, Target.");
        }
        if (left.equals(right)) {
            return Result.error("A and B should be different.");
        }
        if (message.isBlank()) {
            return Result.error("Message must not be empty.");
        }

        repo.add(new CompareRule(left, op, right, target, message));
        return Result.success("Added condition: " + left + " " + op.getSymbol() + " " + right + " (target=" + target + ")");
    }

    public sealed interface Result permits Result.Success, Result.Error {
        record Success(String message) implements Result {}
        record Error(String message) implements Result {}
        static Success success(String message) { return new Success(message); }
        static Error error(String message) { return new Error(message); }
    }
}
