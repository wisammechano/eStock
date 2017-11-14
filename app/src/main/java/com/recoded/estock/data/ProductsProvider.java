/*
 * Created by Wisam Naji on 11/11/17 10:59 AM.
 * Copyright (c) 2017. All rights reserved.
 * Copying, redistribution or usage of material used in this file is free for educational purposes ONLY and should not be used in profitable context.
 *
 * Last modified on 11/11/17 10:59 AM
 */

package com.recoded.estock.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.recoded.estock.Product;
import com.recoded.estock.data.ProductsHelper.TableCategories;

import java.net.URI;

import static com.recoded.estock.data.ProductsHelper.AUTHORITY;
import static com.recoded.estock.data.ProductsHelper.TableProducts.*;

/**
 * Created by wisam on Nov 11 17.
 */

public class ProductsProvider extends ContentProvider {
    ProductsHelper dbHelper;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, TABLE, 1);
        uriMatcher.addURI(AUTHORITY, TABLE + "/#", 2);
        uriMatcher.addURI(AUTHORITY, "productCategory/#", 3);
        uriMatcher.addURI(AUTHORITY, "productCategory", 4);
    }
    @Override
    public boolean onCreate() {
        dbHelper = new ProductsHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int code = uriMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        long id;
        switch (code){
            case 1:
                c = db.query(TABLE, ALL_FIELDS, null, null, null, null, null);
                break;
            case 2:
                selection = _ID + "=?";
                id = ContentUris.parseId(uri);
                selectionArgs = new String [] {String.valueOf(id)};
                c = db.query(TABLE, ALL_FIELDS, selection, selectionArgs, null, null, null, null);
                break;
            case 3:
                selection = _ID + "=?";
                id = ContentUris.parseId(uri);
                selectionArgs = new String[] {String.valueOf(id)};
                String[] column = {TableCategories.NAME};
                c = db.query(TableCategories.TABLE, column, selection, selectionArgs, null, null, null);
                break;

            case 4:
                c=db.query(TableCategories.TABLE, null, null, null, null, null, null);
                break;
        }
        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int code = uriMatcher.match(uri);
        long newRowId = -1;
        if (code == 1) {
            newRowId = dbHelper.getWritableDatabase().insert(TABLE, null, values);
        }
        if (code == 4){
            newRowId = dbHelper.getWritableDatabase().insert(TableCategories.TABLE, null, values);
        }
        if(newRowId != -1){
            return Uri.withAppendedPath(PRODUCTS_URI, String.valueOf(newRowId));
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int code = uriMatcher.match(uri);
        if (code == 1) {
            return dbHelper.getWritableDatabase().delete(TABLE, selection, selectionArgs);
        } else if (code == 2){
            long id = ContentUris.parseId(uri);
            selection = _ID + "=?";
            selectionArgs = new String[]{String.valueOf(id)};
            return dbHelper.getWritableDatabase().delete(TABLE, selection, selectionArgs);
        }
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int code = uriMatcher.match(uri);
        if (code == 1) {
            return dbHelper.getWritableDatabase().update(TABLE, values, selection, selectionArgs);
        } else if (code == 2){
            long id = ContentUris.parseId(uri);
            selection = _ID + "=?";
            selectionArgs = new String[]{String.valueOf(id)};
            return dbHelper.getWritableDatabase().update(TABLE, values, selection, selectionArgs);
        }
        return 0;
    }
}
