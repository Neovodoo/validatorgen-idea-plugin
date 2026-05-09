package com.vkr.validatorgen.infrastructure;

import com.vkr.validatorgen.domain.CodeGenerator;
import com.vkr.validatorgen.domain.DtoSpec;
import com.vkr.validatorgen.domain.RuleSpec;


import java.util.List;
import java.util.Objects;

public final class JavaValidatorGenerator implements CodeGenerator {

    private final RuleCodeEmitterRegistry emitterRegistry;

    public JavaValidatorGenerator() {
        this(RuleCodeEmitterRegistry.defaults());
    }

    public JavaValidatorGenerator(RuleCodeEmitterRegistry emitterRegistry) {
        this.emitterRegistry = Objects.requireNonNull(emitterRegistry);
    }

    @Override
    public String generate(DtoSpec dto, List<RuleSpec> rules) {
        String outPackage = dto.getPackageName().isBlank()
                ? "generated"
                : dto.getPackageName() + ".generated";

        String validatorClass = dto.getClassName() + "GeneratedValidator";
        String dtoClass = dto.getClassName();
        String dtoFqn = dto.getPackageName().isBlank()
                ? dto.getClassName()
                : dto.getPackageName() + "." + dto.getClassName();

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(outPackage).append(";\n\n")
                .append("import java.util.ArrayList;\n")
                .append("import java.util.List;\n")
                .append("import ").append(dtoFqn).append(";\n\n")
                .append("public final class ").append(validatorClass).append(" {\n")
                .append("  private ").append(validatorClass).append("() {}\n\n")
                .append("  public static List<Violation> validate(").append(dtoClass).append(" dto) {\n")
                .append("    List<Violation> violations = new ArrayList<>();\n");

        JavaRuleCodeContext context = new JavaRuleCodeContext(dto, "dto");
        for (int i = 0; i < rules.size(); i++) {
            RuleSpec rule = rules.get(i);
            RuleCode ruleCode = emitterRegistry.emit(dto, rule, i, context);
            appendRuleViolation(sb, rule, ruleCode);
        }

        sb.append("\n")
                .append("    return violations;\n")
                .append("  }\n\n")
                .append("  public record Violation(String path, String message, String ruleId) {}\n")
                .append("}\n");

        return sb.toString();
    }

    private void appendRuleViolation(StringBuilder sb, RuleSpec rule, RuleCode ruleCode) {
        sb.append("\n")
                .append("    // Rule ").append(ruleCode.ruleId()).append(": ").append(ruleCode.comment()).append("\n")
                .append("    if (!(").append(ruleCode.conditionExpression()).append(")) {\n")
                .append("      violations.add(new Violation(\"").append(escapeJava(rule.getViolationTarget())).append("\", \"")
                .append(escapeJava(rule.getMessage())).append("\", \"").append(escapeJava(ruleCode.ruleId())).append("\"));\n")
                .append("    }\n");
    }

    private String escapeJava(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
