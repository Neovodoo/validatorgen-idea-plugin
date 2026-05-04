package com.vkr.validatorgen.domain;

import java.util.List;

public interface RuleRepository {
    List<RuleSpec> all();
    void add(RuleSpec rule);
    void removeAt(int index);
    void updateAt(int index, RuleSpec rule);
    void clear();
}
