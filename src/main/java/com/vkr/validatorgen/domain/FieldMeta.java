package com.vkr.validatorgen.domain;

public record FieldMeta(
        String name,
        TypeRef javaType,
        boolean isPrimitive,
        boolean isStringLike,
        boolean isBooleanLike,
        boolean isNumericLike,
        boolean isEnumLike,
        boolean isReferenceLike,
        String accessorName
) {
    public FieldMeta {
        name = name == null ? "" : name;
        javaType = javaType == null ? TypeRef.of("", "") : javaType;
        accessorName = accessorName == null ? "" : accessorName;
    }

    public String accessorExpression(String target) {
        if (accessorName.isBlank()) return target + "." + name;
        return target + "." + accessorName + "()";
    }
}