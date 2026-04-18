package com.vkr.validatorgen.prototype.application;

import com.vkr.validatorgen.prototype.model.DtoDescriptor;
import com.vkr.validatorgen.prototype.model.RuleSpec;
import com.vkr.validatorgen.prototype.model.RuleTemplate;

import java.util.List;

public interface RuleTemplateService {
    List<RuleTemplate> listTemplates();

    RuleSpec instantiateTemplate(RuleTemplate template, DtoDescriptor dto, int ordinal);
}
