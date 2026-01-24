package com.vkr.plugin.ui;

import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;

public class ValidatorGenPanel {
    private final JPanel root = new JPanel(new BorderLayout());

    public ValidatorGenPanel(Project project) {
        JLabel label = new JLabel("Скелет интерефейса плагина");
        label.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(label, BorderLayout.NORTH);

        JTextArea area = new JTextArea();
        area.setText("Какой то текст");
        area.setEditable(false);
        root.add(new JScrollPane(area), BorderLayout.CENTER);
    }

    public JComponent getComponent() {
        return root;
    }
}
