package com.vkr.validatorgen.domain;

import java.util.List;

public interface RuleRepository {
    List<CompareRule> all();
    void add(CompareRule rule);
    void removeAt(int index);
    void updateAt(int index, CompareRule rule);
    void clear();
}
