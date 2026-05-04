package com.vkr.validatorgen.domain;

import java.util.List;
import java.util.Objects;

public final class CompareFieldsRule implements RuleSpec {
    private final String id;
    private final String left;
    private final CompareOp op;
    private final String right;
    private final String violationTarget;
    private final String message;

    public CompareFieldsRule(String id, String left, CompareOp op, String right, String violationTarget, String message) {
        this.id = Objects.requireNonNull(id);
        this.left = Objects.requireNonNull(left);
        this.op = Objects.requireNonNull(op);
        this.right = Objects.requireNonNull(right);
        this.violationTarget = Objects.requireNonNull(violationTarget);
        this.message = Objects.requireNonNull(message);
    }

    public String getLeft() { return left; }
    public CompareOp getOp() { return op; }
    public String getRight() { return right; }

    @Override
    public String getId() { return id; }

    @Override
    public RuleKind getKind() { return RuleKind.COMPARE_FIELDS; }

    @Override
    public String getViolationTarget() { return violationTarget; }

    @Override
    public String getMessage() { return message; }

    @Override
    public List<String> getInvolvedFields() {
        return List.of(left, right);
    }
}