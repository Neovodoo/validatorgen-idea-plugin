package com.vkr.validatorgen.prototype.model;

import java.util.List;

public record DtoDescriptor(String packageName, String className, List<String> fields) {
    public String displayName() {
        return packageName + "." + className;
    }

    @Override
    public String toString() {
        return displayName();
    }
}
