package com.example.moneybook.daily;

import java.io.Serializable;

public class DailyInAndOut implements Serializable {
    private int id;
    private String type;
    private String date;
    private String assetName;
    private String categoryName;
    private int amount;
    private String memo;
    private String regDateTime;

    public DailyInAndOut() {}

    public DailyInAndOut(int id,String type,String date, String assetName, String categoryName, int amount, String memo) {
        this.id = id;
        this.type = type;
        this.date = date;
        this.assetName = assetName;
        this.categoryName = categoryName;
        this.amount = amount;
        this.memo = memo;
    }

    public DailyInAndOut(int id, String type, String date, String assetName, String categoryName, int amount, String memo, String regDateTime) {
        this.id = id;
        this.type = type;
        this.date = date;
        this.assetName = assetName;
        this.categoryName = categoryName;
        this.amount = amount;
        this.memo = memo;
        this.regDateTime = regDateTime;
    }

    @Override
    public String toString() {
        return "DailyInAndOut{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", date='" + date + '\'' +
                ", assetName='" + assetName + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", amount=" + amount +
                ", memo='" + memo + '\'' +
                ", regDateTime='" + regDateTime + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getRegDateTime() {
        return regDateTime;
    }

    public void setRegDateTime(String regDateTime) {
        this.regDateTime = regDateTime;
    }
}
