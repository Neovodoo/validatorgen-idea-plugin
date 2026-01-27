package com.vkr.validatorgen.application;

import com.vkr.validatorgen.domain.ClipboardService;

public final class CopyGeneratedCodeUseCase {

    private final ClipboardService clipboard;

    public CopyGeneratedCodeUseCase(ClipboardService clipboard) {
        this.clipboard = clipboard;
    }

    public Result execute(String code) {
        if (code == null || code.isBlank()) {
            return Result.error("Nothing to copy. Generate code first.");
        }
        clipboard.copy(code);
        return Result.success("Generated code copied to clipboard.");
    }

    public sealed interface Result permits Result.Success, Result.Error {
        record Success(String message) implements Result {}
        record Error(String message) implements Result {}
        static Success success(String message) { return new Success(message); }
        static Error error(String message) { return new Error(message); }
    }
}
