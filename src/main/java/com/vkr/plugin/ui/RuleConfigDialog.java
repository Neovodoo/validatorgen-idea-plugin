package com.vkr.plugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBTextField;
import com.vkr.validatorgen.domain.CompareOp;
import com.vkr.validatorgen.domain.ConditionOperator;
import com.vkr.validatorgen.domain.RuleKind;
import com.vkr.validatorgen.presentation.RuleDraft;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public final class RuleConfigDialog extends DialogWrapper {

    private final Map<String, List<String>> fieldsByType;
    private static final Map<String, CompareOp[]> OPS_BY_TYPE = Map.of(
            "int", new CompareOp[]{CompareOp.EQ, CompareOp.NE, CompareOp.GT, CompareOp.GE, CompareOp.LT, CompareOp.LE}, //TODO: сделать мапу с числовыми типами данныъх и ограничить сравнение только для них
            "String", new CompareOp[]{CompareOp.EQ, CompareOp.NE}
    );

    private JComboBox<RuleKind> ruleKindCombo;
    private JComboBox<String> fieldTypeCombo;
    private JComboBox<String> leftCombo;
    private JComboBox<CompareOp> opCombo;
    private JComboBox<String> rightCombo;
    private JComboBox<ConditionOperator> conditionOpCombo;
    private JBTextField conditionLiteralField;

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

        ruleKindCombo = new JComboBox<>(RuleKind.values());
        fieldTypeCombo = new JComboBox<>(fieldsByType.keySet().stream().sorted().toArray(String[]::new));
        leftCombo = new JComboBox<>();
        opCombo = new JComboBox<>();
        rightCombo = new JComboBox<>();
        conditionOpCombo = new JComboBox<>(ConditionOperator.values());
        conditionLiteralField = new JBTextField("CARD");

        attachToFieldCheck = new JCheckBox("Attach violation to field (target)", true);
        targetCombo = new JComboBox<>();

        messageField = new JBTextField("Validation rule failed");

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(UIManager.getColor("Label.foreground"));

        addRow(panel, c, 0, "Rule:", ruleKindCombo);
        addRow(panel, c, 1, "Type:", fieldTypeCombo);
        addRow(panel, c, 2, "A / Condition field:", leftCombo);

        c.gridy++;
        addRow(panel, c, 0, "Compare op:", opCombo);
        addRow(panel, c, 1, "B:", rightCombo);
        addRow(panel, c, 2, "Condition op:", conditionOpCombo);
        addRow(panel, c, 3, "Literal:", conditionLiteralField);

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 3;
        panel.add(attachToFieldCheck, c);

        c.gridx = 3;
        c.gridwidth = 1;
        panel.add(targetCombo, c);


        c.gridy++;
        c.gridx = 0;
        panel.add(new JLabel("Message:"), c);

        c.gridx = 1;
        c.gridwidth = 5;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(messageField, c);


        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 6;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(errorLabel, c);

        fieldTypeCombo.addActionListener(e -> onTypeChanged());
        ruleKindCombo.addActionListener(e -> onRuleKindChanged());
        leftCombo.addActionListener(e -> {
            if (attachToFieldCheck.isSelected() && selectedKind() == RuleKind.COMPARE_FIELDS){
                targetCombo.setSelectedItem(leftCombo.getSelectedItem());
            }
        });

        attachToFieldCheck.addActionListener(e -> targetCombo.setEnabled(attachToFieldCheck.isSelected()));
        targetCombo.setEnabled(true);
        onTypeChanged();
        onRuleKindChanged();

        panel.setPreferredSize(new Dimension(760, 240));
        return panel;
    }

    private void onTypeChanged() {
        String selectedType = (String) fieldTypeCombo.getSelectedItem();
        List<String> fields = selectedType == null ? List.of() : fieldsByType.getOrDefault(selectedType, List.of());
        leftCombo.setModel(new DefaultComboBoxModel<>(fields.toArray(String[]::new)));
        rightCombo.setModel(new DefaultComboBoxModel<>(fields.toArray(String[]::new)));
        targetCombo.setModel(new DefaultComboBoxModel<>(allFields().toArray(String[]::new)));

        CompareOp[] ops = selectedType == null ? new CompareOp[0] : OPS_BY_TYPE.getOrDefault(selectedType, CompareOp.values());
        opCombo.setModel(new DefaultComboBoxModel<>(ops));

        if (leftCombo.getItemCount() > 0) {
            leftCombo.setSelectedIndex(0);
            targetCombo.setSelectedItem(leftCombo.getSelectedItem());
        }
        if (rightCombo.getItemCount() > 1) {
            rightCombo.setSelectedIndex(1);
        }
    }

    private List<String> allFields() {
        return fieldsByType.values().stream().flatMap(List::stream).distinct().sorted().toList();
    }

    private void onRuleKindChanged() {
        boolean requiredIf = selectedKind() == RuleKind.REQUIRED_IF;
        opCombo.setEnabled(!requiredIf);
        rightCombo.setEnabled(!requiredIf);
        conditionOpCombo.setEnabled(requiredIf);
        conditionLiteralField.setEnabled(requiredIf);
        if (requiredIf) {
            messageField.setText("Field is required when condition is met");
        }
    }

    private RuleKind selectedKind() {
        RuleKind selected = (RuleKind) ruleKindCombo.getSelectedItem();
        return selected == null ? RuleKind.COMPARE_FIELDS : selected;
    }


    private void addRow(JPanel panel, GridBagConstraints c, int x, String label, JComponent comp) {
        c.gridx = x * 2;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(label), c);

        c.gridx = x * 2 + 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        if (comp instanceof JComboBox || comp instanceof JBTextField) {
            comp.setPreferredSize(new Dimension(150, comp.getPreferredSize().height));
        }
        panel.add(comp, c);
    }

    @Override
    protected void doOKAction() {
        String left = (String) leftCombo.getSelectedItem();
        String message = messageField.getText() == null ? "" : messageField.getText().trim();
        String target = attachToFieldCheck.isSelected() ? (String) targetCombo.getSelectedItem() : left;

        if (selectedKind() == RuleKind.REQUIRED_IF) {
            createRequiredIfDraft(left, target, message);
            return;
        }
        createCompareDraft(left, target, message);
    }

    private void createCompareDraft(String left, String target, String message) {
        String right = (String) rightCombo.getSelectedItem();
        CompareOp op = (CompareOp) opCombo.getSelectedItem();
        if (left == null || left.isBlank() || right == null || right.isBlank() || op == null) {
            showError("Please select A, Op and B.");
            return;
        }
        if (left.equals(right)) {
            showError("A and B should be different.");
            return;
        }
        if (!validateMessageAndTarget(message, target)) {
            return;
        }
        resultDraft = new RuleDraft(left, op, right, target, message);
        super.doOKAction();
    }

    private void createRequiredIfDraft(String conditionField, String target, String message) {
        ConditionOperator operator = (ConditionOperator) conditionOpCombo.getSelectedItem();
        String literal = conditionLiteralField.getText() == null ? "" : conditionLiteralField.getText().trim();
        if (conditionField == null || conditionField.isBlank() || operator == null || literal.isBlank()) {
            showError("Please select condition field, operator and literal.");
            return;
        }
        if (!validateMessageAndTarget(message, target)) {
            return;
        }
        resultDraft = RuleDraft.requiredIf(conditionField, operator, literal, target, message);
        super.doOKAction();
    }

    private boolean validateMessageAndTarget(String message, String target) {
        if (message.isBlank()) {
            showError("Message must not be empty.");
            return false;
        }
        if (target == null || target.isBlank()) {
            showError("Target must not be empty.");
            return false;
        }
        return true;
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }

    public @Nullable RuleDraft getResultDraft() {
        return resultDraft;
    }
}
