package com.vkr.validatorgen.domain;

import java.util.Objects;

public final class CompareRule {
    private final String left;
    private final CompareOp op;
    private final String right;
    private final String target;
    private final String message;

    public CompareRule(String left, CompareOp op, String right, String target, String message) {
        this.left = Objects.requireNonNull(left);
        this.op = Objects.requireNonNull(op);
        this.right = Objects.requireNonNull(right);
        this.target = Objects.requireNonNull(target);
        this.message = Objects.requireNonNull(message);
    }

    public String getLeft() { return left; }
    public CompareOp getOp() { return op; }
    public String getRight() { return right; }
    public String getTarget() { return target; }
    public String getMessage() { return message; }
}
