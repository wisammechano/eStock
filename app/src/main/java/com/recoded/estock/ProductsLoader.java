/*
 * Created by Wisam Naji on 11/14/17 4:16 AM.
 * Copyright (c) 2017. All rights reserved.
 * Copying, redistribution or usage of material used in this file is free for educational purposes ONLY and should not be used in profitable context.
 *
 * Last modified on 11/14/17 4:16 AM
 */

package com.recoded.estock;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.recoded.estock.data.ProductsHelper;

import java.util.ArrayList;

import static com.recoded.estock.data.ProductsHelper.TableProducts.PRODUCTS_URI;

/**
 * Created by wisam on Nov 14 17.
 */

public class ProductsLoader extends AsyncTaskLoader<ArrayList<Product>> {
    public ProductsLoader(Context context) {
        super(context);
    }

    @Override
    public ArrayList<Product> loadInBackground() {
        ArrayList<Product> products = new ArrayList<>();
        //Products cursor
        //Columns indices:
        // _ID -> 0, NAME -> 1, CATEGORY -> 2, PRICE -> 3, QUANTITY -> 4, IMAGE -> 5, DESCRIPTION -> 6

        Cursor pC = getContext().getContentResolver().query(PRODUCTS_URI, null, null, null, null);
        while (pC != null && pC.moveToNext()) {
            Product p = new Product(pC.getInt(0), pC.getString(1),
                    pC.getInt(2),
                    pC.getDouble(3), pC.getInt(4));

            p.setImagePath(pC.getString(5));
            p.setProductDesc(pC.getString(6));

            //Category cursor
            Cursor cC = getContext().getContentResolver().query(Uri.withAppendedPath(ProductsHelper.TableCategories.CATEGORIES_URI, String.valueOf(p.getCategory())), null, null, null, null);
            if (cC != null && cC.moveToFirst()) {
                p.setCatName(cC.getString(0));
                cC.close();
            } else {
                p.setCatName("No Category");
            }

            products.add(p);
        }
        if (pC != null) {
            pC.close();
        }

        return products;
    }
}
