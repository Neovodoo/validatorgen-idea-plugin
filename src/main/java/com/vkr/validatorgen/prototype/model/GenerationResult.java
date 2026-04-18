package com.vkr.validatorgen.prototype.model;

import java.time.Instant;
import java.util.List;

public record GenerationResult(String summary,
                               List<String> generatedArtifacts,
                               Instant generatedAt,
                               boolean success) {
}
