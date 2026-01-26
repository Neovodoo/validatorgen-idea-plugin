package com.vkr.validatorgen.infrastructure;

import com.vkr.validatorgen.domain.CompareRule;
import com.vkr.validatorgen.domain.RuleRepository;

import java.util.ArrayList;
import java.util.List;

public final class InMemoryRuleRepository implements RuleRepository {
    private final List<CompareRule> rules = new ArrayList<>();

    @Override
    public List<CompareRule> all() {
        return List.copyOf(rules);
    }

    @Override
    public void add(CompareRule rule) {
        rules.add(rule);
    }

    @Override
    public void removeAt(int index) {
        if (index >= 0 && index < rules.size()) rules.remove(index);
    }

    @Override
    public void updateAt(int index, CompareRule rule) {
        if (index >= 0 && index < rules.size()) rules.set(index, rule);
    }

    @Override
    public void clear() {
        rules.clear();
    }
}
