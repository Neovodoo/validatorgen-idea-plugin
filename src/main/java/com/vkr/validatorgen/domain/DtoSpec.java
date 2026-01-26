package com.vkr.validatorgen.domain;

import java.util.List;
import java.util.Set;

public final class DtoSpec {
    private final String packageName;
    private final String className;
    private final Set<String> getterNames;
    private final List<String> intFields;

    public DtoSpec(String packageName, String className, Set<String> getterNames, List<String> intFields) {
        this.packageName = packageName == null ? "" : packageName;
        this.className = className;
        this.getterNames = getterNames;
        this.intFields = intFields;
    }

    public String getPackageName() { return packageName; }
    public String getClassName() { return className; }
    public Set<String> getGetterNames() { return getterNames; }
    public List<String> getIntFields() { return intFields; }
}
