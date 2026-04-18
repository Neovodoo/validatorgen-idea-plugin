package com.vkr.validatorgen.prototype.stubs;

import com.vkr.validatorgen.prototype.application.CodeGenerationService;
import com.vkr.validatorgen.prototype.model.*;

import java.time.Instant;
import java.util.List;

public final class CodeGenerationServiceStub implements CodeGenerationService {
    @Override
    public GenerationResult generate(DtoDescriptor dto, List<RuleSpec> rules, GenerationMode mode) {
        String summary = "Stub generation finished for " + dto.displayName() + " in mode: " + mode.label() + ". Rules: " + rules.size();

        List<String> artifacts = mode == GenerationMode.JAKARTA_BEAN_VALIDATION
                ? List.of(
                dto.className() + "ConstraintMapping.java",
                dto.className() + "CompositeConstraint.java",
                dto.className() + "ValidationMessages.properties"
        )
                : List.of(
                dto.className() + "Validator.java",
                dto.className() + "ValidationReport.java"
        );

        return new GenerationResult(summary, artifacts, Instant.now(), true);
    }
}
