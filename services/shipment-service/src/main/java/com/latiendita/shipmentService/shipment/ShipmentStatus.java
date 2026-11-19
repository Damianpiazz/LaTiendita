package com.latiendita.shipmentService.shipment;

public enum ShipmentStatus {
    AWAITING_PAYMENT, PENDING_DISPATCH, DISPATCHED, DELIVERED, CANCELLED;

    public boolean canBeCancelled() {
        return this == AWAITING_PAYMENT || this == PENDING_DISPATCH;
    }
}
