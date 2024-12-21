// src/main/java/com/yourapp/dto/InboundRequest.java
package com.chimera.weapp.dto;


public class InboundRequest {

    private String inventoryId;

    private int amount;

    // Getters and Setters

    public String getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}