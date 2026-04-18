package com.vkr.validatorgen.prototype.stubs;

import com.vkr.validatorgen.prototype.application.DtoCatalogService;
import com.vkr.validatorgen.prototype.model.DtoDescriptor;

import java.util.List;

public final class DtoCatalogServiceStub implements DtoCatalogService {
    @Override
    public List<DtoDescriptor> listAvailableDtos() {
        return List.of(
                new DtoDescriptor("com.example.billing.dto", "InvoiceDto", List.of("id", "currency", "amount", "tax", "total", "issueDate", "dueDate")),
                new DtoDescriptor("com.example.order.dto", "OrderDto", List.of("orderId", "customerType", "amount", "discount", "startDate", "endDate")),
                new DtoDescriptor("com.example.customer.dto", "CustomerProfileDto", List.of("customerId", "email", "phone", "country", "state")),
                new DtoDescriptor("com.example.payment.dto", "PaymentDto", List.of("method", "cardToken", "iban", "swift", "scheduledAt")),
                new DtoDescriptor("com.example.shipment.dto", "ShipmentDto", List.of("shipmentId", "departureTime", "arrivalTime", "weight", "volume"))
        );
    }
}
