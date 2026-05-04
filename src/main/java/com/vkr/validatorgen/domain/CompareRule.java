package com.vkr.validatorgen.domain;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Deprecated
public final class CompareRule implements RuleSpec {

    private final CompareFieldsRule delegate;

    public CompareRule(String left, CompareOp op, String right, String target, String message) {
        this(UUID.randomUUID().toString(), left, op, right, target, message);
    }

    public CompareRule(String id, String left, CompareOp op, String right, String target, String message) {
        this.delegate = new CompareFieldsRule(
                Objects.requireNonNull(id),
                left,
                op,
                right,
                target,
                message
        );
    }

    public String getLeft() { return delegate.getLeft(); }
    public CompareOp getOp() { return delegate.getOp(); }
    public String getRight() { return delegate.getRight(); }
    public String getTarget() { return delegate.getViolationTarget(); }

    @Override
    public String getId() { return delegate.getId(); }

    @Override
    public RuleKind getKind() { return delegate.getKind(); }

    @Override
    public String getViolationTarget() { return delegate.getViolationTarget(); }

    @Override
    public String getMessage() { return delegate.getMessage(); }

    @Override
    public List<String> getInvolvedFields() { return delegate.getInvolvedFields(); }

    public CompareFieldsRule toCompareFieldsRule() {
        return delegate;
    }
}
