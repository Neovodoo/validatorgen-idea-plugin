package com.vkr.validatorgen.infrastructure;

import com.vkr.validatorgen.domain.CompareFieldsRule;
import com.vkr.validatorgen.domain.CompareRule;
import com.vkr.validatorgen.domain.DtoSpec;
import com.vkr.validatorgen.domain.RuleKind;
import com.vkr.validatorgen.domain.RuleSpec;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class RuleCodeEmitterRegistry {
    private final Map<RuleKind, RuleCodeEmitter<? extends RuleSpec>> emitters;

    public RuleCodeEmitterRegistry(Collection<RuleCodeEmitter<? extends RuleSpec>> emitters) {
        this.emitters = new EnumMap<>(RuleKind.class);
        for (RuleCodeEmitter<? extends RuleSpec> emitter : emitters) {
            RuleCodeEmitter<? extends RuleSpec> previous = this.emitters.put(emitter.getKind(), emitter);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate Java rule emitter for kind: " + emitter.getKind());
            }
        }
    }

    public static RuleCodeEmitterRegistry defaults() {
        return new RuleCodeEmitterRegistry(List.of(new CompareFieldsRuleEmitter()));
    }

    public RuleCode emit(DtoSpec dto, RuleSpec rule, int ruleIndex, JavaRuleCodeContext context) {
        RuleCodeEmitter<? extends RuleSpec> emitter = emitters.get(rule.getKind());
        if (emitter == null) {
            throw new IllegalArgumentException("Unsupported rule kind for Java generation: " + rule.getKind());
        }
        return emitTyped(emitter, dto, rule, ruleIndex, context);
    }

    private <T extends RuleSpec> RuleCode emitTyped(RuleCodeEmitter<T> emitter, DtoSpec dto, RuleSpec rule, int ruleIndex, JavaRuleCodeContext context) {
        T typedRule = castRule(emitter, rule);
        return emitter.emit(dto, typedRule, ruleIndex, context);
    }

    private <T extends RuleSpec> T castRule(RuleCodeEmitter<T> emitter, RuleSpec rule) {
        if (emitter.getRuleType().isInstance(rule)) {
            return emitter.getRuleType().cast(rule);
        }
        if (rule instanceof CompareRule compareRule && emitter.getRuleType().equals(CompareFieldsRule.class)) {
            return emitter.getRuleType().cast(compareRule.toCompareFieldsRule());
        }
        throw new IllegalArgumentException("Rule " + rule.getKind() + " is not supported by emitter " + emitter.getClass().getSimpleName());
    }
}