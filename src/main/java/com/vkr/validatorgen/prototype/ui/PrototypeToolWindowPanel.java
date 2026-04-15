package com.vkr.validatorgen.prototype.ui;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.vkr.validatorgen.prototype.model.*;
import com.vkr.validatorgen.prototype.state.PrototypeState;
import com.vkr.validatorgen.prototype.state.PrototypeStatus;
import com.vkr.validatorgen.prototype.state.PrototypeViewModel;

import javax.swing.*;
import java.awt.*;

public final class PrototypeToolWindowPanel {

    private final JPanel root = new JPanel(new BorderLayout());

    private final PrototypeViewModel viewModel;

    private final DefaultListModel<DtoDescriptor> dtoListModel = new DefaultListModel<>();
    private final JBList<DtoDescriptor> dtoList = new JBList<>(dtoListModel);

    private final DefaultListModel<RuleTemplate> templateListModel = new DefaultListModel<>();
    private final JBList<RuleTemplate> templateList = new JBList<>(templateListModel);

    private final RuleSpecTableModel ruleTableModel = new RuleSpecTableModel();
    private final JBTable ruleTable = new JBTable(ruleTableModel);

    private final JComboBox<GenerationMode> modeCombo = new JComboBox<>(GenerationMode.values());
    private final JLabel statusLabel = new JLabel("Ready");
    private final JBTextArea diagnosticsArea = new JBTextArea();
    private final EditorTextField previewEditor;

    private final JButton validateButton = new JButton("Validate spec");
    private final JButton generateButton = new JButton("Generate (stub)");
    private final JButton addTemplateRuleButton = new JButton("Add template rule");
    private final JButton removeRuleButton = new JButton("Remove rule");

    private final JBCheckBox compactDensity = new JBCheckBox("Compact density", false);

    public PrototypeToolWindowPanel(Project project, PrototypeViewModel viewModel) {
        this.viewModel = viewModel;

        Document doc = EditorFactory.getInstance().createDocument("No generation yet.");
        this.previewEditor = new EditorTextField(doc, project,
                FileTypeManager.getInstance().getFileTypeByExtension("java"), true, false);

        buildUi();
        bindHandlers();
        bindState();
        this.viewModel.initialize();
    }

    public JComponent component() {
        return root;
    }

    private void buildUi() {
        root.setBorder(JBUI.Borders.empty(8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, JBUI.scale(8), 0));
        top.add(new JLabel("Generation mode:"));
        top.add(modeCombo);
        top.add(validateButton);
        top.add(generateButton);
        top.add(compactDensity);

        JBSplitter horizontal = new JBSplitter(false, 0.26f);
        horizontal.setFirstComponent(buildLeftPane());
        horizontal.setSecondComponent(buildCenterRight());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(JBUI.Borders.emptyTop(6));
        bottom.add(statusLabel, BorderLayout.CENTER);

        root.add(top, BorderLayout.NORTH);
        root.add(horizontal, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
    }

    private JComponent buildLeftPane() {
        dtoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel dtoPanel = new JPanel(new BorderLayout());
        dtoPanel.setBorder(JBUI.Borders.emptyBottom(6));
        dtoPanel.add(new JLabel("DTO Catalog"), BorderLayout.NORTH);
        dtoPanel.add(new JBScrollPane(dtoList), BorderLayout.CENTER);

        JPanel templatePanel = new JPanel(new BorderLayout());
        templatePanel.add(new JLabel("Rule Templates"), BorderLayout.NORTH);
        templatePanel.add(new JBScrollPane(templateList), BorderLayout.CENTER);
        JPanel templateButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, JBUI.scale(8), 0));
        templateButtons.add(addTemplateRuleButton);
        templatePanel.add(templateButtons, BorderLayout.SOUTH);

        JPanel container = new JPanel(new BorderLayout());
        container.add(dtoPanel, BorderLayout.NORTH);
        container.add(templatePanel, BorderLayout.CENTER);

