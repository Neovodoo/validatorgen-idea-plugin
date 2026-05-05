package com.vkr.validatorgen.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldMetaTest {
    @Test
    void keepsTypedCapabilities() {
        FieldMeta meta = new FieldMeta("status", TypeRef.of("com.example.Status", "Status"), false, false, false, false, true, false, "getStatus");
        assertTrue(meta.isEnumLike());
        assertFalse(meta.isReferenceLike());
        assertEquals("dto.getStatus()", meta.accessorExpression("dto"));
    }

    @Test
    void fallsBackToFieldAccessWhenAccessorMissing() {
        FieldMeta meta = new FieldMeta("customer", TypeRef.of("com.example.Customer", "Customer"), false, false, false, false, false, true, "");
        assertTrue(meta.isReferenceLike());
        assertEquals("dto.customer", meta.accessorExpression("dto"));
    }
}