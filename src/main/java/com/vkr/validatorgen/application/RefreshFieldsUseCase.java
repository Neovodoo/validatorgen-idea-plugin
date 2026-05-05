package com.vkr.validatorgen.application;

import com.vkr.validatorgen.domain.DtoParser;
import com.vkr.validatorgen.domain.DtoSpec;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RefreshFieldsUseCase {
    private final DtoParser parser;

    public RefreshFieldsUseCase(DtoParser parser) {
        this.parser = parser;
    }

    public Result execute(String dtoText) {
        DtoSpec dto = parser.parse(dtoText);
        if (dto == null) return Result.error("Could not parse DTO class from editor text.");
        Map<String, List<String>> fieldsByType = dto.getFieldTypes().entrySet().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Map.Entry::getValue,
                        LinkedHashMap::new,
                        java.util.stream.Collectors.mapping(Map.Entry::getKey, java.util.stream.Collectors.toList())
                ));
        fieldsByType.replaceAll((k, v) -> v.stream().sorted().toList());

        List<String> fields = fieldsByType.values().stream().flatMap(List::stream).sorted().toList();
        if (fields.isEmpty()) return Result.error("No fields found in DTO.");
        return Result.success(fields, fieldsByType);
    }

    public sealed interface Result permits Result.Success, Result.Error {
        record Success(List<String> fields, Map<String, List<String>> fieldsByType) implements Result {}
        record Error(String message) implements Result {}

        static Success success(List<String> fields, Map<String, List<String>> fieldsByType) { return new Success(fields, fieldsByType); }
        static Error error(String message) { return new Error(message); }
    }
}
