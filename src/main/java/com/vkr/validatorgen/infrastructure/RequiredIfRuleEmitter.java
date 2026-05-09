package com.vkr.validatorgen.infrastructure;

import com.vkr.validatorgen.domain.DtoSpec;
import com.vkr.validatorgen.domain.FieldMeta;
import com.vkr.validatorgen.domain.RequiredIfRule;
import com.vkr.validatorgen.domain.RuleKind;

public final class RequiredIfRuleEmitter implements RuleCodeEmitter<RequiredIfRule> {
    private final ConditionCodeEmitter conditionEmitter;
    private final JavaPresenceCodeEmitter presenceEmitter;

    public RequiredIfRuleEmitter() {
        this(new ConditionCodeEmitter(), new JavaPresenceCodeEmitter());
    }

    public RequiredIfRuleEmitter(ConditionCodeEmitter conditionEmitter, JavaPresenceCodeEmitter presenceEmitter) {
        this.conditionEmitter = conditionEmitter == null ? new ConditionCodeEmitter() : conditionEmitter;
        this.presenceEmitter = presenceEmitter == null ? new JavaPresenceCodeEmitter() : presenceEmitter;
    }

    @Override
    public RuleKind getKind() {
        return RuleKind.REQUIRED_IF;
    }

    @Override
    public Class<RequiredIfRule> getRuleType() {
        return RequiredIfRule.class;
    }

    @Override
    public RuleCode emit(DtoSpec dto, RequiredIfRule rule, int ruleIndex, JavaRuleCodeContext context) {
        FieldMeta conditionField = dto.getField(rule.getCondition().fieldName());
        FieldMeta targetField = dto.getField(rule.getTargetField());
        String conditionExpr = conditionEmitter.emit(context.accessor(rule.getCondition().fieldName()), conditionField, rule.getCondition());
        String isPresentExpr = presenceEmitter.isPresent(context.accessor(rule.getTargetField()), targetField);
        String validExpr = "!(" + conditionExpr + ") || (" + isPresentExpr + ")";
        String comment = rule.getTargetField() + " required if " + rule.getCondition().fieldName()
                + " " + rule.getCondition().operator().getSymbol() + " " + rule.getCondition().rawLiteral();
        return new RuleCode(validExpr, ruleId(ruleIndex, rule), comment);
    }

    private String ruleId(int index, RequiredIfRule rule) {
        return "REQUIRED_IF_" + rule.getTargetField() + "_" + rule.getCondition().fieldName() + "_" + (index + 1);
    }
}