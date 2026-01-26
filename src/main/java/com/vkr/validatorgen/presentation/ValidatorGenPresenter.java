package com.vkr.validatorgen.presentation;

import com.vkr.validatorgen.application.AddRuleUseCase;
import com.vkr.validatorgen.application.GenerateCodeUseCase;
import com.vkr.validatorgen.application.RefreshFieldsUseCase;
import com.vkr.validatorgen.application.RemoveRuleUseCase;

public final class ValidatorGenPresenter {

    private final ValidatorGenView view;
    private final RefreshFieldsUseCase refreshFields;
    private final AddRuleUseCase addRule;
    private final RemoveRuleUseCase removeRule;
    private final GenerateCodeUseCase generateCode;

    public ValidatorGenPresenter(
            ValidatorGenView view,
            RefreshFieldsUseCase refreshFields,
            AddRuleUseCase addRule,
            RemoveRuleUseCase removeRule,
            GenerateCodeUseCase generateCode
    ) {
        this.view = view;
        this.refreshFields = refreshFields;
        this.addRule = addRule;
        this.removeRule = removeRule;
        this.generateCode = generateCode;
    }

    public void onRefreshFields() {
        var res = refreshFields.execute(view.getDtoText());
        if (res instanceof RefreshFieldsUseCase.Result.Success s) {
            view.showFields(s.fields());
            view.showOutput("Found int fields: " + String.join(", ", s.fields()));
        } else if (res instanceof RefreshFieldsUseCase.Result.Error e) {
            view.showOutput(e.message());
        }
    }

    public void onAddRule() {
        var res = addRule.execute(view.getRuleDraft());
        if (res instanceof AddRuleUseCase.Result.Success s) {
            view.refreshRulesTable();
            view.showOutput(s.message());
        } else if (res instanceof AddRuleUseCase.Result.Error e) {
            view.showOutput(e.message());
        }
    }

    public void onRemoveRule() {
        var res = removeRule.execute(view.getSelectedRuleIndex());
        if (res instanceof RemoveRuleUseCase.Result.Success s) {
            view.refreshRulesTable();
            view.showOutput(s.message());
        } else if (res instanceof RemoveRuleUseCase.Result.Error e) {
            view.showOutput(e.message());
        }
    }

    public void onGenerateCode() {
        var res = generateCode.execute(view.getDtoText());
        if (res instanceof GenerateCodeUseCase.Result.Success s) {
            view.showGeneratedCode(s.code());
            view.showOutput("Generated " + s.dtoClassName() + "GeneratedValidator with " + s.rulesCount() + " rule(s).");
        } else if (res instanceof GenerateCodeUseCase.Result.Error e) {
            view.showOutput(e.message());
        }
    }
}
