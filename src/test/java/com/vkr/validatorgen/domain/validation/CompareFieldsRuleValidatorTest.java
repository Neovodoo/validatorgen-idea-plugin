package com.vkr.validatorgen.domain.validation;

import com.vkr.validatorgen.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CompareFieldsRuleValidatorTest {
    private final CompareFieldsRuleValidator validator = new CompareFieldsRuleValidator();

    @Test
    void acceptsNumericOrderingForExistingSameTypedDifferentFields() {
        var rule = rule("amount", CompareOp.GT, "limit");

        var diagnostics = validator.validate(rule, dto(
                field("amount", "int", true, false, false, true, false, false),
                field("limit", "int", true, false, false, true, false, false)
        ));

        assertTrue(diagnostics.isEmpty());
    }

    @Test
    void reportsMissingLeftAndRightFields() {
        var rule = rule("amount", CompareOp.GT, "limit");

        var diagnostics = validator.validate(rule, dto());

        assertEquals(List.of(
                CompareFieldsRuleValidator.LEFT_FIELD_MISSING,
                CompareFieldsRuleValidator.RIGHT_FIELD_MISSING
        ), diagnostics.stream().map(ValidationDiagnostic::code).toList());
        assertEquals("amount", diagnostics.get(0).fieldName());
        assertEquals("limit", diagnostics.get(1).fieldName());
        assertEquals(DiagnosticSeverity.ERROR, diagnostics.get(0).severity());
    }

    @Test
    void reportsSameFields() {
        var rule = rule("amount", CompareOp.EQ, "amount");

        var diagnostics = validator.validate(rule, dto(field("amount", "int", true, false, false, true, false, false)));

        assertEquals(CompareFieldsRuleValidator.SAME_FIELDS, diagnostics.get(0).code());
        assertEquals("rule-1", diagnostics.get(0).ruleId());
        assertEquals("amount", diagnostics.get(0).fieldName());
    }

    @Test
    void reportsTypeMismatch() {
        var rule = rule("amount", CompareOp.GT, "comment");

        var diagnostics = validator.validate(rule, dto(
                field("amount", "int", true, false, false, true, false, false),
                field("comment", "java.lang.String", false, true, false, false, false, false)
        ));

        assertEquals(CompareFieldsRuleValidator.TYPE_MISMATCH, diagnostics.get(0).code());
        assertTrue(diagnostics.get(0).message().contains("Type mismatch in rule"));
    }

    @Test
    void reportsOrderingOperatorForString() {
        var rule = rule("actual", CompareOp.GT, "expected");

        var diagnostics = validator.validate(rule, dto(
                field("actual", "java.lang.String", false, true, false, false, false, false),
                field("expected", "java.lang.String", false, true, false, false, false, false)
        ));

        assertEquals(CompareFieldsRuleValidator.OPERATOR_NOT_ALLOWED, diagnostics.get(0).code());
        assertTrue(diagnostics.get(0).message().contains("Operator > is not allowed"));
    }

    @Test
    void acceptsEqualityOperatorForString() {
        var rule = rule("actual", CompareOp.EQ, "expected");

        var diagnostics = validator.validate(rule, dto(
                field("actual", "java.lang.String", false, true, false, false, false, false),
                field("expected", "java.lang.String", false, true, false, false, false, false)
        ));

        assertTrue(diagnostics.isEmpty());
    }

    private static CompareFieldsRule rule(String left, CompareOp op, String right) {
        return new CompareFieldsRule("rule-1", left, op, right, left, "message");
    }

    private static DtoSpec dto(FieldMeta... fields) {
        return new DtoSpec("com.example", "OrderDto", Set.of(), List.of(fields));
    }

    private static FieldMeta field(String name, String type, boolean primitive, boolean stringLike, boolean booleanLike, boolean numericLike, boolean enumLike, boolean referenceLike) {
        return new FieldMeta(name, TypeRef.of(type, type), primitive, stringLike, booleanLike, numericLike, enumLike, referenceLike, "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
    }
}