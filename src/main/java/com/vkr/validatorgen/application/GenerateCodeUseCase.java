package com.vkr.validatorgen.application;

import com.vkr.validatorgen.domain.CodeGenerator;
import com.vkr.validatorgen.domain.DtoParser;
import com.vkr.validatorgen.domain.DtoSpec;
import com.vkr.validatorgen.domain.RuleRepository;

public final class GenerateCodeUseCase {
    private final DtoParser parser;
    private final RuleRepository repo;
    private final CodeGenerator generator;

    public GenerateCodeUseCase(DtoParser parser, RuleRepository repo, CodeGenerator generator) {
        this.parser = parser;
        this.repo = repo;
        this.generator = generator;
    }

    public Result execute(String dtoText) {
        var rules = repo.all();
        if (rules.isEmpty()) return Result.error("No conditions. Add at least one.");

        DtoSpec dto = parser.parse(dtoText);
        if (dto == null) return Result.error("Could not parse DTO class from editor text.");

        String code = generator.generate(dto, rules);
        return Result.success(code, dto.getClassName(), rules.size());
    }

    public sealed interface Result permits Result.Success, Result.Error {
        record Success(String code, String dtoClassName, int rulesCount) implements Result {}
        record Error(String message) implements Result {}

        static Success success(String code, String dtoClassName, int rulesCount) { return new Success(code, dtoClassName, rulesCount); }
        static Error error(String message) { return new Error(message); }
    }
}
