package software.openlab.purchase.domain.order;

/** Listing filters; every field may be null. q matches the number or the supplier name. */
public record OrderFilter(String q, OrderState state, String supplierId, ReceiptStatus receiptStatus,
                          BillStatus billStatus) {
}
