package com.vkr.plugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.vkr.validatorgen.prototype.state.PrototypeViewModel;
import com.vkr.validatorgen.prototype.stubs.*;
import com.vkr.validatorgen.prototype.ui.PrototypeToolWindowPanel;
import org.jetbrains.annotations.NotNull;

public class ValidatorGenToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        PrototypeViewModel viewModel = new PrototypeViewModel(
                new DtoCatalogServiceStub(),
                new RuleTemplateServiceStub(),
                new SpecificationValidationServiceStub(),
                new CodeGenerationServiceStub(),
                new OutputPreviewServiceStub()
        );

        PrototypeToolWindowPanel panel = new PrototypeToolWindowPanel(project, viewModel);
        Content content = ContentFactory.getInstance()
                .createContent(panel.component(), "Prototype", false);
        toolWindow.getContentManager().addContent(content);
    }
}
