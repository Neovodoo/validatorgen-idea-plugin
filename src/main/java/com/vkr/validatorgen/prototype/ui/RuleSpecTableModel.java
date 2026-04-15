package com.vkr.validatorgen.prototype.ui;

import com.vkr.validatorgen.prototype.model.RuleSpec;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public final class RuleSpecTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"Rule ID", "Category", "Expression", "Target", "Message"};

    private List<RuleSpec> rules = List.of();

    public void setRules(List<RuleSpec> rules) {
        this.rules = rules;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return rules.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        RuleSpec spec = rules.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> spec.id();
            case 1 -> spec.category().name();
            case 2 -> spec.expression();
            case 3 -> spec.targetField();
            case 4 -> spec.message();
            default -> "";
        };
    }
}
