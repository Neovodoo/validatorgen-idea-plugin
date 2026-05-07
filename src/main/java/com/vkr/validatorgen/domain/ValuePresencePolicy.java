package com.vkr.validatorgen.domain;

import com.vkr.validatorgen.domain.validation.ValidationDiagnostic;

import java.util.Optional;

/**
 * Defines whether a field type can participate in presence-based rules.
 *
 * Поля примтивных типов данных всегда имеют дефолтные значения JVM, поэтому без знания политики определения
 * дефолтных значений не имеет смысла реализовывать для них поддержку в текущем прототипе
 *
 */
public final class ValuePresencePolicy {
    public static final String PRIMITIVE_FIELD_NOT_SUPPORTED = "PRESENCE.PRIMITIVE_FIELD_NOT_SUPPORTED";

    public boolean supportsPresenceRules(FieldMeta field) {
        return field != null && !field.isPrimitive();
    }

    public Optional<ValidationDiagnostic> validateTargetField(FieldMeta field, String ruleId) {
        if (field == null || supportsPresenceRules(field)) {
            return Optional.empty();
        }
        return Optional.of(ValidationDiagnostic.error(
                PRIMITIVE_FIELD_NOT_SUPPORTED,
                "Primitive field " + field.name() + " cannot be used in presence rules without an explicit default-value policy.",
                ruleId,
                field.name()
        ));
    }

    public boolean usesBlankStringPolicy(FieldMeta field) {
        return field != null && field.isStringLike();
    }
}