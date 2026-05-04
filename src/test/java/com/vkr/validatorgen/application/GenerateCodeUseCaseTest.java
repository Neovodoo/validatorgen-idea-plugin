package com.vkr.validatorgen.application;

import com.vkr.validatorgen.domain.*;
import com.vkr.validatorgen.infrastructure.InMemoryRuleRepository;
import com.vkr.validatorgen.infrastructure.JavaValidatorGenerator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class GenerateCodeUseCaseTest {
    @Test
    void returnsErrorWhenRuleFieldIsMissingInDto() {
        RuleRepository repo = new InMemoryRuleRepository();
        repo.add(new CompareFieldsRule("rule-1", "amount", CompareOp.GT, "missingField", "amount", "bad"));

        DtoParser parser = ignored -> dtoWithTypes(Map.of("amount", "int", "limit", "int"));
        var useCase = new GenerateCodeUseCase(parser, repo, new JavaValidatorGenerator());

        var result = useCase.execute("dto");

        assertInstanceOf(GenerateCodeUseCase.Result.Error.class, result);
        var error = (GenerateCodeUseCase.Result.Error) result;
        assertTrue(error.message().contains("Unknown field in rule"));
    }

    @Test
    void returnsErrorWhenRuleFieldTypesMismatch() {
        RuleRepository repo = new InMemoryRuleRepository();
        repo.add(new CompareFieldsRule("rule-2", "amount", CompareOp.GT, "comment", "amount", "bad"));

        DtoParser parser = ignored -> dtoWithTypes(Map.of("amount", "int", "comment", "String"));
        var useCase = new GenerateCodeUseCase(parser, repo, new JavaValidatorGenerator());

        var result = useCase.execute("dto");

        assertInstanceOf(GenerateCodeUseCase.Result.Error.class, result);
        var error = (GenerateCodeUseCase.Result.Error) result;
        assertTrue(error.message().contains("Type mismatch in rule"));
    }

    private static DtoSpec dtoWithTypes(Map<String, String> fieldTypes) {
        return new DtoSpec("com.example", "OrderDto", Set.of("getAmount", "getLimit", "getComment"), List.of(), fieldTypes);
    }
}
