package com.vkr.validatorgen.presentation;

import java.util.List;
import java.util.Map;

public interface ValidatorGenView {
    String getDtoText();
    Integer getSelectedRuleIndex();
    String getGeneratedCode();

    void showFields(List<String> fields);
    void showOutput(String text);
    void showGeneratedCode(String code);
    void showFieldsByType(Map<String, List<String>> fieldsByType);
    void refreshRulesTable();
}
