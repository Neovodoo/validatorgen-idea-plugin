package com.vkr.plugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class ValidatorGenToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ValidatorGenPanel panel = new ValidatorGenPanel(project);
        Content content = ContentFactory.getInstance()
                .createContent(panel.getComponent(), "Prototype", false);
        toolWindow.getContentManager().addContent(content);
    }
}
