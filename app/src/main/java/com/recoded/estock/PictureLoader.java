/*
 * Created by Wisam Naji on 11/14/17 5:26 AM.
 * Copyright (c) 2017. All rights reserved.
 * Copying, redistribution or usage of material used in this file is free for educational purposes ONLY and should not be used in profitable context.
 *
 * Last modified on 11/14/17 5:26 AM
 */

package com.recoded.estock;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

/**
 * Created by wisam on Nov 14 17.
 */

public class PictureLoader extends AsyncTask<Object, Void, Bitmap> {
    private final ImageView image;
    private final String path;

    public PictureLoader(ImageView img, String path) {
        this.image = img;
        this.path = path;
    }

    @Override
    protected Bitmap doInBackground(Object[] o) {
        // Get the dimensions of the View
        int targetW = image.getWidth();
        int targetH = image.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        return BitmapFactory.decodeFile(path, bmOptions);
    }

    @Override
    protected void onPostExecute(Bitmap img) {
        image.setImageBitmap(img);
    }
}
