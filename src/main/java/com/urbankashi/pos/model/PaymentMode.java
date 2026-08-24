package com.urbankashi.pos.model;

import lombok.Getter;

@Getter
public enum PaymentMode {
    CASH("Cash"),
    UPI("UPI"),
    CARD("Card"),
    SPLIT("Split Payment");

    private final String displayName;

    PaymentMode(String displayName) {
        this.displayName = displayName;
    }
}
