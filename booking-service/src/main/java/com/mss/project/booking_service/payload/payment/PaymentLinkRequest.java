package com.mss.project.booking_service.payload.payment;

import lombok.Data;
import vn.payos.type.ItemData;

import java.util.List;

@Data
public class PaymentLinkRequest {
    private int amount;
    private List<ItemData> items;
    private String description;
}
