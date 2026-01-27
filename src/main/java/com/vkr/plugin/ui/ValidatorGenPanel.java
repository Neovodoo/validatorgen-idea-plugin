package com.vkr.plugin.ui;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.vkr.validatorgen.application.*;
import com.vkr.validatorgen.domain.CompareOp;
import com.vkr.validatorgen.domain.RuleRepository;
import com.vkr.validatorgen.infrastructure.InMemoryRuleRepository;
import com.vkr.validatorgen.infrastructure.JavaValidatorGenerator;
import com.vkr.validatorgen.infrastructure.PsiDtoParser;
import com.vkr.validatorgen.presentation.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ValidatorGenPanel implements ValidatorGenView {

    private final JPanel root = new JPanel(new BorderLayout());

    private final FileType javaFileType = FileTypeManager.getInstance().getFileTypeByExtension("java");

    private final Document dtoDocument;
    private final EditorTextField dtoEditor;

    private final Document generatedDocument;
    private final EditorTextField generatedCodeEditor;

    private final JBTextArea outputArea = new JBTextArea();

    private List<String> availableFields = List.of();


    private final JButton refreshFieldsButton = new JButton("Refresh fields");
    private final JButton addRuleButton = new JButton("Add");
    private final JButton removeRuleButton = new JButton("Remove");
    private final JButton generateButton = new JButton("Generate code");
    private final JButton copyButton = new JButton("Copy");
    private final JButton saveButton = new JButton("Save to generated-sources");


    // repo + table
    private final RuleRepository repo = new InMemoryRuleRepository();
    private final RulesTableModel rulesTableModel = new RulesTableModel(repo);
    private final JTable rulesTable = new JTable(rulesTableModel);

    // presenter
    private final ValidatorGenPresenter presenter;

    private final Project project;

    public ValidatorGenPanel(Project project) {
        this.project = project;
        dtoDocument = EditorFactory.getInstance().createDocument(defaultDtoText());
        dtoEditor = new EditorTextField(dtoDocument, project, javaFileType, false, false);
        dtoEditor.setOneLineMode(false);

        generatedDocument = EditorFactory.getInstance().createDocument("");
        generatedCodeEditor = new EditorTextField(generatedDocument, project, javaFileType, true, false);
        generatedCodeEditor.setOneLineMode(false);

        outputArea.setEditable(false);
        outputArea.setText("Output / diagnostics will appear here...");

        // wiring
        var parser = new PsiDtoParser(project);
        var generator = new JavaValidatorGenerator();

        var refreshFields = new RefreshFieldsUseCase(parser);
        var addRule = new AddRuleUseCase(repo);
        var removeRule = new RemoveRuleUseCase(repo);
        var generate = new GenerateCodeUseCase(parser, repo, generator);

        var clipboard = new com.vkr.validatorgen.infrastructure.AwtClipboardService();
        var saver = new com.vkr.validatorgen.infrastructure.DefaultGeneratedCodeSaver(project);
        var copyUc = new CopyGeneratedCodeUseCase(clipboard);
        var saveUc = new SaveGeneratedCodeUseCase(parser, saver);

        presenter = new ValidatorGenPresenter(
                this,
                refreshFields,
                addRule,
                removeRule,
                generate,
                copyUc,
                saveUc
        );

        buildUi();

        // handlers
        refreshFieldsButton.addActionListener(e -> presenter.onRefreshFields());
        addRuleButton.addActionListener(e -> {
            if (availableFields == null || availableFields.isEmpty()) {
                showOutput("No fields available. Click Refresh fields first.");
                return;
            }

            RuleConfigDialog dlg = new RuleConfigDialog(project, availableFields);
            if (dlg.showAndGet()) { // true если OK
                RuleDraft draft = dlg.getResultDraft();
                if (draft != null) {
                    presenter.onAddRule(draft); // новый метод
                }
            }
        });
        removeRuleButton.addActionListener(e -> presenter.onRemoveRule());
        generateButton.addActionListener(e -> presenter.onGenerateCode());
        copyButton.addActionListener(e -> presenter.onCopyGenerated());
        saveButton.addActionListener(e -> presenter.onSaveGenerated());


        // initial
        presenter.onRefreshFields();
    }

    private void buildUi() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(refreshFieldsButton);
        controls.add(addRuleButton);
        controls.add(removeRuleButton);
        controls.add(generateButton);
        controls.add(copyButton);
        controls.add(saveButton);


        rulesTable.setFillsViewportHeight(true);
        rulesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Conditions", new JBScrollPane(rulesTable));
        tabs.addTab("Generated code", new JBScrollPane(generatedCodeEditor));

        JPanel center = new JPanel(new BorderLayout());
        center.add(new JBScrollPane(dtoEditor), BorderLayout.CENTER);

        JPanel east = new JPanel(new BorderLayout());
        east.setPreferredSize(new Dimension(520, 600));
        east.add(tabs, BorderLayout.CENTER);

        JBScrollPane outPane = new JBScrollPane(outputArea);
        outPane.setPreferredSize(new Dimension(0, 140));

        root.add(controls, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(east, BorderLayout.EAST);
        root.add(outPane, BorderLayout.SOUTH);
    }

    public JComponent getComponent() {
        return root;
    }

    // ---------------- ValidatorGenView ----------------

    @Override
    public String getDtoText() {
        return dtoEditor.getText();
    }


    @Override
    public Integer getSelectedRuleIndex() {
        int idx = rulesTable.getSelectedRow();
        return idx >= 0 ? idx : null;
    }

    @Override
    public void showFields(List<String> fields) {
        this.availableFields = fields;
    }

    @Override
    public void showOutput(String text) {
        outputArea.setText(text);
    }

    @Override
    public void showGeneratedCode(String code) {
        generatedCodeEditor.setText(code);
    }

    @Override
    public void refreshRulesTable() {
        rulesTableModel.reload();
    }

    @Override
    public String getGeneratedCode() {
        return generatedCodeEditor.getText();
    }


    // ---------------- defaults ----------------

    private String defaultDtoText() {
        return """
                package com.example.demo.dto;

                public class OrderDto {

                    private final int amount;   // A
                    private final int limit;    // B
                    private final String comment;

                    public OrderDto(int amount, int limit, String comment) {
                        this.amount = amount;
                        this.limit = limit;
                        this.comment = comment;
                    }

                    public int getAmount() { return amount; }
                    public int getLimit() { return limit; }
                    public String getComment() { return comment; }
                }
                """;
    }
}
