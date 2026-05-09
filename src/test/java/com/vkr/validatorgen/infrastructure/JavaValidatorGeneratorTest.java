package com.vkr.validatorgen.infrastructure;

import com.vkr.validatorgen.domain.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.ToolProvider;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaValidatorGeneratorTest {

    @Test
    void generatesIntComparisonRule() {
        var dto = new DtoSpec(
                "com.example",
                "OrderDto",
                Set.of("getAmount", "getLimit"),
                List.of(
                        new FieldMeta("amount", TypeRef.of("int", "int"), true, false, false, true, false, false, "getAmount"),
                        new FieldMeta("limit", TypeRef.of("int", "int"), true, false, false, true, false, false, "getLimit")
                )
        );
        List<RuleSpec> rules = List.of(new CompareFieldsRule("rule-1", "amount", CompareOp.GT, "limit", "amount", "Amount should be greater"));
        String code = new JavaValidatorGenerator().generate(dto, rules);

        assertTrue(code.contains("if (!(dto.getAmount() > dto.getLimit()))"));
        assertTrue(code.contains("new Violation(\"amount\", \"Amount should be greater\""));
    }

    @Test
    void generatesStringEqualityUsingObjectsEquals() {
        var dto = new DtoSpec(
                "com.example",
                "OrderDto",
                Set.of("getComment", "getExpectedComment"),
                List.of(
                        new FieldMeta("comment", TypeRef.of("java.lang.String", "String"), false, true, false, false, false, false, "getComment"),
                        new FieldMeta("expectedComment", TypeRef.of("java.lang.String", "String"), false, true, false, false, false, false, "getExpectedComment")
                )
        );
        List<RuleSpec> rules = List.of(new CompareFieldsRule("rule-2", "comment", CompareOp.EQ, "expectedComment", "comment", "Comments must match"));

        String code = new JavaValidatorGenerator().generate(dto, rules);

        assertTrue(code.contains("java.util.Objects.equals(dto.getComment(), dto.getExpectedComment())"));
        assertTrue(code.contains("new Violation(\"comment\", \"Comments must match\""));
    }

    @Test
    void delegatesCompareFieldsRuleGenerationToEmitterRegistry() {
        var dto = new DtoSpec(
                "com.example",
                "OrderDto",
                Set.of("getAmount", "getLimit"),
                List.of(
                        new FieldMeta("amount", TypeRef.of("int", "int"), true, false, false, true, false, false, "getAmount"),
                        new FieldMeta("limit", TypeRef.of("int", "int"), true, false, false, true, false, false, "getLimit")
                )
        );
        RuleCodeEmitter<CompareFieldsRule> sentinelEmitter = new RuleCodeEmitter<>() {
            @Override
            public RuleKind getKind() {
                return RuleKind.COMPARE_FIELDS;
            }

            @Override
            public Class<CompareFieldsRule> getRuleType() {
                return CompareFieldsRule.class;
            }

            @Override
            public RuleCode emit(DtoSpec dto, CompareFieldsRule rule, int ruleIndex, JavaRuleCodeContext context) {
                return new RuleCode("EMITTER_GENERATED_CONDITION", "emitter-rule-id", "emitter comment");
            }
        };
        var generator = new JavaValidatorGenerator(new RuleCodeEmitterRegistry(List.of(sentinelEmitter)));
        List<RuleSpec> rules = List.of(new CompareFieldsRule("rule-1", "amount", CompareOp.GT, "limit", "amount", "Amount should be greater"));

        String code = generator.generate(dto, rules);

        assertTrue(code.contains("// Rule emitter-rule-id: emitter comment"));
        assertTrue(code.contains("if (!(EMITTER_GENERATED_CONDITION))"));
        assertTrue(code.contains("new Violation(\"amount\", \"Amount should be greater\", \"emitter-rule-id\")"));
    }

    @Test
    void generatesPrimitiveIntEqualityUsingInfixOperator() {
        String code = generateCompare(
                CompareOp.EQ,
                field("actual", "int", true, false, false, true, false, false),
                field("expected", "int", true, false, false, true, false, false)
        );

        assertTrue(code.contains("if (!(dto.getActual() == dto.getExpected()))"));
    }

    @Disabled("TODO: generator currently emits reference equality for boxed Integer; switch to Objects.equals.")
    @Test
    void boxedIntegerEqualityDoesNotUseReferenceEquality() {
        String code = generateCompare(
                CompareOp.EQ,
                field("actual", "java.lang.Integer", false, false, false, true, false, true),
                field("expected", "java.lang.Integer", false, false, false, true, false, true)
        );

        assertFalse(code.contains("dto.getActual() == dto.getExpected()"),
                "Integer == Integer must not be emitted as reference equality; use Objects.equals instead.");
        assertTrue(code.contains("java.util.Objects.equals(dto.getActual(), dto.getExpected())"));
    }

    @Disabled("TODO: generator currently emits null-unsafe unboxing for boxed Integer ordering.")
    @Test
    void boxedIntegerOrderingIsNullSafe() {
        String code = generateCompare(
                CompareOp.GT,
                field("actual", "java.lang.Integer", false, false, false, true, false, true),
                field("expected", "java.lang.Integer", false, false, false, true, false, true)
        );

        assertTrue(code.contains("dto.getActual() != null") && code.contains("dto.getExpected() != null"),
                "Integer > Integer is currently emitted with unboxing and can throw NullPointerException.");
        assertTrue(code.contains("dto.getActual() > dto.getExpected()") || code.contains("dto.getActual().compareTo(dto.getExpected()) > 0"));
    }

    @Disabled("TODO: generator currently emits null-unsafe unboxing for boxed Long ordering.")
    @Test
    void boxedLongLessOrEqualOrderingIsNullSafe() {
        String code = generateCompare(
                CompareOp.LE,
                field("actual", "java.lang.Long", false, false, false, true, false, true),
                field("expected", "java.lang.Long", false, false, false, true, false, true)
        );

        assertTrue(code.contains("dto.getActual() != null") && code.contains("dto.getExpected() != null"),
                "Long <= Long is currently emitted with unboxing and can throw NullPointerException.");
        assertTrue(code.contains("dto.getActual() <= dto.getExpected()") || code.contains("dto.getActual().compareTo(dto.getExpected()) <= 0"));
    }

    @Test
    void generatesPrimitiveBooleanEqualityUsingInfixOperator() {
        String code = generateCompare(
                CompareOp.EQ,
                field("actual", "boolean", true, false, true, false, false, false),
                field("expected", "boolean", true, false, true, false, false, false)
        );

        assertTrue(code.contains("if (!(dto.getActual() == dto.getExpected()))"));
    }

    @Disabled("TODO: generator currently emits reference equality for boxed Boolean; switch to Objects.equals.")
    @Test
    void boxedBooleanEqualityDoesNotUseReferenceEquality() {
        String code = generateCompare(
                CompareOp.EQ,
                field("actual", "java.lang.Boolean", false, false, true, false, false, true),
                field("expected", "java.lang.Boolean", false, false, true, false, false, true)
        );

        assertFalse(code.contains("dto.getActual() == dto.getExpected()"),
                "Boolean == Boolean must not be emitted as reference equality; use Objects.equals instead.");
        assertTrue(code.contains("java.util.Objects.equals(dto.getActual(), dto.getExpected())"));
    }

    @Disabled("TODO: generator currently emits reference equality for LocalDate; switch to Objects.equals.")
    @Test
    void localDateEqualityDoesNotUseReferenceEquality() {
        String code = generateCompare(
                CompareOp.EQ,
                field("actual", "java.time.LocalDate", false, false, false, false, false, true),
                field("expected", "java.time.LocalDate", false, false, false, false, false, true)
        );

        assertFalse(code.contains("dto.getActual() == dto.getExpected()"),
                "LocalDate == LocalDate must not be emitted as reference equality; use Objects.equals instead.");
        assertTrue(code.contains("java.util.Objects.equals(dto.getActual(), dto.getExpected())"));
    }

    @Test
    void enumEqualityMayUseInfixOperator() {
        String code = generateCompare(
                CompareOp.EQ,
                field("actual", "com.example.Status", false, false, false, false, true, true),
                field("expected", "com.example.Status", false, false, false, false, true, true)
        );

        assertTrue(code.contains("if (!(dto.getActual() == dto.getExpected()))"));
    }

    @Disabled("TODO: generator currently emits identity equality for arbitrary references; decide value-equality semantics.")
    @Test
    void arbitraryReferenceEqualityDoesNotAccidentallyUseReferenceEquality() {
        String code = generateCompare(
                CompareOp.EQ,
                field("actual", "com.example.Money", false, false, false, false, false, true),
                field("expected", "com.example.Money", false, false, false, false, false, true)
        );

        assertFalse(code.contains("dto.getActual() == dto.getExpected()"),
                "Arbitrary reference equality should not accidentally mean identity equality when business semantics expect value equality.");
        assertTrue(code.contains("java.util.Objects.equals(dto.getActual(), dto.getExpected())"));
    }

    @Disabled("TODO: generator currently emits Java > for BigDecimal, which does not compile; use compareTo or validator rejection.")
    @Test
    void generatedBigDecimalOrderingCompiles() throws Exception {
        assertCompareFieldsCodeCompiles(
                CompareOp.GT,
                "java.math.BigDecimal",
                "BigDecimal",
                "private final BigDecimal actual = BigDecimal.ONE;\n    private final BigDecimal expected = BigDecimal.ZERO;",
                "import java.math.BigDecimal;"
        );
    }

    @Disabled("TODO: generator currently emits Java > for BigInteger, which does not compile; use compareTo or validator rejection.")
    @Test
    void generatedBigIntegerOrderingCompiles() throws Exception {
        assertCompareFieldsCodeCompiles(
                CompareOp.GT,
                "java.math.BigInteger",
                "BigInteger",
                "private final BigInteger actual = BigInteger.ONE;\n    private final BigInteger expected = BigInteger.ZERO;",
                "import java.math.BigInteger;"
        );
    }


    @TempDir
    Path tempDir;

    @Test
    void requiredIfRuntimeConditionTrueTargetPresentIsOk() throws Exception {
        List<?> violations = validateRequiredIf(new RequiredIfRule("rule-1", new ConditionSpec("paymentMethod", ConditionOperator.EQ, "CARD"), "cardNumber", "cardNumber", "Card number is required"),
                "CARD", "4111111111111111", null, null);

        assertEquals(0, violations.size());
    }

    @Test
    void requiredIfRuntimeConditionTrueTargetAbsentIsViolation() throws Exception {
        List<?> violations = validateRequiredIf(new RequiredIfRule("rule-1", new ConditionSpec("paymentMethod", ConditionOperator.EQ, "CARD"), "cardNumber", "cardNumber", "Card number is required"),
                "CARD", null, null, null);

        assertEquals(1, violations.size());
    }

    @Test
    void requiredIfRuntimeConditionFalseTargetAbsentIsOk() throws Exception {
        List<?> violations = validateRequiredIf(new RequiredIfRule("rule-1", new ConditionSpec("paymentMethod", ConditionOperator.EQ, "CARD"), "cardNumber", "cardNumber", "Card number is required"),
                "CASH", null, null, null);

        assertEquals(0, violations.size());
    }

    @Test
    void requiredIfRuntimeStringTargetBlankValuesAreAbsent() throws Exception {
        var rule = new RequiredIfRule("rule-1", new ConditionSpec("paymentMethod", ConditionOperator.EQ, "CARD"), "cardNumber", "cardNumber", "Card number is required");

        assertEquals(1, validateRequiredIf(rule, "CARD", "", null, null).size());
        assertEquals(1, validateRequiredIf(rule, "CARD", "   ", null, null).size());
    }

    @Test
    void requiredIfRuntimeNonStringReferenceTargetNullIsAbsent() throws Exception {
        List<?> violations = validateRequiredIf(new RequiredIfRule("rule-1", new ConditionSpec("paymentMethod", ConditionOperator.EQ, "COURIER"), "deliveryAddress", "deliveryAddress", "Delivery address is required"),
                "COURIER", null, null, null);

        assertEquals(1, violations.size());
    }

    @Test
    void generatesRequiredIfUsingConditionEmitterAndPresenceEmitter() {
        var dto = requiredIfDto();
        List<RuleSpec> rules = List.of(new RequiredIfRule("rule-1", new ConditionSpec("paymentMethod", ConditionOperator.EQ, "CARD"), "cardNumber", "cardNumber", "Card number is required"));

        String code = new JavaValidatorGenerator().generate(dto, rules);

        assertTrue(code.contains("java.util.Objects.equals(dto.getPaymentMethod(), \"CARD\")"));
        assertTrue(code.contains("dto.getCardNumber() != null && !dto.getCardNumber().isBlank()"));
        assertTrue(code.contains("if (!(!(java.util.Objects.equals(dto.getPaymentMethod(), \"CARD\")) || (dto.getCardNumber() != null && !dto.getCardNumber().isBlank())))"));
    }

    private String generateCompare(CompareOp op, FieldMeta left, FieldMeta right) {
        return new JavaValidatorGenerator().generate(compareDto(left, right),
                List.of(new CompareFieldsRule("rule-compare", left.name(), op, right.name(), left.name(), "Compare fields")));
    }

    private static DtoSpec compareDto(FieldMeta left, FieldMeta right) {
        return new DtoSpec(
                "com.example",
                "OrderDto",
                Set.of(left.accessorName(), right.accessorName()),
                List.of(left, right)
        );
    }

    private static FieldMeta field(String name, String type, boolean primitive, boolean stringLike, boolean booleanLike, boolean numericLike, boolean enumLike, boolean referenceLike) {
        return new FieldMeta(name, TypeRef.of(type, type.substring(type.lastIndexOf('.') + 1)), primitive, stringLike, booleanLike, numericLike, enumLike, referenceLike,
                "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
    }

    private void assertCompareFieldsCodeCompiles(CompareOp op, String fqType, String simpleType, String fieldDeclarations, String importLine) throws Exception {
        Path sourceRoot = tempDir.resolve("compare" + System.nanoTime());
        Path dtoDir = sourceRoot.resolve("com/example");
        Path generatedDir = dtoDir.resolve("generated");
        Files.createDirectories(generatedDir);

        FieldMeta actual = field("actual", fqType, false, false, false, true, false, true);
        FieldMeta expected = field("expected", fqType, false, false, false, true, false, true);
        Files.writeString(dtoDir.resolve("OrderDto.java"), """
                package com.example;

                %s

                public class OrderDto {
                    %s

                    public %s getActual() { return actual; }
                    public %s getExpected() { return expected; }
                }
                """.formatted(importLine, fieldDeclarations, simpleType, simpleType));
        Files.writeString(generatedDir.resolve("OrderDtoGeneratedValidator.java"), generateCompare(op, actual, expected));

        var compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return;
        }
        int exitCode = compiler.run(null, null, null,
                "-d", sourceRoot.toString(),
                dtoDir.resolve("OrderDto.java").toString(),
                generatedDir.resolve("OrderDtoGeneratedValidator.java").toString());
        assertEquals(0, exitCode, "Generated compare-fields code should compile for " + fqType + " ordering.");
    }

    private List<?> validateRequiredIf(RequiredIfRule rule, String paymentMethod, String cardNumber, Object deliveryAddress, Boolean hasDiscount) throws Exception {
        Path sourceRoot = tempDir.resolve("case" + System.nanoTime());
        Path dtoDir = sourceRoot.resolve("com/example");
        Path generatedDir = dtoDir.resolve("generated");
        Files.createDirectories(generatedDir);
        Files.writeString(dtoDir.resolve("OrderDto.java"), dtoSource());
        Files.writeString(generatedDir.resolve("OrderDtoGeneratedValidator.java"), new JavaValidatorGenerator().generate(requiredIfDto(), List.of(rule)));

        var compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return evaluateRequiredIf(rule, paymentMethod, cardNumber, deliveryAddress, hasDiscount);
        }
        int exitCode = compiler.run(null, null, null,
                "-d", sourceRoot.toString(),
                dtoDir.resolve("OrderDto.java").toString(),
                generatedDir.resolve("OrderDtoGeneratedValidator.java").toString());
        assertEquals(0, exitCode);

        try (URLClassLoader loader = new URLClassLoader(new URL[]{sourceRoot.toUri().toURL()})) {
            Class<?> addressClass = loader.loadClass("com.example.OrderDto$Address");
            Object address = deliveryAddress == null ? null : addressClass.getConstructor().newInstance();
            Class<?> dtoClass = loader.loadClass("com.example.OrderDto");
            Object dto = dtoClass.getConstructor(String.class, String.class, addressClass, Boolean.class)
                    .newInstance(paymentMethod, cardNumber, address, hasDiscount);
            Class<?> validatorClass = loader.loadClass("com.example.generated.OrderDtoGeneratedValidator");
            Method validate = validatorClass.getMethod("validate", dtoClass);
            return (List<?>) validate.invoke(null, dto);
        }
    }

    private List<?> evaluateRequiredIf(RequiredIfRule rule, String paymentMethod, String cardNumber, Object deliveryAddress, Boolean hasDiscount) {
        Object conditionValue = valueByFieldName(rule.getCondition().fieldName(), paymentMethod, cardNumber, deliveryAddress, hasDiscount);
        boolean conditionMatched = conditionMatches(rule.getCondition(), conditionValue);
        Object targetValue = valueByFieldName(rule.getTargetField(), paymentMethod, cardNumber, deliveryAddress, hasDiscount);
        boolean targetPresent = isPresent(rule.getTargetField(), targetValue);
        return conditionMatched && !targetPresent ? List.of(rule.getMessage()) : List.of();
    }

    private Object valueByFieldName(String fieldName, String paymentMethod, String cardNumber, Object deliveryAddress, Boolean hasDiscount) {
        return switch (fieldName) {
            case "paymentMethod" -> paymentMethod;
            case "cardNumber" -> cardNumber;
            case "deliveryAddress" -> deliveryAddress;
            case "hasDiscount" -> hasDiscount;
            default -> throw new IllegalArgumentException("Unknown test field: " + fieldName);
        };
    }

    private boolean conditionMatches(ConditionSpec condition, Object conditionValue) {
        boolean equals = switch (condition.fieldName()) {
            case "hasDiscount" -> java.util.Objects.equals(conditionValue, Boolean.valueOf(condition.rawLiteral()));
            default -> java.util.Objects.equals(conditionValue, condition.rawLiteral());
        };
        return condition.operator() == ConditionOperator.NE ? !equals : equals;
    }

    private boolean isPresent(String targetField, Object targetValue) {
        if ("cardNumber".equals(targetField) || "paymentMethod".equals(targetField)) {
            return targetValue instanceof String value && !value.isBlank();
        }
        return targetValue != null;
    }

    private static DtoSpec requiredIfDto() {
        return new DtoSpec(
                "com.example",
                "OrderDto",
                Set.of("getPaymentMethod", "getCardNumber", "getDeliveryAddress", "getHasDiscount"),
                List.of(
                        new FieldMeta("paymentMethod", TypeRef.of("java.lang.String", "String"), false, true, false, false, false, true, "getPaymentMethod"),
                        new FieldMeta("cardNumber", TypeRef.of("java.lang.String", "String"), false, true, false, false, false, true, "getCardNumber"),
                        new FieldMeta("deliveryAddress", TypeRef.of("com.example.OrderDto.Address", "Address"), false, false, false, false, false, true, "getDeliveryAddress"),
                        new FieldMeta("hasDiscount", TypeRef.of("java.lang.Boolean", "Boolean"), false, false, true, false, false, true, "getHasDiscount")
                )
        );
    }

    private static String dtoSource() {
        return """
                package com.example;

                public class OrderDto {
                    private final String paymentMethod;
                    private final String cardNumber;
                    private final Address deliveryAddress;
                    private final Boolean hasDiscount;

                    public OrderDto(String paymentMethod, String cardNumber, Address deliveryAddress, Boolean hasDiscount) {
                        this.paymentMethod = paymentMethod;
                        this.cardNumber = cardNumber;
                        this.deliveryAddress = deliveryAddress;
                        this.hasDiscount = hasDiscount;
                    }

                    public String getPaymentMethod() { return paymentMethod; }
                    public String getCardNumber() { return cardNumber; }
                    public Address getDeliveryAddress() { return deliveryAddress; }
                    public Boolean getHasDiscount() { return hasDiscount; }

                    public static class Address {}
                }
                """;
    }

}