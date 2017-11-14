/*
 * Created by Wisam Naji on 11/7/17 3:33 AM.
 * Copyright (c) 2017. All rights reserved.
 * Copying, redistribution or usage of material used in this file is free for educational purposes ONLY and should not be used in profitable context.
 *
 * Last modified on 11/7/17 3:33 AM
 */

package com.recoded.estock.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

import com.recoded.estock.Product;

/**
 * Created by wisam on Nov 7 17.
 */

public class ProductsHelper extends SQLiteOpenHelper {
    public static final String AUTHORITY = "com.recoded.estock";
    private static final String DB_NAME = "products.db";
    private static final int DB_VER = 1;


    public ProductsHelper(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TableProducts.getCreateSql());
        db.execSQL(TableCategories.getCreateSql());

        //create demo categories
        ContentValues values = new ContentValues();
        for (String CATEGORY : TableCategories.CATEGORIES) {
            values.put(TableCategories.NAME, CATEGORY);
            db.insert(TableCategories.TABLE, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //The contract class for products table
    public final static class TableProducts implements BaseColumns {
        public static final String TABLE, NAME, CATEGORY, PRICE, QUANTITY, DESCRIPTION, IMAGE;

        static {
            TABLE = "products";
            NAME = "product_name";
            CATEGORY = "category_id";
            PRICE = "product_price";
            QUANTITY = "stock_quantity";
            DESCRIPTION = "product_desc";
            IMAGE = "image_path";
        }

        public static final String[] ALL_FIELDS = {_ID, NAME, CATEGORY, PRICE, QUANTITY, IMAGE, DESCRIPTION};
        public static final Uri PRODUCTS_URI = Uri.parse("content://" + AUTHORITY + '/' + TABLE);

        static String getCreateSql() {
            return "CREATE TABLE " + TABLE + "(" +
                    _ID + " INTEGER PRIMARY KEY, " +
                    NAME + " TEXT NOT NULL, " +
                    CATEGORY + " INTEGER NOT NULL, " +
                    PRICE + " REAL NOT NULL, " +
                    QUANTITY + " INTEGER NOT NULL, " +
                    IMAGE + " TEXT," +
                    DESCRIPTION + " TEXT);";
        }

        public static ContentValues createProductValues(Product p) {
            ContentValues values = new ContentValues(5);
            values.put(NAME, p.getProductName());
            values.put(CATEGORY, p.getCategory());
            values.put(PRICE, p.getPriceD());
            values.put(QUANTITY, p.getQuantity());
            values.put(DESCRIPTION, p.getProductDesc());
            values.put(IMAGE, p.getImagePath());
            return values;
        }
    }

    //The contract class for categories table;
    public final static class TableCategories implements BaseColumns {
        public static final String TABLE, NAME;
        private static final String[] CATEGORIES = {"Women", "Men", "Boys", "Girls", "Newborns"};

        static {
            TABLE = "categories";
            NAME = "category_name";
        }

        public static final Uri CATEGORIES_URI = Uri.parse("content://" + AUTHORITY + '/' + "productCategory");
        static String getCreateSql() {
            return "CREATE TABLE " + TABLE + "(" +
                    _ID + " INTEGER PRIMARY KEY, " +
                    NAME + " TEXT NOT NULL);";
        }
    }
}
