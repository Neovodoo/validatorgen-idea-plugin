package com.vkr.validatorgen.presentation;

import java.util.List;

public interface ValidatorGenView {
    String getDtoText();
    RuleDraft getRuleDraft();
    Integer getSelectedRuleIndex();

    void showFields(List<String> fields);
    void showOutput(String text);
    void showGeneratedCode(String code);
    void refreshRulesTable();
}
