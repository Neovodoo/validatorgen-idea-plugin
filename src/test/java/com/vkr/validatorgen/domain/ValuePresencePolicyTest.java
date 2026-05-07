package com.vkr.validatorgen.domain;

import com.vkr.validatorgen.infrastructure.JavaPresenceCodeEmitter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValuePresencePolicyTest {
    private final ValuePresencePolicy policy = new ValuePresencePolicy();
    private final JavaPresenceCodeEmitter emitter = new JavaPresenceCodeEmitter(policy);

    @Test
    void treatsStringAsPresentWhenNonBlank() {
        FieldMeta field = field("name", "java.lang.String", false, true, false, false);

        assertTrue(policy.supportsPresenceRules(field));
        assertEquals("dto.getName() != null && !dto.getName().isBlank()", emitter.isPresent("dto.getName()", field));
        assertEquals("dto.getName() == null || dto.getName().isBlank()", emitter.isAbsent("dto.getName()", field));
    }

    @Test
    void treatsIntegerReferenceAsPresentWhenNonNull() {
        FieldMeta field = field("count", "java.lang.Integer", false, false, false, true);

        assertTrue(policy.supportsPresenceRules(field));
        assertEquals("dto.getCount() != null", emitter.isPresent("dto.getCount()", field));
        assertEquals("dto.getCount() == null", emitter.isAbsent("dto.getCount()", field));
    }

    @Test
    void treatsBooleanReferenceAsPresentWhenNonNull() {
        FieldMeta field = field("enabled", "java.lang.Boolean", false, false, true, false);

        assertTrue(policy.supportsPresenceRules(field));
        assertEquals("dto.getEnabled() != null", emitter.isPresent("dto.getEnabled()", field));
        assertEquals("dto.getEnabled() == null", emitter.isAbsent("dto.getEnabled()", field));
    }

    @Test
    void rejectsPrimitiveIntForPresenceRules() {
        FieldMeta field = field("amount", "int", true, false, false, true);

        assertFalse(policy.supportsPresenceRules(field));
        var diagnostic = policy.validateTargetField(field, "rule-1").orElseThrow();
        assertEquals(ValuePresencePolicy.PRIMITIVE_FIELD_NOT_SUPPORTED, diagnostic.code());
        assertEquals("amount", diagnostic.fieldName());
        assertThrows(IllegalArgumentException.class, () -> emitter.isPresent("dto.getAmount()", field));
    }

    private static FieldMeta field(String name, String type, boolean primitive, boolean stringLike, boolean booleanLike, boolean numericLike) {
        return new FieldMeta(name, TypeRef.of(type, type), primitive, stringLike, booleanLike, numericLike, false, !primitive, "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
    }
}