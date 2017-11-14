/*
 * Created by Wisam Naji on 11/7/17 2:57 AM.
 * Copyright (c) 2017. All rights reserved.
 * Copying, redistribution or usage of material used in this file is free for educational purposes ONLY and should not be used in profitable context.
 *
 * Last modified on 11/7/17 2:57 AM
 */

package com.recoded.estock;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wisam on Nov 7 17.
 */

public class Product implements Parcelable {
    private long stockId;
    private String productName;
    private String productDesc;
    private String catName;
    private double price;
    private int quantity;
    private long category;
    private String imagePath;

    public Product(long stockId, String productName, long category, double price, int quantity) {
        this.stockId = stockId;
        this.productName = productName;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.productDesc = "";
    }

    public Product() {
        this.stockId = -1;
        this.productName = "";
        this.category = -1;
        this.price = 0.0;
        this.quantity = 0;
        this.productDesc = "";
        this.imagePath = "";
    }

    public long getStockId() {
        return stockId;
    }

    public String getProductName() {
        return productName;
    }

    public String getPrice() {
        return '$' + String.valueOf(price);
    }

    public double getPriceD() {
        return price;
    }

    public String getImagePath() {
        return imagePath;
    }


    public int getQuantity() {
        return quantity;
    }

    public String getCatName() {
        return "In " + catName;
    }

    public String getQuantityString() {
        return quantity + " item in stock";
    }

    public long getCategory() {
        return category;
    }

    public String getProductDesc() {
        return productDesc;
    }

    public void setProductDesc(String productDesc) {
        this.productDesc = productDesc;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setCategory(long category) {
        this.category = category;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int sell(int quantity) {
        if (quantity < this.quantity) {
            this.quantity -= quantity;
            return this.quantity;
        }
        return this.quantity;
    }

    public int sell() {
        if (quantity != 0) {
            return --quantity;
        }
        return quantity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.stockId);
        dest.writeString(this.productName);
        dest.writeString(this.productDesc);
        dest.writeString(this.catName);
        dest.writeDouble(this.price);
        dest.writeInt(this.quantity);
        dest.writeLong(this.category);
        dest.writeString(this.imagePath);
    }

    protected Product(Parcel in) {
        this.stockId = in.readLong();
        this.productName = in.readString();
        this.productDesc = in.readString();
        this.catName = in.readString();
        this.price = in.readDouble();
        this.quantity = in.readInt();
        this.category = in.readLong();
        this.imagePath = in.readString();
    }

    public static final Parcelable.Creator<Product> CREATOR = new Parcelable.Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel source) {
            return new Product(source);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };
}
