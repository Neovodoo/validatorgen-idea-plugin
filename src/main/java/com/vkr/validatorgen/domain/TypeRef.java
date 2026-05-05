package com.vkr.validatorgen.domain;

import java.util.Objects;

public record TypeRef(String canonicalText, String presentableText) {
    public TypeRef {
        canonicalText = canonicalText == null ? "" : canonicalText;
        presentableText = presentableText == null ? canonicalText : presentableText;
    }

    public static TypeRef of(String canonicalText, String presentableText) {
        return new TypeRef(canonicalText, presentableText);
    }

    public String normalizedName() {
        return !canonicalText.isBlank() ? canonicalText : presentableText;
    }

    @Override
    public String toString() {
        return presentableText;
    }
}