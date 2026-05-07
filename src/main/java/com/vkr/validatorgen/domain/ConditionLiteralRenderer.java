package com.vkr.validatorgen.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;
import java.util.Set;

public final class ConditionLiteralRenderer {
    private static final Set<String> INTEGER_TYPES = Set.of(
            "byte", "short", "int", "long",
            "java.lang.Byte", "java.lang.Short", "java.lang.Integer", "java.lang.Long",
            "Byte", "Short", "Integer", "Long"
    );
    private static final Set<String> DECIMAL_TYPES = Set.of(
            "float", "double",
            "java.lang.Float", "java.lang.Double",
            "Float", "Double"
    );

    private ConditionLiteralRenderer() {
    }

    public static String renderLiteral(String rawLiteral, FieldMeta field) {
        String literal = rawLiteral == null ? "" : rawLiteral;
        if (field.isStringLike()) {
            return quoteJavaString(literal);
        }
        if (field.isBooleanLike()) {
            return renderBoolean(literal);
        }
        if (field.isNumericLike()) {
            return renderNumber(literal, field);
        }
        if (field.isEnumLike()) {
            throw new IllegalArgumentException("Enum condition literals are not supported yet because enum constant resolution is not available in this layer.");
        }
        throw new IllegalArgumentException("Condition literals for type " + field.javaType() + " are not supported yet.");
    }

    public static boolean isWrapperBoolean(FieldMeta field) {
        String type = normalizedType(field);
        return "java.lang.Boolean".equals(type) || "Boolean".equals(type);
    }

    public static boolean isPrimitiveBoolean(FieldMeta field) {
        return field.isPrimitive() && "boolean".equals(normalizedType(field));
    }

    public static boolean isNumericWrapper(FieldMeta field) {
        String type = normalizedType(field);
        return !field.isPrimitive() && (INTEGER_TYPES.contains(type) || DECIMAL_TYPES.contains(type));
    }

    private static String renderBoolean(String rawLiteral) {
        String normalized = rawLiteral.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(normalized) || "false".equals(normalized)) {
            return normalized;
        }
        throw new IllegalArgumentException("Boolean condition literal must be true or false.");
    }

    private static String renderNumber(String rawLiteral, FieldMeta field) {
        String normalizedType = normalizedType(field);
        String trimmed = rawLiteral.trim();
        if (INTEGER_TYPES.contains(normalizedType)) {
            return renderInteger(trimmed, normalizedType);
        }
        if (DECIMAL_TYPES.contains(normalizedType)) {
            return renderDecimal(trimmed, normalizedType);
        }
        throw new IllegalArgumentException("Numeric condition literal for type " + field.javaType() + " is not supported yet.");
    }

    private static String renderInteger(String literal, String normalizedType) {
        BigInteger value;
        try {
            value = new BigInteger(literal);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Integer condition literal is not a valid Java integer number.");
        }
        ensureIntegerRange(value, normalizedType);
        if ("long".equals(normalizedType) || "java.lang.Long".equals(normalizedType) || "Long".equals(normalizedType)) {
            return literal + "L";
        }
        if ("byte".equals(normalizedType) || "java.lang.Byte".equals(normalizedType) || "Byte".equals(normalizedType)) {
            return "(byte) " + literal;
        }
        if ("short".equals(normalizedType) || "java.lang.Short".equals(normalizedType) || "Short".equals(normalizedType)) {
            return "(short) " + literal;
        }
        return literal;
    }

    private static void ensureIntegerRange(BigInteger value, String normalizedType) {
        if (("byte".equals(normalizedType) || "java.lang.Byte".equals(normalizedType) || "Byte".equals(normalizedType))
                && (value.compareTo(BigInteger.valueOf(Byte.MIN_VALUE)) < 0 || value.compareTo(BigInteger.valueOf(Byte.MAX_VALUE)) > 0)) {
            throw new IllegalArgumentException("Integer condition literal is outside byte range.");
        }
        if (("short".equals(normalizedType) || "java.lang.Short".equals(normalizedType) || "Short".equals(normalizedType))
                && (value.compareTo(BigInteger.valueOf(Short.MIN_VALUE)) < 0 || value.compareTo(BigInteger.valueOf(Short.MAX_VALUE)) > 0)) {
            throw new IllegalArgumentException("Integer condition literal is outside short range.");
        }
        if (("int".equals(normalizedType) || "java.lang.Integer".equals(normalizedType) || "Integer".equals(normalizedType))
                && (value.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0 || value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0)) {
            throw new IllegalArgumentException("Integer condition literal is outside int range.");
        }
        if (("long".equals(normalizedType) || "java.lang.Long".equals(normalizedType) || "Long".equals(normalizedType))
                && (value.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0 || value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0)) {
            throw new IllegalArgumentException("Integer condition literal is outside long range.");
        }
    }

    private static String renderDecimal(String literal, String normalizedType) {
        try {
            new BigDecimal(literal);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Decimal condition literal is not a valid Java decimal number.");
        }
        if ("float".equals(normalizedType) || "java.lang.Float".equals(normalizedType) || "Float".equals(normalizedType)) {
            return literal + "F";
        }
        return literal + "D";
    }

    private static String quoteJavaString(String value) {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> sb.append(c);
            }
        }
        return sb.append('"').toString();
    }

    private static String normalizedType(FieldMeta field) {
        return field.javaType().normalizedName();
    }
}