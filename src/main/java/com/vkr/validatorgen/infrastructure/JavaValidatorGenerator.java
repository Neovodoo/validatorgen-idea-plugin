package com.vkr.validatorgen.infrastructure;

import com.vkr.validatorgen.domain.CodeGenerator;
import com.vkr.validatorgen.domain.CompareRule;
import com.vkr.validatorgen.domain.DtoSpec;

import java.util.List;

public final class JavaValidatorGenerator implements CodeGenerator {

    @Override
    public String generate(DtoSpec dto, List<CompareRule> rules) {
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

        for (int i = 0; i < rules.size(); i++) {
            CompareRule r = rules.get(i);

            String leftExpr = accessor(dto, r.getLeft());
            String rightExpr = accessor(dto, r.getRight());
            String rid = ruleId(i, r);
            String msgEsc = escapeJava(r.getMessage());
            String pathEsc = escapeJava(r.getTarget());

            sb.append("\n")
                    .append("    // Rule ").append(rid).append(": ")
                    .append(r.getLeft()).append(" ").append(r.getOp().getSymbol()).append(" ").append(r.getRight()).append("\n")
                    .append("    if (!(").append(leftExpr).append(" ").append(r.getOp().getSymbol()).append(" ").append(rightExpr).append(")) {\n")
                    .append("      violations.add(new Violation(\"").append(pathEsc).append("\", \"").append(msgEsc).append("\", \"").append(rid).append("\"));\n")
                    .append("    }\n");
        }

        sb.append("\n")
                .append("    return violations;\n")
                .append("  }\n\n")
                .append("  public record Violation(String path, String message, String ruleId) {}\n")
                .append("}\n");

        return sb.toString();
    }

    private String accessor(DtoSpec dto, String fieldName) {
        String getter = "get" + capitalize(fieldName);
        if (dto.getGetterNames().contains(getter)) return "dto." + getter + "()";
        return "dto." + fieldName;
    }

    private String ruleId(int index, CompareRule r) {
        String opCode = switch (r.getOp()) {
            case GT -> "GT";
        };
        return opCode + "_" + r.getLeft() + "_" + r.getRight() + "_" + (index + 1);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String escapeJava(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
