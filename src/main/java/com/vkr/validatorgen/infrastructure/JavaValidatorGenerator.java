package com.vkr.validatorgen.infrastructure;

import com.vkr.validatorgen.domain.*;

import java.util.List;

public final class JavaValidatorGenerator implements CodeGenerator {

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

        for (int i = 0; i < rules.size(); i++) {
            if (!(rules.get(i) instanceof CompareFieldsRule r)) {
                throw new IllegalArgumentException("Unsupported rule kind for Java generation: " + rules.get(i).getKind());
            }

            String leftExpr = accessor(dto, r.getLeft());
            String rightExpr = accessor(dto, r.getRight());
            String conditionExpr = condition(dto, r, leftExpr, rightExpr);
            String rid = ruleId(i, r);
            String msgEsc = escapeJava(r.getMessage());
            String pathEsc = escapeJava(r.getViolationTarget());

            sb.append("\n")
                    .append("    // Rule ").append(rid).append(": ")
                    .append(r.getLeft()).append(" ").append(r.getOp().getSymbol()).append(" ").append(r.getRight()).append("\n")
                    .append("    if (!(").append(conditionExpr).append(")) {\n")
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
        FieldMeta field = dto.getField(fieldName);
        if (field != null) return field.accessorExpression("dto");
        String getter = "get" + capitalize(fieldName);
        if (dto.getGetterNames().contains(getter)) return "dto." + getter + "()";
        return "dto." + fieldName;
    }

    private String ruleId(int index, CompareFieldsRule r) {
        String opCode = switch (r.getOp()) {
            case EQ -> "EQ";
            case GT -> "GT";
            case LT -> "LT";
            case GE -> "GE";
            case LE -> "LE";
            case NE -> "NE";
        };
        return opCode + "_" + r.getLeft() + "_" + r.getRight() + "_" + (index + 1);
    }


    private String condition(DtoSpec dto, CompareFieldsRule rule, String leftExpr, String rightExpr) {
        FieldMeta left = dto.getField(rule.getLeft());
        if (left != null && left.isStringLike()) {
            return switch (rule.getOp()) {
                case EQ -> "java.util.Objects.equals(" + leftExpr + ", " + rightExpr + ")";
                case NE -> "!java.util.Objects.equals(" + leftExpr + ", " + rightExpr + ")";
                case GT, LT, GE, LE -> leftExpr + " != null && " + rightExpr + " != null && " + leftExpr + ".compareTo(" + rightExpr + ") " + rule.getOp().getSymbol() + " 0";
            };
        }
        return leftExpr + " " + rule.getOp().getSymbol() + " " + rightExpr;
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
