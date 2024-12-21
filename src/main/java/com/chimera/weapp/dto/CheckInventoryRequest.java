package com.chimera.weapp.dto;


public class CheckInventoryRequest {

    private String inventoryId;

    private int checkedAmount;

    // Getters and Setters

    public String getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
    }

    public int getCheckedAmount() {
        return checkedAmount;
    }

    public void setCheckedAmount(int checkedAmount) {
        this.checkedAmount = checkedAmount;
    }
}
