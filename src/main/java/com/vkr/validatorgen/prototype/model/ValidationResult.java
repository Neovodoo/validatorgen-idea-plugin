package com.vkr.validatorgen.prototype.model;

import java.util.List;

public record ValidationResult(boolean valid, List<ValidationIssue> issues) {
}
