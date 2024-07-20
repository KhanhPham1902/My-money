package com.example.mymoney.model;

import java.io.Serializable;

public class MoneyData implements Serializable {
    private Integer id;
    private String money;
    private String note;
    private SpendingItem spendingItem;
    private String date;

    public MoneyData() {
    }

    public MoneyData(Integer id, String money, String note, SpendingItem spendingItem, String date) {
        this.id = id;
        this.money = money;
        this.note = note;
        this.spendingItem = spendingItem;
        this.date = date;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public SpendingItem getSpendingItem() {
        return spendingItem;
    }

    public void setSpendingItem(SpendingItem spendingItem) {
        this.spendingItem = spendingItem;
    }
}
