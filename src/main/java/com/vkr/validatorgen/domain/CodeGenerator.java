package com.vkr.validatorgen.domain;

import java.util.List;

public interface CodeGenerator {
    String generate(DtoSpec dto, List<CompareRule> rules);
}
