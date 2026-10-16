package software.openlab.sales.domain.order;

/** Listing filters; every field may be null. q matches the number or the customer name. */
public record OrderFilter(String q, OrderState state, String customerId, DeliveryStatus deliveryStatus,
                          InvoiceStatus invoiceStatus) {
}
