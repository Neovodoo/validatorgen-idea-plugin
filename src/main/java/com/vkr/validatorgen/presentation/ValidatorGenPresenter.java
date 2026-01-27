package com.vkr.validatorgen.presentation;

import com.vkr.validatorgen.application.*;

public final class ValidatorGenPresenter {

    private final ValidatorGenView view;
    private final RefreshFieldsUseCase refreshFields;
    private final AddRuleUseCase addRule;
    private final RemoveRuleUseCase removeRule;
    private final GenerateCodeUseCase generateCode;

    // NEW:
    private final CopyGeneratedCodeUseCase copyGenerated;
    private final SaveGeneratedCodeUseCase saveGenerated;

    public ValidatorGenPresenter(
            ValidatorGenView view,
            RefreshFieldsUseCase refreshFields,
            AddRuleUseCase addRule,
            RemoveRuleUseCase removeRule,
            GenerateCodeUseCase generateCode,
            CopyGeneratedCodeUseCase copyGenerated,
            SaveGeneratedCodeUseCase saveGenerated
    ) {
        this.view = view;
        this.refreshFields = refreshFields;
        this.addRule = addRule;
        this.removeRule = removeRule;
        this.generateCode = generateCode;
        this.copyGenerated = copyGenerated;
        this.saveGenerated = saveGenerated;
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

    public void onAddRule(RuleDraft draft) {
        var res = addRule.execute(draft);
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

    // NEW:
    public void onCopyGenerated() {
        var res = copyGenerated.execute(view.getGeneratedCode());
        if (res instanceof CopyGeneratedCodeUseCase.Result.Success s) {
            view.showOutput(s.message());
        } else if (res instanceof CopyGeneratedCodeUseCase.Result.Error e) {
            view.showOutput(e.message());
        }
    }

    // NEW:
    public void onSaveGenerated() {
        var res = saveGenerated.execute(view.getDtoText(), view.getGeneratedCode());
        if (res instanceof SaveGeneratedCodeUseCase.Result.Success s) {
            view.showOutput(s.message());
        } else if (res instanceof SaveGeneratedCodeUseCase.Result.Error e) {
            view.showOutput(e.message());
        }
    }
}
