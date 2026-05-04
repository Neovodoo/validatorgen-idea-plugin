package com.vkr.validatorgen.infrastructure;

import com.vkr.validatorgen.domain.CompareRule;
import com.vkr.validatorgen.domain.RuleRepository;
import com.vkr.validatorgen.domain.RuleSpec;

import java.util.ArrayList;
import java.util.List;

public final class InMemoryRuleRepository implements RuleRepository {
    private final List<RuleSpec> rules = new ArrayList<>();

    @Override
    public List<RuleSpec> all() {
        return List.copyOf(rules);
    }

    @Override
    public void add(RuleSpec rule) {
        rules.add(rule);
    }

    @Override
    public void removeAt(int index) {
        if (index >= 0 && index < rules.size()) rules.remove(index);
    }

    @Override
    public void updateAt(int index, RuleSpec rule) {
        if (index >= 0 && index < rules.size()) rules.set(index, rule);
    }

    @Override
    public void clear() {
        rules.clear();
    }
}
