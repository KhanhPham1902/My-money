package com.example.mymoney.model;

import java.io.Serializable;

public class SpendingItem implements Serializable {
    private Integer itemType;       // 0: chi tiêu, 1: thu nhập
    private String itemName;
    private int imgItem;

    public SpendingItem(Integer itemType, String itemName, int imgItem) {
        this.itemType = itemType;
        this.itemName = itemName;
        this.imgItem = imgItem;
    }

    public Integer getItemType() {
        return itemType;
    }

    public void setItemType(Integer itemType) {
        this.itemType = itemType;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getImgItem() {
        return imgItem;
    }

    public void setImgItem(int imgItem) {
        this.imgItem = imgItem;
    }
}
