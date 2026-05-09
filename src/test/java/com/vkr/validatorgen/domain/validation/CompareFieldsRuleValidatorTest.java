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

    @Test
    void acceptsPrimitiveIntEquality() {
        var diagnostics = validator.validate(rule("actual", CompareOp.EQ, "expected"), dto(
                field("actual", "int", true, false, false, true, false, false),
                field("expected", "int", true, false, false, true, false, false)
        ));

        assertTrue(diagnostics.isEmpty());
    }

    @Test
    void acceptsBoxedNumericOrderingForCurrentlyNumericLikeTypes() {
        var integerDiagnostics = validator.validate(rule("actual", CompareOp.GT, "expected"), dto(
                field("actual", "java.lang.Integer", false, false, false, true, false, true),
                field("expected", "java.lang.Integer", false, false, false, true, false, true)
        ));
        var longDiagnostics = validator.validate(rule("actual", CompareOp.LE, "expected"), dto(
                field("actual", "java.lang.Long", false, false, false, true, false, true),
                field("expected", "java.lang.Long", false, false, false, true, false, true)
        ));

        assertTrue(integerDiagnostics.isEmpty(), "Current validator accepts Integer > Integer; generator must make this null-safe.");
        assertTrue(longDiagnostics.isEmpty(), "Current validator accepts Long <= Long; generator must make this null-safe.");
    }

    @Test
    void acceptsPrimitiveAndBoxedBooleanEquality() {
        var primitiveDiagnostics = validator.validate(rule("actual", CompareOp.EQ, "expected"), dto(
                field("actual", "boolean", true, false, true, false, false, false),
                field("expected", "boolean", true, false, true, false, false, false)
        ));
        var boxedDiagnostics = validator.validate(rule("actual", CompareOp.EQ, "expected"), dto(
                field("actual", "java.lang.Boolean", false, false, true, false, false, true),
                field("expected", "java.lang.Boolean", false, false, true, false, false, true)
        ));

        assertTrue(primitiveDiagnostics.isEmpty());
        assertTrue(boxedDiagnostics.isEmpty());
    }

    @Test
    void reportsOrderingOperatorForLocalDate() {
        var diagnostics = validator.validate(rule("actual", CompareOp.LT, "expected"), dto(
                field("actual", "java.time.LocalDate", false, false, false, false, false, true),
                field("expected", "java.time.LocalDate", false, false, false, false, false, true)
        ));

        assertEquals(CompareFieldsRuleValidator.OPERATOR_NOT_ALLOWED, diagnostics.get(0).code(),
                "Current validator forbids LocalDate < LocalDate; future support should use compareTo instead of Java '<'.");
    }

    @Test
    void acceptsLocalDateEqualityAsReferenceEqualityRiskForGeneratorToAvoid() {
        var diagnostics = validator.validate(rule("actual", CompareOp.EQ, "expected"), dto(
                field("actual", "java.time.LocalDate", false, false, false, false, false, true),
                field("expected", "java.time.LocalDate", false, false, false, false, false, true)
        ));

        assertTrue(diagnostics.isEmpty(), "Equality for LocalDate is currently allowed; generator must not use reference equality.");
    }

    @Test
    void acceptsBigDecimalAndBigIntegerOrderingAsCurrentCompilationRisk() {
        var bigDecimalDiagnostics = validator.validate(rule("actual", CompareOp.GT, "expected"), dto(
                field("actual", "java.math.BigDecimal", false, false, false, true, false, true),
                field("expected", "java.math.BigDecimal", false, false, false, true, false, true)
        ));
        var bigIntegerDiagnostics = validator.validate(rule("actual", CompareOp.GT, "expected"), dto(
                field("actual", "java.math.BigInteger", false, false, false, true, false, true),
                field("expected", "java.math.BigInteger", false, false, false, true, false, true)
        ));

        assertTrue(bigDecimalDiagnostics.isEmpty(), "TODO: either forbid BigDecimal ordering or emit compareTo-based code.");
        assertTrue(bigIntegerDiagnostics.isEmpty(), "TODO: either forbid BigInteger ordering or emit compareTo-based code.");
    }

    @Test
    void acceptsEnumEqualityAndRejectsEnumOrdering() {
        var equalityDiagnostics = validator.validate(rule("actual", CompareOp.EQ, "expected"), dto(
                field("actual", "com.example.Status", false, false, false, false, true, true),
                field("expected", "com.example.Status", false, false, false, false, true, true)
        ));
        var orderingDiagnostics = validator.validate(rule("actual", CompareOp.GT, "expected"), dto(
                field("actual", "com.example.Status", false, false, false, false, true, true),
                field("expected", "com.example.Status", false, false, false, false, true, true)
        ));

        assertTrue(equalityDiagnostics.isEmpty());
        assertEquals(CompareFieldsRuleValidator.OPERATOR_NOT_ALLOWED, orderingDiagnostics.get(0).code());
    }

    @Test
    void acceptsReferenceEqualityAndRejectsReferenceOrdering() {
        var equalityDiagnostics = validator.validate(rule("actual", CompareOp.EQ, "expected"), dto(
                field("actual", "com.example.Money", false, false, false, false, false, true),
                field("expected", "com.example.Money", false, false, false, false, false, true)
        ));
        var orderingDiagnostics = validator.validate(rule("actual", CompareOp.GT, "expected"), dto(
                field("actual", "com.example.Money", false, false, false, false, false, true),
                field("expected", "com.example.Money", false, false, false, false, false, true)
        ));

        assertTrue(equalityDiagnostics.isEmpty(), "Equality for arbitrary references is allowed; generator must avoid accidental reference equality.");
        assertEquals(CompareFieldsRuleValidator.OPERATOR_NOT_ALLOWED, orderingDiagnostics.get(0).code());
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