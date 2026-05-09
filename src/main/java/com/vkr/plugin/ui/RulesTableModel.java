package com.vkr.plugin.ui;

import com.vkr.validatorgen.domain.*;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public final class RulesTableModel extends AbstractTableModel {

    private final RuleRepository repo;
    private final String[] columns = {"Kind", "A / Condition", "Op", "B / Literal", "Target", "Message"};

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
        return columnIndex != 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        List<RuleSpec> rules = repo.all();
        RuleSpec rule = rules.get(rowIndex);
        if (rule instanceof RequiredIfRule r) {
            return switch (columnIndex) {
                case 0 -> r.getKind();
                case 1 -> r.getCondition().fieldName();
                case 2 -> r.getCondition().operator().getSymbol();
                case 3 -> r.getCondition().rawLiteral();
                case 4 -> r.getViolationTarget();
                case 5 -> r.getMessage();
                default -> "";
            };
        }
        CompareFieldsRule r = (CompareFieldsRule) rule;
        return switch (columnIndex) {
            case 0 -> r.getKind();
            case 1 -> r.getLeft();
            case 2 -> r.getOp().getSymbol();
            case 3 -> r.getRight();
            case 4 -> r.getViolationTarget();
            case 5 -> r.getMessage();
            default -> "";
        };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String v = aValue == null ? "" : aValue.toString();
        RuleSpec old = repo.all().get(rowIndex);
        if (old instanceof RequiredIfRule requiredIf) {
            updateRequiredIf(v, rowIndex, columnIndex, requiredIf);
        } else {
            updateCompare(v, rowIndex, columnIndex, (CompareFieldsRule) old);
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }


    private void updateCompare(String v, int rowIndex, int columnIndex, CompareFieldsRule old) {
        String left = old.getLeft();
        CompareOp op = old.getOp();
        String right = old.getRight();
        String target = old.getViolationTarget();
        String message = old.getMessage();

        switch (columnIndex) {
            case 1 -> left = v;
            case 2 -> op = CompareOp.fromInput(v).orElse(old.getOp());
            case 3 -> right = v;
            case 4 -> target = v;
            case 5 -> message = v;
        }
        repo.updateAt(rowIndex, new CompareFieldsRule(old.getId(), left, op, right, target, message));
    }
    private void updateRequiredIf(String v, int rowIndex, int columnIndex, RequiredIfRule old) {
        String conditionField = old.getCondition().fieldName();
        ConditionOperator op = old.getCondition().operator();
        String literal = old.getCondition().rawLiteral();
        String target = old.getTargetField();
        String violationTarget = old.getViolationTarget();
        String message = old.getMessage();

        switch (columnIndex) {
            case 1 -> conditionField = v;
            case 2 -> op = parseConditionOperator(v, old.getCondition().operator());
            case 3 -> literal = v;
            case 4 -> {
                target = v;
                violationTarget = v;
            }
            case 5 -> message = v;
        }

        repo.updateAt(rowIndex, new RequiredIfRule(old.getId(), new ConditionSpec(conditionField, op, literal), target, violationTarget, message));
    }

    private ConditionOperator parseConditionOperator(String input, ConditionOperator fallback) {
        for (ConditionOperator op : ConditionOperator.values()) {
            if (op.name().equalsIgnoreCase(input) || op.getSymbol().equals(input)) {
                return op;
            }
        }
        return fallback;
    }

    public void reload() {
        fireTableDataChanged();
    }
}
