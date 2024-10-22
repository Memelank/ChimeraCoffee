package com.chimera.weapp.dto;

import java.util.List;

public class BatchSupplyOrderDTO {
    private List<String> orderIds;

    public List<String> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(List<String> orderIds) {
        this.orderIds = orderIds;
    }
}