        return container;
    }

    private JComponent buildCenterRight() {
        JBSplitter split = new JBSplitter(false, 0.56f);
        split.setFirstComponent(buildRulesAndDiagnostics());
        split.setSecondComponent(buildPreviewPane());
        return split;
    }

    private JComponent buildRulesAndDiagnostics() {
        ruleTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        ruleTable.setStriped(true);

        JPanel rulePanel = ToolbarDecorator.createDecorator(ruleTable)
                .disableAddAction()
                .disableRemoveAction()
                .createPanel();
        rulePanel.setBorder(JBUI.Borders.empty());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, JBUI.scale(8), 0));
        actions.add(removeRuleButton);

        JPanel rulesSection = new JPanel(new BorderLayout());
        rulesSection.add(new JLabel("Specification Rules"), BorderLayout.NORTH);
        rulesSection.add(rulePanel, BorderLayout.CENTER);
        rulesSection.add(actions, BorderLayout.SOUTH);

        diagnosticsArea.setEditable(false);
        diagnosticsArea.setLineWrap(true);
        diagnosticsArea.setWrapStyleWord(true);

        JPanel diagnosticsSection = new JPanel(new BorderLayout());
        diagnosticsSection.setBorder(JBUI.Borders.emptyTop(6));
        diagnosticsSection.add(new JLabel("Diagnostics"), BorderLayout.NORTH);
        diagnosticsSection.add(new JBScrollPane(diagnosticsArea), BorderLayout.CENTER);

        JBSplitter split = new JBSplitter(true, 0.7f);
        split.setFirstComponent(rulesSection);
        split.setSecondComponent(diagnosticsSection);
        return split;
    }

    private JComponent buildPreviewPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Output Preview"), BorderLayout.NORTH);
        panel.add(new JBScrollPane(previewEditor), BorderLayout.CENTER);
        return panel;
    }

    private void bindHandlers() {
        modeCombo.addActionListener(e -> {
            GenerationMode mode = (GenerationMode) modeCombo.getSelectedItem();
            if (mode != null) {
                viewModel.setMode(mode);
            }
        });

        dtoList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                DtoDescriptor selected = dtoList.getSelectedValue();
                if (selected != null) {
                    viewModel.selectDto(selected);
                }
            }
        });

        addTemplateRuleButton.addActionListener(e -> {
            RuleTemplate selected = templateList.getSelectedValue();
            if (selected != null) {
                viewModel.addTemplateRule(selected);
            }
        });

        removeRuleButton.addActionListener(e -> viewModel.removeRule(ruleTable.getSelectedRow()));
        validateButton.addActionListener(e -> viewModel.validateSpecification());
        generateButton.addActionListener(e -> viewModel.generate());

        compactDensity.addActionListener(e -> {
            int rowHeight = compactDensity.isSelected() ? JBUI.scale(20) : JBUI.scale(26);
            ruleTable.setRowHeight(rowHeight);
            dtoList.setFixedCellHeight(rowHeight);
            templateList.setFixedCellHeight(rowHeight);
        });
    }

    private void bindState() {
        viewModel.addStateListener(evt -> render((PrototypeState) evt.getNewValue()));
    }

    private void render(PrototypeState state) {
        refillDtoCatalog(state);
        refillTemplates(state);
        ruleTableModel.setRules(state.activeRules());

        modeCombo.setSelectedItem(state.generationMode());
        previewEditor.setText(state.preview().content());
        diagnosticsArea.setText(toDiagnosticsText(state));

        statusLabel.setText(statusPrefix(state.status()) + " " + state.statusMessage());
    }

    private void refillDtoCatalog(PrototypeState state) {
        dtoListModel.clear();
        for (DtoDescriptor dto : state.catalog()) {
            dtoListModel.addElement(dto);
        }
        if (state.selectedDto() != null) {
            dtoList.setSelectedValue(state.selectedDto(), true);
        }
    }

    private void refillTemplates(PrototypeState state) {
        templateListModel.clear();
        for (RuleTemplate template : state.templates()) {
            templateListModel.addElement(template);
        }
        if (!templateListModel.isEmpty() && templateList.getSelectedIndex() < 0) {
            templateList.setSelectedIndex(0);
        }
    }

    private String toDiagnosticsText(PrototypeState state) {
        if (state.diagnostics().isEmpty()) {
            return "No diagnostics yet. Click 'Validate spec'.";
        }

        StringBuilder sb = new StringBuilder();
        for (ValidationIssue issue : state.diagnostics()) {
            sb.append("[").append(issue.severity()).append("] ")
                    .append(issue.message())
                    .append("\n");
        }
        return sb.toString();
    }

    private String statusPrefix(PrototypeStatus status) {
        return switch (status) {
            case EMPTY -> "[EMPTY]";
            case LOADING -> "[LOADING]";
            case READY -> "[READY]";
            case ERROR -> "[ERROR]";
            case SUCCESS -> "[SUCCESS]";
        };
    }
}
