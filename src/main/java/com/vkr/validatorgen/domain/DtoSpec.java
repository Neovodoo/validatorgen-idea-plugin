package com.vkr.validatorgen.domain;

import java.util.*;
import java.util.stream.Collectors;

public final class DtoSpec {
    private final String packageName;
    private final String className;
    private final Set<String> getterNames;
    private final List<FieldMeta> fields;
    private final Map<String, FieldMeta> fieldsByName;

    public DtoSpec(String packageName, String className, Set<String> getterNames, List<FieldMeta> fields) {
        this.packageName = packageName == null ? "" : packageName;
        this.className = className;
        this.getterNames = getterNames == null ? Set.of() : Set.copyOf(getterNames);
        this.fields = fields == null ? List.of() : List.copyOf(fields);
        this.fieldsByName = this.fields.stream().collect(Collectors.toMap(FieldMeta::name, f -> f, (a, b) -> a, LinkedHashMap::new));
    }

    public String getPackageName() { return packageName; }
    public String getClassName() { return className; }
    public Set<String> getGetterNames() { return getterNames; }
    public List<FieldMeta> getFields() { return fields; }
    public Map<String, FieldMeta> getFieldsByName() { return fieldsByName; }

    public Map<String, String> getFieldTypes() {
        return fields.stream().collect(Collectors.toMap(FieldMeta::name, f -> f.javaType().toString(), (a, b) -> a, LinkedHashMap::new));
    }

    public FieldMeta getField(String name) {
        return fieldsByName.get(name);
    }
}
