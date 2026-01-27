package com.vkr.validatorgen.application;

import com.vkr.validatorgen.domain.DtoParser;
import com.vkr.validatorgen.domain.DtoSpec;
import com.vkr.validatorgen.domain.GeneratedCodeSaver;

public final class SaveGeneratedCodeUseCase {

    private final DtoParser parser;
    private final GeneratedCodeSaver saver;

    public SaveGeneratedCodeUseCase(DtoParser parser, GeneratedCodeSaver saver) {
        this.parser = parser;
        this.saver = saver;
    }

    public Result execute(String dtoText, String code) {
        if (code == null || code.isBlank()) {
            return Result.error("Nothing to save. Generate code first.");
        }

        DtoSpec dto = parser.parse(dtoText);
        if (dto == null) {
            return Result.error("Could not parse DTO info. Cannot compute output path.");
        }

        try {
            var path = saver.save(dto, code);
            return Result.success("Saved: " + path);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    public sealed interface Result permits Result.Success, Result.Error {
        record Success(String message) implements Result {}
        record Error(String message) implements Result {}
        static Success success(String message) { return new Success(message); }
        static Error error(String message) { return new Error(message); }
    }
}
