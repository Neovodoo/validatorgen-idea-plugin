package com.vkr.validatorgen.prototype.stubs;

import com.vkr.validatorgen.prototype.application.OutputPreviewService;
import com.vkr.validatorgen.prototype.model.GenerationResult;
import com.vkr.validatorgen.prototype.model.PreviewDocument;

public final class OutputPreviewServiceStub implements OutputPreviewService {
    @Override
    public PreviewDocument buildPreview(GenerationResult generationResult) {
        StringBuilder content = new StringBuilder();
        content.append("// Stub preview (no real generation)\n")
                .append("// Generated at: ").append(generationResult.generatedAt()).append("\n")
                .append("// Summary: ").append(generationResult.summary()).append("\n\n")
                .append("Artifacts:\n");

        for (String artifact : generationResult.generatedArtifacts()) {
            content.append(" - ").append(artifact).append("\n");
        }

        content.append("\n// Next step: connect real generation engine in application layer.");
        return new PreviewDocument("Output Preview", content.toString());
    }
}
