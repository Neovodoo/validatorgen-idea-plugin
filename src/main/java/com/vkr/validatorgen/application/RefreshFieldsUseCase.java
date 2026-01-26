package com.vkr.validatorgen.application;

import com.vkr.validatorgen.domain.DtoParser;
import com.vkr.validatorgen.domain.DtoSpec;

import java.util.List;

public final class RefreshFieldsUseCase {
    private final DtoParser parser;

    public RefreshFieldsUseCase(DtoParser parser) {
        this.parser = parser;
    }

    public Result execute(String dtoText) {
        DtoSpec dto = parser.parse(dtoText);
        if (dto == null) return Result.error("Could not parse DTO class from editor text.");
        List<String> fields = dto.getIntFields();
        if (fields.isEmpty()) return Result.error("No int fields found in DTO. Ensure fields are declared as 'int'.");
        return Result.success(fields);
    }

    public sealed interface Result permits Result.Success, Result.Error {
        record Success(List<String> fields) implements Result {}
        record Error(String message) implements Result {}

        static Success success(List<String> fields) { return new Success(fields); }
        static Error error(String message) { return new Error(message); }
    }
}
