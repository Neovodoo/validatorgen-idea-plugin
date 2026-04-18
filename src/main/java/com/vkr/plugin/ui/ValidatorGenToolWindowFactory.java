package com.vkr.plugin.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.vkr.validatorgen.prototype.state.PrototypeViewModel;
import com.vkr.validatorgen.prototype.stubs.*;
import com.vkr.validatorgen.prototype.ui.PrototypeToolWindowPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ValidatorGenToolWindowFactory implements ToolWindowFactory, DumbAware {
    private static final Logger LOG = Logger.getInstance(ValidatorGenToolWindowFactory.class);

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setTitle("ValidatorGen");
        JComponent contentComponent;
        try {
            PrototypeViewModel viewModel = new PrototypeViewModel(
                    new DtoCatalogServiceStub(),
                    new RuleTemplateServiceStub(),
                    new SpecificationValidationServiceStub(),
                    new CodeGenerationServiceStub(),
                    new OutputPreviewServiceStub()
            );

            PrototypeToolWindowPanel panel = new PrototypeToolWindowPanel(project, viewModel);
            contentComponent = panel.component();
        } catch (Throwable t) {
            LOG.error("Unable to initialize ValidatorGen prototype tool window", t);
            contentComponent = buildFallbackPanel(t);
        }
        Content content = ContentFactory.getInstance()
                .createContent(contentComponent, "Prototype", false);
        toolWindow.getContentManager().addContent(content);
    }

    private JComponent buildFallbackPanel(Throwable throwable) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Failed to initialize ValidatorGen UI."), BorderLayout.NORTH);

        JTextArea details = new JTextArea(throwable.toString());
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        panel.add(new JScrollPane(details), BorderLayout.CENTER);
        return panel;
    }
}
