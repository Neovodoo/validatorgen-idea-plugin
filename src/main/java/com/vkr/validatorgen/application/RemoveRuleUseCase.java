package com.vkr.validatorgen.application;

import com.vkr.validatorgen.domain.RuleRepository;

public final class RemoveRuleUseCase {
    private final RuleRepository repo;

    public RemoveRuleUseCase(RuleRepository repo) {
        this.repo = repo;
    }

    public Result execute(Integer index) {
        if (index == null || index < 0) return Result.error("Select a condition row to remove.");
        repo.removeAt(index);
        return Result.success("Removed condition at row " + index);
    }

    public sealed interface Result permits Result.Success, Result.Error {
        record Success(String message) implements Result {}
        record Error(String message) implements Result {}
        static Success success(String message) { return new Success(message); }
        static Error error(String message) { return new Error(message); }
    }
}
