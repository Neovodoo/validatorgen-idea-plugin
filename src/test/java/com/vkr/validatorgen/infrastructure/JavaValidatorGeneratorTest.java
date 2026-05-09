package com.vkr.validatorgen.infrastructure;

import com.vkr.validatorgen.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaValidatorGeneratorTest {

    @Test
    void generatesIntComparisonRule() {
        var dto = new DtoSpec(
                "com.example",
                "OrderDto",
                Set.of("getAmount", "getLimit"),
                List.of(
                        new FieldMeta("amount", TypeRef.of("int", "int"), true, false, false, true, false, false, "getAmount"),
                        new FieldMeta("limit", TypeRef.of("int", "int"), true, false, false, true, false, false, "getLimit")
                )
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
                List.of(
                        new FieldMeta("comment", TypeRef.of("java.lang.String", "String"), false, true, false, false, false, false, "getComment"),
                        new FieldMeta("expectedComment", TypeRef.of("java.lang.String", "String"), false, true, false, false, false, false, "getExpectedComment")
                )
        );
        List<RuleSpec> rules = List.of(new CompareFieldsRule("rule-2", "comment", CompareOp.EQ, "expectedComment", "comment", "Comments must match"));

        String code = new JavaValidatorGenerator().generate(dto, rules);

        assertTrue(code.contains("java.util.Objects.equals(dto.getComment(), dto.getExpectedComment())"));
        assertTrue(code.contains("new Violation(\"comment\", \"Comments must match\""));
    }

    @Test
    void delegatesCompareFieldsRuleGenerationToEmitterRegistry() {
        var dto = new DtoSpec(
                "com.example",
                "OrderDto",
                Set.of("getAmount", "getLimit"),
                List.of(
                        new FieldMeta("amount", TypeRef.of("int", "int"), true, false, false, true, false, false, "getAmount"),
                        new FieldMeta("limit", TypeRef.of("int", "int"), true, false, false, true, false, false, "getLimit")
                )
        );
        RuleCodeEmitter<CompareFieldsRule> sentinelEmitter = new RuleCodeEmitter<>() {
            @Override
            public RuleKind getKind() {
                return RuleKind.COMPARE_FIELDS;
            }

            @Override
            public Class<CompareFieldsRule> getRuleType() {
                return CompareFieldsRule.class;
            }

            @Override
            public RuleCode emit(DtoSpec dto, CompareFieldsRule rule, int ruleIndex, JavaRuleCodeContext context) {
                return new RuleCode("EMITTER_GENERATED_CONDITION", "emitter-rule-id", "emitter comment");
            }
        };
        var generator = new JavaValidatorGenerator(new RuleCodeEmitterRegistry(List.of(sentinelEmitter)));
        List<RuleSpec> rules = List.of(new CompareFieldsRule("rule-1", "amount", CompareOp.GT, "limit", "amount", "Amount should be greater"));

        String code = generator.generate(dto, rules);

        assertTrue(code.contains("// Rule emitter-rule-id: emitter comment"));
        assertTrue(code.contains("if (!(EMITTER_GENERATED_CONDITION))"));
        assertTrue(code.contains("new Violation(\"amount\", \"Amount should be greater\", \"emitter-rule-id\")"));
    }
}