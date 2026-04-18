package com.vkr.validatorgen.prototype.stubs;

import com.vkr.validatorgen.prototype.application.RuleTemplateService;
import com.vkr.validatorgen.prototype.model.*;

import java.util.List;

public final class RuleTemplateServiceStub implements RuleTemplateService {
    @Override
    public List<RuleTemplate> listTemplates() {
        return List.of(
                new RuleTemplate("T-CR-1", RuleCategory.CONDITIONAL_REQUIRED,
                        "Conditional required",
                        "If customerType = BUSINESS then taxId is required",
                        "customerType == BUSINESS -> taxId != null",
                        "taxId is required for business customers"),
                new RuleTemplate("T-ME-1", RuleCategory.MUTUAL_EXCLUSION,
                        "Mutual exclusion",
                        "Exactly one payment source must be provided",
                        "xor(cardToken, iban)",
                        "Provide either cardToken or iban"),
                new RuleTemplate("T-FC-1", RuleCategory.FIELD_COMPARISON,
                        "Field comparison",
                        "Amount must not exceed limit",
                        "amount <= limit",
                        "Amount exceeds allowed limit"),
                new RuleTemplate("T-TO-1", RuleCategory.TEMPORAL_ORDER,
                        "Temporal order",
                        "End must be after start",
                        "startDate < endDate",
                        "endDate must be after startDate"),
                new RuleTemplate("T-AC-1", RuleCategory.ARITHMETIC_CONSISTENCY,
                        "Arithmetic consistency",
                        "Total equals subtotal + tax",
                        "total == amount + tax",
                        "Total is inconsistent with amount and tax")
        );
    }

    @Override
    public RuleSpec instantiateTemplate(RuleTemplate template, DtoDescriptor dto, int ordinal) {
        String fieldFallback = dto.fields().isEmpty() ? "<root>" : dto.fields().get(0);
        String expression = template.expressionExample().replace("startDate", guessField(dto, "startDate", fieldFallback))
                .replace("endDate", guessField(dto, "endDate", fieldFallback))
                .replace("amount", guessField(dto, "amount", fieldFallback))
                .replace("limit", guessField(dto, "discount", fieldFallback))
                .replace("taxId", guessField(dto, "state", fieldFallback))
                .replace("customerType", guessField(dto, "customerType", fieldFallback))
                .replace("cardToken", guessField(dto, "cardToken", fieldFallback))
                .replace("iban", guessField(dto, "iban", fieldFallback))
                .replace("total", guessField(dto, "total", fieldFallback))
                .replace("tax", guessField(dto, "tax", fieldFallback));

        String ruleId = template.id() + "-" + ordinal;
        return new RuleSpec(ruleId, template.category(), expression, fieldFallback, template.defaultMessage());
    }

    private String guessField(DtoDescriptor dto, String preferred, String fallback) {
        for (String field : dto.fields()) {
            if (field.equalsIgnoreCase(preferred)) {
                return field;
            }
        }
        return fallback;
    }
}
