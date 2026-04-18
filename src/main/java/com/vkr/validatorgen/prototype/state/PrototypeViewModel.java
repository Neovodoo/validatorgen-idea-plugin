package com.vkr.validatorgen.prototype.state;

import com.vkr.validatorgen.prototype.application.*;
import com.vkr.validatorgen.prototype.model.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

public final class PrototypeViewModel {
    public static final String STATE_PROPERTY = "prototype.state";

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private final DtoCatalogService dtoCatalogService;
    private final RuleTemplateService ruleTemplateService;
    private final SpecificationValidationService specificationValidationService;
    private final CodeGenerationService codeGenerationService;
    private final OutputPreviewService outputPreviewService;

    private final PrototypeState state = new PrototypeState();

    public PrototypeViewModel(DtoCatalogService dtoCatalogService,
                              RuleTemplateService ruleTemplateService,
                              SpecificationValidationService specificationValidationService,
                              CodeGenerationService codeGenerationService,
                              OutputPreviewService outputPreviewService) {
        this.dtoCatalogService = dtoCatalogService;
        this.ruleTemplateService = ruleTemplateService;
        this.specificationValidationService = specificationValidationService;
        this.codeGenerationService = codeGenerationService;
        this.outputPreviewService = outputPreviewService;
    }

    public PrototypeState state() {
        return state;
    }

    public void addStateListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(STATE_PROPERTY, listener);
    }

    public void initialize() {
        state.status(PrototypeStatus.LOADING);
        state.statusMessage("Loading mock data...");
        emit();

        List<DtoDescriptor> dtos = dtoCatalogService.listAvailableDtos();
        List<RuleTemplate> templates = ruleTemplateService.listTemplates();

        state.catalog(dtos);
        state.templates(templates);

        if (!dtos.isEmpty()) {
            state.selectedDto(dtos.get(0));
            state.status(PrototypeStatus.READY);
            state.statusMessage("Mock catalog loaded.");
            preloadRules(templates);
        } else {
            state.status(PrototypeStatus.EMPTY);
            state.statusMessage("No DTOs available in catalog.");
        }
        emit();
    }

    public void selectDto(DtoDescriptor dtoDescriptor) {
        if (dtoDescriptor == null || dtoDescriptor.equals(state.selectedDto())) {
            return;
        }
        state.selectedDto(dtoDescriptor);
        state.status(PrototypeStatus.READY);
        state.statusMessage("Selected DTO: " + dtoDescriptor.displayName());
        emit();
    }

    public void setMode(GenerationMode mode) {
        if (mode == null || mode == state.generationMode()) {
            return;
        }
        state.generationMode(mode);
        state.statusMessage("Generation mode switched to: " + mode.label());
        emit();
    }

    public void addTemplateRule(RuleTemplate template) {
        DtoDescriptor dto = state.selectedDto();
        if (dto == null) {
            state.status(PrototypeStatus.ERROR);
            state.statusMessage("Select DTO first.");
            emit();
            return;
        }

        RuleSpec rule = ruleTemplateService.instantiateTemplate(template, dto, state.activeRules().size() + 1);
        state.activeRules().add(rule);
        state.status(PrototypeStatus.READY);
        state.statusMessage("Added rule template: " + template.title());
        emit();
    }

    public void removeRule(int index) {
        if (index < 0 || index >= state.activeRules().size()) {
            state.statusMessage("Select rule to remove.");
            emit();
            return;
        }
        RuleSpec removed = state.activeRules().remove(index);
        state.statusMessage("Removed rule: " + removed.id());
        emit();
    }

    public void validateSpecification() {
        ValidationResult result = specificationValidationService.validate(
                state.selectedDto(),
                List.copyOf(state.activeRules()),
                state.generationMode()
        );
        state.diagnostics(result.issues());
        state.status(result.valid() ? PrototypeStatus.SUCCESS : PrototypeStatus.ERROR);
        state.statusMessage(result.valid() ? "Specification looks consistent (stub)." : "Specification has issues (stub)." );
        emit();
    }

    public void generate() {
        if (state.selectedDto() == null) {
            state.status(PrototypeStatus.ERROR);
            state.statusMessage("Choose DTO before generation.");
            emit();
            return;
        }

        state.status(PrototypeStatus.LOADING);
        state.statusMessage("Running stub generation workflow...");
        emit();

        GenerationResult result = codeGenerationService.generate(
                state.selectedDto(),
                List.copyOf(state.activeRules()),
                state.generationMode()
        );

        state.preview(outputPreviewService.buildPreview(result));
        state.status(result.success() ? PrototypeStatus.SUCCESS : PrototypeStatus.ERROR);
        state.statusMessage(result.summary());
        emit();
    }

    private void preloadRules(List<RuleTemplate> templates) {
        state.activeRules().clear();
        DtoDescriptor dto = state.selectedDto();
        if (dto == null) {
            return;
        }

        int initialCount = Math.min(5, templates.size());
        for (int i = 0; i < initialCount; i++) {
            RuleTemplate template = templates.get(i);
            state.activeRules().add(ruleTemplateService.instantiateTemplate(template, dto, i + 1));
        }
    }

    private void emit() {
        pcs.firePropertyChange(STATE_PROPERTY, null, state);
    }
}
