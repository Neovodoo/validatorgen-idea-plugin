package com.vkr.plugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBTextField;
import com.vkr.validatorgen.domain.CompareOp;
import com.vkr.validatorgen.presentation.RuleDraft;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public final class RuleConfigDialog extends DialogWrapper {

    private final Map<String, List<String>> fieldsByType;
    private static final Map<String, CompareOp[]> OPS_BY_TYPE = Map.of(
            "int", new CompareOp[]{CompareOp.EQ, CompareOp.NE, CompareOp.GT, CompareOp.GE, CompareOp.LT, CompareOp.LE},
            "String", new CompareOp[]{CompareOp.EQ, CompareOp.NE}
    );

    private JComboBox<String> fieldTypeCombo;
    private JComboBox<String> leftCombo;
    private JComboBox<CompareOp> opCombo;
    private JComboBox<String> rightCombo;

    private JCheckBox attachToFieldCheck;
    private JComboBox<String> targetCombo;

    private JBTextField messageField;
    private JLabel errorLabel;

    private RuleDraft resultDraft;

    public RuleConfigDialog(@Nullable Project project, Map<String, List<String>> fieldsByType) {
        super(project);
        this.fieldsByType = fieldsByType;

        setTitle("Add validation rule");
        init(); // важно: вызывает createCenterPanel
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0;
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        fieldTypeCombo = new JComboBox<>(fieldsByType.keySet().stream().sorted().toArray(String[]::new));
        leftCombo = new JComboBox<>();
        opCombo = new JComboBox<>();
        rightCombo = new JComboBox<>();


        attachToFieldCheck = new JCheckBox("Attach violation to field (target)", true);
        targetCombo = new JComboBox<>();

        messageField = new JBTextField("Field A must be > Field B");

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(UIManager.getColor("Label.foreground"));

        // Row 1: A op B
        addRow(panel, c, 0, "Type:", fieldTypeCombo);
        addRow(panel, c, 1, "A:", leftCombo);
        addRow(panel, c, 2, "Op:", opCombo);
        addRow(panel, c, 3, "B:", rightCombo);

        // Row 2: target enable + combo
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 3;
        panel.add(attachToFieldCheck, c);

        c.gridx = 3;
        c.gridwidth = 1;
        panel.add(targetCombo, c);

        // Row 3: message
        c.gridy++;
        c.gridx = 0;
        panel.add(new JLabel("Message:"), c);

        c.gridx = 1;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(messageField, c);

        // Row 4: error
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 4;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(errorLabel, c);

        fieldTypeCombo.addActionListener(e -> onTypeChanged());
        // UX: по умолчанию target = left
        targetCombo.setSelectedItem(leftCombo.getSelectedItem());
        leftCombo.addActionListener(e -> {
            if (attachToFieldCheck.isSelected()) {
                targetCombo.setSelectedItem(leftCombo.getSelectedItem());
            }
        });

        attachToFieldCheck.addActionListener(e -> targetCombo.setEnabled(attachToFieldCheck.isSelected()));
        targetCombo.setEnabled(true);
        onTypeChanged();

        panel.setPreferredSize(new Dimension(560, 200));
        return panel;
    }

    private void onTypeChanged() {
        String selectedType = (String) fieldTypeCombo.getSelectedItem();
        List<String> fields = selectedType == null ? List.of() : fieldsByType.getOrDefault(selectedType, List.of());
        leftCombo.setModel(new DefaultComboBoxModel<>(fields.toArray(String[]::new)));
        rightCombo.setModel(new DefaultComboBoxModel<>(fields.toArray(String[]::new)));
        targetCombo.setModel(new DefaultComboBoxModel<>(fields.toArray(String[]::new)));

        CompareOp[] ops = selectedType == null ? new CompareOp[0] : OPS_BY_TYPE.getOrDefault(selectedType, CompareOp.values());
        opCombo.setModel(new DefaultComboBoxModel<>(ops));

        if (leftCombo.getItemCount() > 0) {
            leftCombo.setSelectedIndex(0);
            targetCombo.setSelectedIndex(0);
        }
        if (rightCombo.getItemCount() > 1) {
            rightCombo.setSelectedIndex(1);
        }
    }

    private void addRow(JPanel panel, GridBagConstraints c, int x, String label, JComponent comp) {
        c.gridx = x * 2;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(label), c);

        c.gridx = x * 2 + 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        if (comp instanceof JComboBox) {
            comp.setPreferredSize(new Dimension(160, comp.getPreferredSize().height));
        }
        panel.add(comp, c);
    }

    @Override
    protected void doOKAction() {
        // Лёгкая UI-валидация (бизнес-валидацию всё равно сделает UseCase через RuleValidator)
        String left = (String) leftCombo.getSelectedItem();
        String right = (String) rightCombo.getSelectedItem();
        CompareOp op = (CompareOp) opCombo.getSelectedItem();
        String message = messageField.getText() == null ? "" : messageField.getText().trim();

        String target = null;
        if (attachToFieldCheck.isSelected()) {
            target = (String) targetCombo.getSelectedItem();
        } else {
            // если target не нужен — можно выбрать стратегию:
            // 1) target = left (самый удобный)
            // 2) target = "" (глобальная ошибка)
            target = left;
        }

        if (left == null || left.isBlank() || right == null || right.isBlank() || op == null) {
            showError("Please select A, Op and B.");
            return;
        }
        if (left.equals(right)) {
            showError("A and B should be different.");
            return;
        }
        if (message.isBlank()) {
            showError("Message must not be empty.");
            return;
        }
        if (target == null || target.isBlank()) {
            showError("Target must not be empty.");
            return;
        }

        resultDraft = new RuleDraft(left, op, right, target, message);
        super.doOKAction();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }

    public @Nullable RuleDraft getResultDraft() {
        return resultDraft;
    }
}
