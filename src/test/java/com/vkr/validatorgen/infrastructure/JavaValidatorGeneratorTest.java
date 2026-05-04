package com.vkr.validatorgen.infrastructure;

import com.vkr.validatorgen.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaValidatorGeneratorTest {

    @Test
    void generatesIntComparisonRule() {
        var dto = new DtoSpec(
                "com.example",
                "OrderDto",
                Set.of("getAmount", "getLimit"),
                List.of("amount", "limit"),
                Map.of("amount", "int", "limit", "int")
        );
        List<RuleSpec> rules = List.of(new CompareFieldsRule("rule-1", "amount", CompareOp.GT, "limit", "amount", "Amount should be greater"));
        String code = new JavaValidatorGenerator().generate(dto, rules);

        assertTrue(code.contains("if (!(dto.getAmount() > dto.getLimit()))"));
        assertTrue(code.contains("new Violation(\"amount\", \"Amount should be greater\""));
    }

    @Test
    void generatesStringEqualityUsingObjectsEquals() {
        var dto = new DtoSpec(
                "com.example",
                "OrderDto",
                Set.of("getComment", "getExpectedComment"),
                List.of(),
                Map.of("comment", "String", "expectedComment", "String")
        );
        List<RuleSpec> rules = List.of(new CompareFieldsRule("rule-2", "comment", CompareOp.EQ, "expectedComment", "comment", "Comments must match"));

        String code = new JavaValidatorGenerator().generate(dto, rules);

        assertTrue(code.contains("java.util.Objects.equals(dto.getComment(), dto.getExpectedComment())"));
        assertTrue(code.contains("new Violation(\"comment\", \"Comments must match\""));
    }
}