package com.vkr.validatorgen.infrastructure;

import com.vkr.validatorgen.domain.DtoSpec;
import com.vkr.validatorgen.domain.FieldMeta;

public final class JavaRuleCodeContext {
    private final DtoSpec dto;
    private final String dtoVariableName;

    public JavaRuleCodeContext(DtoSpec dto, String dtoVariableName) {
        this.dto = dto;
        this.dtoVariableName = dtoVariableName == null || dtoVariableName.isBlank() ? "dto" : dtoVariableName;
    }

    public String accessor(String fieldName) {
        FieldMeta field = dto.getField(fieldName);
        if (field != null) return field.accessorExpression(dtoVariableName);
        String getter = "get" + capitalize(fieldName);
        if (dto.getGetterNames().contains(getter)) return dtoVariableName + "." + getter + "()";
        return dtoVariableName + "." + fieldName;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}