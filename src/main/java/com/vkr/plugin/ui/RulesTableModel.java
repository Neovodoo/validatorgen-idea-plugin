package com.vkr.plugin.ui;

import com.vkr.validatorgen.domain.*;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public final class RulesTableModel extends AbstractTableModel {

    private final RuleRepository repo;
    private final String[] columns = {"A", "Op", "B", "Target", "Message"};

    public RulesTableModel(RuleRepository repo) {
        this.repo = repo;
    }

    @Override
    public int getRowCount() {
        return repo.all().size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        List<RuleSpec> rules = repo.all();
        CompareFieldsRule r = (CompareFieldsRule) rules.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> r.getLeft();
            case 1 -> r.getOp().getSymbol();
            case 2 -> r.getRight();
            case 3 -> r.getViolationTarget();
            case 4 -> r.getMessage();
            default -> "";
        };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String v = aValue == null ? "" : aValue.toString();
        CompareFieldsRule old = (CompareFieldsRule) repo.all().get(rowIndex);

        String left = old.getLeft();
        CompareOp op = old.getOp();
        String right = old.getRight();
        String target = old.getViolationTarget();
        String message = old.getMessage();

        switch (columnIndex) {
            case 0 -> left = v;
            case 1 -> op = CompareOp.fromInput(v).orElse(old.getOp());
            case 2 -> right = v;
            case 3 -> target = v;
            case 4 -> message = v;
        }

        repo.updateAt(rowIndex, new CompareFieldsRule(old.getId(), left, op, right, target, message));
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public void reload() {
        fireTableDataChanged();
    }
}
