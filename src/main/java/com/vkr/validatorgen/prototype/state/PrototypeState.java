package com.vkr.validatorgen.prototype.state;

import com.vkr.validatorgen.prototype.model.*;

import java.util.ArrayList;
import java.util.List;

public final class PrototypeState {
    private List<DtoDescriptor> catalog = List.of();
    private List<RuleTemplate> templates = List.of();
    private DtoDescriptor selectedDto;
    private GenerationMode generationMode = GenerationMode.JAKARTA_BEAN_VALIDATION;
    private final List<RuleSpec> activeRules = new ArrayList<>();
    private List<ValidationIssue> diagnostics = List.of();
    private PreviewDocument preview = new PreviewDocument("Generated output", "No generation yet.");
    private PrototypeStatus status = PrototypeStatus.EMPTY;
    private String statusMessage = "Select a DTO to start.";

    public List<DtoDescriptor> catalog() { return catalog; }
    public void catalog(List<DtoDescriptor> catalog) { this.catalog = catalog; }

    public List<RuleTemplate> templates() { return templates; }
    public void templates(List<RuleTemplate> templates) { this.templates = templates; }

    public DtoDescriptor selectedDto() { return selectedDto; }
    public void selectedDto(DtoDescriptor selectedDto) { this.selectedDto = selectedDto; }

    public GenerationMode generationMode() { return generationMode; }
    public void generationMode(GenerationMode generationMode) { this.generationMode = generationMode; }

    public List<RuleSpec> activeRules() { return activeRules; }

    public List<ValidationIssue> diagnostics() { return diagnostics; }
    public void diagnostics(List<ValidationIssue> diagnostics) { this.diagnostics = diagnostics; }

    public PreviewDocument preview() { return preview; }
    public void preview(PreviewDocument preview) { this.preview = preview; }

    public PrototypeStatus status() { return status; }
    public void status(PrototypeStatus status) { this.status = status; }

    public String statusMessage() { return statusMessage; }
    public void statusMessage(String statusMessage) { this.statusMessage = statusMessage; }
}
