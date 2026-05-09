package com.vkr.validatorgen.infrastructure;

import com.vkr.validatorgen.domain.CompareFieldsRule;
import com.vkr.validatorgen.domain.DtoSpec;
import com.vkr.validatorgen.domain.FieldMeta;
import com.vkr.validatorgen.domain.RuleKind;

public final class CompareFieldsRuleEmitter implements RuleCodeEmitter<CompareFieldsRule> {
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
        String leftExpr = context.accessor(rule.getLeft());
        String rightExpr = context.accessor(rule.getRight());
        String conditionExpr = condition(dto, rule, leftExpr, rightExpr);
        String comment = rule.getLeft() + " " + rule.getOp().getSymbol() + " " + rule.getRight();
        return new RuleCode(conditionExpr, ruleId(ruleIndex, rule), comment);
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

    private String ruleId(int index, CompareFieldsRule rule) {
        String opCode = switch (rule.getOp()) {
            case EQ -> "EQ";
            case GT -> "GT";
            case LT -> "LT";
            case GE -> "GE";
            case LE -> "LE";
            case NE -> "NE";
        };
        return opCode + "_" + rule.getLeft() + "_" + rule.getRight() + "_" + (index + 1);
    }
}