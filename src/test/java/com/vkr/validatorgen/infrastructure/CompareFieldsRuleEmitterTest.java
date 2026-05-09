package com.vkr.validatorgen.infrastructure;

import com.vkr.validatorgen.domain.CompareFieldsRule;
import com.vkr.validatorgen.domain.CompareOp;
import com.vkr.validatorgen.domain.DtoSpec;
import com.vkr.validatorgen.domain.FieldMeta;
import com.vkr.validatorgen.domain.TypeRef;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompareFieldsRuleEmitterTest {
    private final CompareFieldsRuleEmitter emitter = new CompareFieldsRuleEmitter();

    @Test
    void emitsPrimitiveIntEqualityAsInfixOperator() {
        RuleCode code = emit(CompareOp.EQ,
                field("actual", "int", true, false, false, true, false, false),
                field("expected", "int", true, false, false, true, false, false));

        assertTrue(code.conditionExpression().contains("dto.getActual() == dto.getExpected()"));
    }

    @Test
    void emitsStringEqualityWithObjectsEquals() {
        RuleCode code = emit(CompareOp.EQ,
                field("actual", "java.lang.String", false, true, false, false, false, true),
                field("expected", "java.lang.String", false, true, false, false, false, true));

        assertTrue(code.conditionExpression().contains("java.util.Objects.equals(dto.getActual(), dto.getExpected())"));
    }

    @Disabled("TODO: emitter currently emits reference equality for boxed Integer; switch to Objects.equals.")
    @Test
    void boxedIntegerEqualityDoesNotUseReferenceEquality() {
        RuleCode code = emit(CompareOp.EQ,
                field("actual", "java.lang.Integer", false, false, false, true, false, true),
                field("expected", "java.lang.Integer", false, false, false, true, false, true));

        assertFalse(code.conditionExpression().contains("dto.getActual() == dto.getExpected()"),
                "Integer equality should be value equality, not reference equality.");
        assertTrue(code.conditionExpression().contains("java.util.Objects.equals(dto.getActual(), dto.getExpected())"));
    }

    @Disabled("TODO: emitter currently emits null-unsafe unboxing for boxed numeric ordering.")
    @Test
    void boxedIntegerOrderingIsNullSafe() {
        RuleCode code = emit(CompareOp.GT,
                field("actual", "java.lang.Integer", false, false, false, true, false, true),
                field("expected", "java.lang.Integer", false, false, false, true, false, true));

        assertTrue(code.conditionExpression().contains("dto.getActual() != null")
                        && code.conditionExpression().contains("dto.getExpected() != null"),
                "Boxed numeric ordering should not rely on null-unsafe unboxing.");
    }

    private RuleCode emit(CompareOp op, FieldMeta left, FieldMeta right) {
        DtoSpec dto = new DtoSpec("com.example", "OrderDto", Set.of(left.accessorName(), right.accessorName()), List.of(left, right));
        return emitter.emit(dto, new CompareFieldsRule("rule-1", left.name(), op, right.name(), left.name(), "message"), 0,
                new JavaRuleCodeContext(dto, "dto"));
    }

    private static FieldMeta field(String name, String type, boolean primitive, boolean stringLike, boolean booleanLike,
                                   boolean numericLike, boolean enumLike, boolean referenceLike) {
        return new FieldMeta(name, TypeRef.of(type, type.substring(type.lastIndexOf('.') + 1)), primitive, stringLike,
                booleanLike, numericLike, enumLike, referenceLike,
                "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
    }
}