package com.vkr.validatorgen.application;

import com.vkr.validatorgen.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RefreshFieldsUseCaseTest {
    @Test
    void returnsAllFieldsWithoutHardcodedTypeFilter() {
        DtoParser parser = ignored -> new DtoSpec(
                "com.example",
                "OrderDto",
                Set.of(),
                List.of(
                        field("name", "java.lang.String", "String", true, false, false, false),
                        field("amount", "int", "int", false, false, true, false),
                        field("boxed", "java.lang.Integer", "Integer", false, false, true, false),
                        field("active", "boolean", "boolean", false, true, false, false),
                        field("enabled", "java.lang.Boolean", "Boolean", false, true, false, false),
                        field("price", "java.math.BigDecimal", "BigDecimal", false, false, true, false),
                        field("count", "java.lang.Long", "Long", false, false, true, false),
                        field("ratio", "java.lang.Double", "Double", false, false, true, false),
                        field("status", "com.example.Status", "Status", false, false, false, true),
                        field("customer", "com.example.Customer", "Customer", false, false, false, false)
                )
        );

        var result = new RefreshFieldsUseCase(parser).execute("dto");

        assertInstanceOf(RefreshFieldsUseCase.Result.Success.class, result);
        var success = (RefreshFieldsUseCase.Result.Success) result;
        assertEquals(10, success.fields().size());
        assertTrue(success.fieldsByType().containsKey("String"));
        assertTrue(success.fieldsByType().containsKey("int"));
        assertTrue(success.fieldsByType().containsKey("Integer"));
        assertTrue(success.fieldsByType().containsKey("boolean"));
        assertTrue(success.fieldsByType().containsKey("Boolean"));
        assertTrue(success.fieldsByType().containsKey("BigDecimal"));
        assertTrue(success.fieldsByType().containsKey("Long"));
        assertTrue(success.fieldsByType().containsKey("Double"));
        assertTrue(success.fieldsByType().containsKey("Status"));
        assertTrue(success.fieldsByType().containsKey("Customer"));
    }

    private static FieldMeta field(String name, String canonical, String presentable, boolean str, boolean bool, boolean num, boolean ref) {
        return new FieldMeta(name, TypeRef.of(canonical, presentable), false, str, bool, num, false, ref, "");
    }
}