package com.vkr.validatorgen.infrastructure;

import com.vkr.validatorgen.domain.FieldMeta;
import com.vkr.validatorgen.domain.ValuePresencePolicy;

public final class JavaPresenceCodeEmitter {
    private final ValuePresencePolicy presencePolicy;

    public JavaPresenceCodeEmitter() {
        this(new ValuePresencePolicy());
    }

    public JavaPresenceCodeEmitter(ValuePresencePolicy presencePolicy) {
        this.presencePolicy = presencePolicy == null ? new ValuePresencePolicy() : presencePolicy;
    }

    public String isPresent(String fieldAccess, FieldMeta fieldMeta) {
        ensureSupported(fieldMeta);
        if (presencePolicy.usesBlankStringPolicy(fieldMeta)) {
            return fieldAccess + " != null && !" + fieldAccess + ".isBlank()";
        }
        return fieldAccess + " != null";
    }

    public String isAbsent(String fieldAccess, FieldMeta fieldMeta) {
        ensureSupported(fieldMeta);
        if (presencePolicy.usesBlankStringPolicy(fieldMeta)) {
            return fieldAccess + " == null || " + fieldAccess + ".isBlank()";
        }
        return fieldAccess + " == null";
    }

    private void ensureSupported(FieldMeta fieldMeta) {
        if (!presencePolicy.supportsPresenceRules(fieldMeta)) {
            String fieldName = fieldMeta == null ? "<unknown>" : fieldMeta.name();
            throw new IllegalArgumentException("Presence rules do not support primitive field: " + fieldName);
        }
    }
}