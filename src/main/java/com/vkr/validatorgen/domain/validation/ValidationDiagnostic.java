package com.vkr.validatorgen.domain.validation;

public record ValidationDiagnostic(
        String code,
        String message,
        String ruleId,
        String fieldName,
        DiagnosticSeverity severity
) {
    public ValidationDiagnostic {
        code = code == null ? "" : code;
        message = message == null ? "" : message;
        severity = severity == null ? DiagnosticSeverity.ERROR : severity;
    }

    public static ValidationDiagnostic error(String code, String message, String ruleId, String fieldName) {
        return new ValidationDiagnostic(code, message, ruleId, fieldName, DiagnosticSeverity.ERROR);
    }
}