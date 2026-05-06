package com.vkr.validatorgen.application;

import com.vkr.validatorgen.domain.*;
import com.vkr.validatorgen.domain.validation.RuleValidatorRegistry;
import com.vkr.validatorgen.domain.validation.ValidationDiagnostic;

public final class GenerateCodeUseCase {
    private final DtoParser parser;
    private final RuleRepository repo;
    private final CodeGenerator generator;
    private final RuleValidatorRegistry validatorRegistry;

    public GenerateCodeUseCase(DtoParser parser, RuleRepository repo, CodeGenerator generator, RuleValidatorRegistry validatorRegistry)  {
        this.parser = parser;
        this.repo = repo;
        this.generator = generator;
        this.validatorRegistry = validatorRegistry;
    }

    public GenerateCodeUseCase(DtoParser parser, RuleRepository repo, CodeGenerator generator) {
        this(parser, repo, generator, RuleValidatorRegistry.defaults());
    }

    public Result execute(String dtoText) {
        var rules = repo.all();
        if (rules.isEmpty()) return Result.error("No conditions. Add at least one.");

        DtoSpec dto = parser.parse(dtoText);
        if (dto == null) return Result.error("Could not parse DTO class from editor text.");

        for (var rule : rules) {
            var diagnostics = validatorRegistry.validate(rule, dto);
            if (!diagnostics.isEmpty()) {
                return Result.error(format(diagnostics.get(0)));
            }
        }

        String code = generator.generate(dto, rules);
        return Result.success(code, dto.getClassName(), rules.size());
    }

    private String format(ValidationDiagnostic diagnostic) {
        return diagnostic.message();
    }

    public sealed interface Result permits Result.Success, Result.Error {
        record Success(String code, String dtoClassName, int rulesCount) implements Result {}
        record Error(String message) implements Result {}

        static Success success(String code, String dtoClassName, int rulesCount) { return new Success(code, dtoClassName, rulesCount); }
        static Error error(String message) { return new Error(message); }
    }
}
