package com.vkr.validatorgen.prototype.application;

import com.vkr.validatorgen.prototype.model.GenerationResult;
import com.vkr.validatorgen.prototype.model.PreviewDocument;

public interface OutputPreviewService {
    PreviewDocument buildPreview(GenerationResult generationResult);
}
