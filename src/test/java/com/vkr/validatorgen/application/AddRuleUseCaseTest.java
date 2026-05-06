package com.vkr.validatorgen.application;

import com.vkr.validatorgen.domain.*;
import com.vkr.validatorgen.infrastructure.InMemoryRuleRepository;
import com.vkr.validatorgen.presentation.RuleDraft;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AddRuleUseCaseTest {

    @Test
    void addsCompareRuleFromDraft() {
        var repo = new InMemoryRuleRepository();
        var useCase = new AddRuleUseCase(repo, ignored -> dto());

        var draft = new RuleDraft("amount", CompareOp.GT, "limit", "amount", "Amount must be greater");
        var result = useCase.execute(draft, "dto");

        assertInstanceOf(AddRuleUseCase.Result.Success.class, result);
        assertEquals(1, repo.all().size());
        var rule = (CompareFieldsRule) repo.all().get(0);
        assertEquals("amount", rule.getLeft());
        assertEquals(CompareOp.GT, rule.getOp());
        assertEquals("limit", rule.getRight());
        assertEquals("amount", rule.getViolationTarget());
        assertEquals("Amount must be greater", rule.getMessage());
    }

    @Test
    void rejectsBusinessValidationErrorsFromRuleValidator() {
        var repo = new InMemoryRuleRepository();
        var useCase = new AddRuleUseCase(repo, ignored -> dto());

        var result = useCase.execute(new RuleDraft("amount", CompareOp.GT, "missing", "amount", "bad"), "dto");

        assertInstanceOf(AddRuleUseCase.Result.Error.class, result);
        assertTrue(((AddRuleUseCase.Result.Error) result).message().contains("Unknown right field"));
        assertTrue(repo.all().isEmpty());
    }

    private static DtoSpec dto() {
        return new DtoSpec("com.example", "OrderDto", Set.of(), List.of(
                new FieldMeta("amount", TypeRef.of("int", "int"), true, false, false, true, false, false, "getAmount"),
                new FieldMeta("limit", TypeRef.of("int", "int"), true, false, false, true, false, false, "getLimit")
        ));
    }
}
