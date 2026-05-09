package com.vkr.validatorgen.domain.validation;

import com.vkr.validatorgen.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequiredIfRuleValidatorTest {
    private final RequiredIfRuleValidator validator = new RequiredIfRuleValidator();

    @Test
    void acceptsKnownCompatibleConditionAndReferenceTarget() {
        var rule = rule("paymentMethod", ConditionOperator.EQ, "CARD", "cardNumber");

        var diagnostics = validator.validate(rule, dto(stringField("paymentMethod"), stringField("cardNumber")));

        assertEquals(List.of(), diagnostics);
    }

    @Test
    void rejectsPrimitiveTargetFieldBeforeGeneration() {
        var rule = rule("paymentMethod", ConditionOperator.EQ, "CARD", "attempts");

        var diagnostics = validator.validate(rule, dto(stringField("paymentMethod"), intField("attempts")));

        assertEquals(ValuePresencePolicy.PRIMITIVE_FIELD_NOT_SUPPORTED, diagnostics.get(0).code());
        assertEquals("attempts", diagnostics.get(0).fieldName());
    }

    @Test
    void rejectsUnknownConditionField() {
        var rule = rule("missing", ConditionOperator.EQ, "CARD", "cardNumber");

        var diagnostics = validator.validate(rule, dto(stringField("paymentMethod"), stringField("cardNumber")));

        assertEquals(ConditionValidator.FIELD_MISSING, diagnostics.get(0).code());
        assertEquals("missing", diagnostics.get(0).fieldName());
    }

    @Test
    void rejectsUnknownTargetField() {
        var rule = rule("paymentMethod", ConditionOperator.EQ, "CARD", "missingTarget");

        var diagnostics = validator.validate(rule, dto(stringField("paymentMethod")));

        assertEquals(RequiredIfRuleValidator.TARGET_FIELD_MISSING, diagnostics.get(0).code());
        assertEquals("missingTarget", diagnostics.get(0).fieldName());
    }

    @Test
    void rejectsConditionLiteralThatDoesNotMatchConditionFieldType() {
        var rule = rule("hasDiscount", ConditionOperator.EQ, "yes", "discountCode");

        var diagnostics = validator.validate(rule, dto(booleanField("hasDiscount"), stringField("discountCode")));

        assertEquals(ConditionValidator.LITERAL_NOT_SUPPORTED, diagnostics.get(0).code());
    }

    private static RequiredIfRule rule(String conditionField, ConditionOperator operator, String literal, String targetField) {
        return new RequiredIfRule("rule-1", new ConditionSpec(conditionField, operator, literal), targetField, targetField, "Required");
    }

    private static DtoSpec dto(FieldMeta... fields) {
        return new DtoSpec("com.example", "OrderDto", Set.of(), List.of(fields));
    }

    private static FieldMeta stringField(String name) {
        return new FieldMeta(name, TypeRef.of("java.lang.String", "String"), false, true, false, false, false, true, accessor(name));
    }

    private static FieldMeta booleanField(String name) {
        return new FieldMeta(name, TypeRef.of("java.lang.Boolean", "Boolean"), false, false, true, false, false, true, accessor(name));
    }

    private static FieldMeta intField(String name) {
        return new FieldMeta(name, TypeRef.of("int", "int"), true, false, false, true, false, false, accessor(name));
    }

    private static String accessor(String name) {
        return "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}