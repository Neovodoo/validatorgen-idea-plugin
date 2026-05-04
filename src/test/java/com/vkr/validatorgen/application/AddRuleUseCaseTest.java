package com.vkr.validatorgen.application;

import com.vkr.validatorgen.domain.CompareFieldsRule;
import com.vkr.validatorgen.domain.CompareOp;
import com.vkr.validatorgen.infrastructure.InMemoryRuleRepository;
import com.vkr.validatorgen.presentation.RuleDraft;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AddRuleUseCaseTest {

    @Test
    void addsCompareRuleFromDraft() {
        var repo = new InMemoryRuleRepository();
        var useCase = new AddRuleUseCase(repo);

        var draft = new RuleDraft("amount", CompareOp.GT, "limit", "amount", "Amount must be greater");
        var result = useCase.execute(draft);

        assertInstanceOf(AddRuleUseCase.Result.Success.class, result);
        assertEquals(1, repo.all().size());
        var rule = (CompareFieldsRule) repo.all().get(0);
        assertEquals("amount", rule.getLeft());
        assertEquals(CompareOp.GT, rule.getOp());
        assertEquals("limit", rule.getRight());
        assertEquals("amount", rule.getViolationTarget());
        assertEquals("Amount must be greater", rule.getMessage());
    }
}
