package com.vkr.validatorgen.domain.validation;

import com.vkr.validatorgen.domain.*;
import com.vkr.validatorgen.infrastructure.ConditionCodeEmitter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConditionValidatorTest {
    private final ConditionValidator validator = new ConditionValidator();
    private final ConditionCodeEmitter emitter = new ConditionCodeEmitter();

    @Test
    void acceptsStringConditionAndEmitsObjectsEquals() {
        FieldMeta status = field("status", "java.lang.String", false, true, false, false);
        ConditionSpec condition = new ConditionSpec("status", ConditionOperator.EQ, "ACTIVE");

        assertTrue(validator.validate(condition, dto(status), "rule-1").isEmpty());
        assertEquals("java.util.Objects.equals(dto.getStatus(), \"ACTIVE\")", emitter.emit("dto.getStatus()", status, condition));
    }

    @Test
    void acceptsStringNotEqualsConditionAndEscapesLiteral() {
        FieldMeta status = field("status", "java.lang.String", false, true, false, false);
        ConditionSpec condition = new ConditionSpec("status", ConditionOperator.NE, "A\"B");

        assertTrue(validator.validate(condition, dto(status), "rule-1").isEmpty());
        assertEquals("!java.util.Objects.equals(dto.getStatus(), \"A\\\"B\")", emitter.emit("dto.getStatus()", status, condition));
    }

    @Test
    void acceptsPrimitiveBooleanConditionAndEmitsDirectComparison() {
        FieldMeta enabled = field("enabled", "boolean", true, false, true, false);
        ConditionSpec condition = new ConditionSpec("enabled", ConditionOperator.EQ, "true");

        assertTrue(validator.validate(condition, dto(enabled), "rule-1").isEmpty());
        assertEquals("dto.isEnabled() == true", emitter.emit("dto.isEnabled()", enabled, condition));
    }

    @Test
    void acceptsBooleanWrapperConditionAndEmitsBooleanEquals() {
        FieldMeta enabled = field("enabled", "java.lang.Boolean", false, false, true, false);
        ConditionSpec condition = new ConditionSpec("enabled", ConditionOperator.NE, "false");

        assertTrue(validator.validate(condition, dto(enabled), "rule-1").isEmpty());
        assertEquals("!java.lang.Boolean.FALSE.equals(dto.getEnabled())", emitter.emit("dto.getEnabled()", enabled, condition));
    }

    @Test
    void reportsMissingConditionField() {
        ConditionSpec condition = new ConditionSpec("status", ConditionOperator.EQ, "ACTIVE");

        var diagnostics = validator.validate(condition, dto(), "rule-1");

        assertEquals(ConditionValidator.FIELD_MISSING, diagnostics.get(0).code());
        assertEquals("status", diagnostics.get(0).fieldName());
    }

    @Test
    void reportsUnsupportedEnumConditionLiteral() {
        FieldMeta status = new FieldMeta("status", TypeRef.of("com.example.Status", "Status"), false, false, false, false, true, false, "getStatus");
        ConditionSpec condition = new ConditionSpec("status", ConditionOperator.EQ, "ACTIVE");

        var diagnostics = validator.validate(condition, dto(status), "rule-1");

        assertEquals(List.of(ConditionValidator.OPERATOR_NOT_ALLOWED, ConditionValidator.LITERAL_NOT_SUPPORTED), diagnostics.stream().map(ValidationDiagnostic::code).toList());
    }

    @Test
    void reportsInvalidBooleanLiteral() {
        FieldMeta enabled = field("enabled", "java.lang.Boolean", false, false, true, false);
        ConditionSpec condition = new ConditionSpec("enabled", ConditionOperator.EQ, "yes");

        var diagnostics = validator.validate(condition, dto(enabled), "rule-1");

        assertEquals(ConditionValidator.LITERAL_NOT_SUPPORTED, diagnostics.get(0).code());
    }

    private static DtoSpec dto(FieldMeta... fields) {
        return new DtoSpec("com.example", "OrderDto", Set.of(), List.of(fields));
    }

    private static FieldMeta field(String name, String type, boolean primitive, boolean stringLike, boolean booleanLike, boolean numericLike) {
        return new FieldMeta(name, TypeRef.of(type, type), primitive, stringLike, booleanLike, numericLike, false, !primitive, "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
    }
}