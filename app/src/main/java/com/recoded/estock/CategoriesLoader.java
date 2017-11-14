/*
 * Created by Wisam Naji on 11/14/17 5:00 AM.
 * Copyright (c) 2017. All rights reserved.
 * Copying, redistribution or usage of material used in this file is free for educational purposes ONLY and should not be used in profitable context.
 *
 * Last modified on 11/14/17 5:00 AM
 */

package com.recoded.estock;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;

import static com.recoded.estock.data.ProductsHelper.TableCategories.CATEGORIES_URI;

/**
 * Created by wisam on Nov 14 17.
 */

public class CategoriesLoader extends AsyncTaskLoader<ArrayList[]> {
    public CategoriesLoader(Context context) {
        super(context);
    }

    @Override
    public ArrayList[] loadInBackground() {
        ArrayList<Long> catsIds = new ArrayList<>();
        ArrayList<String> cats = new ArrayList<>();

        catsIds.add(-1L);
        cats.add("Select Category");
        Cursor c = getContext().getContentResolver().query(CATEGORIES_URI, null, null, null, null);
        while (c != null && c.moveToNext()) {
            catsIds.add(c.getLong(0));
            cats.add(c.getString(1));
        }
        catsIds.add(-2L);
        cats.add("<New Category>");
        if (c != null) {
            c.close();
        }
        return new ArrayList[]{catsIds, cats};
    }
}
