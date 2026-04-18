package com.vkr.validatorgen.prototype.application;

import com.vkr.validatorgen.prototype.model.DtoDescriptor;
import com.vkr.validatorgen.prototype.model.GenerationMode;
import com.vkr.validatorgen.prototype.model.GenerationResult;
import com.vkr.validatorgen.prototype.model.RuleSpec;

import java.util.List;

public interface CodeGenerationService {
    GenerationResult generate(DtoDescriptor dto, List<RuleSpec> rules, GenerationMode mode);
}
