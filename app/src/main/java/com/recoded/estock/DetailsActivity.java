/*
 * Created by Wisam Naji on 11/15/17 2:33 PM.
 * Copyright (c) 2017. All rights reserved.
 * Copying, redistribution or usage of material used in this file is free for educational purposes ONLY and should not be used in profitable context.
 *
 * Last modified on 11/15/17 2:29 PM
 */

package com.recoded.estock;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.recoded.estock.data.ProductsHelper;
import com.recoded.estock.databinding.ActivityDetailsBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.recoded.estock.data.ProductsHelper.TableCategories.CATEGORIES_URI;
import static com.recoded.estock.data.ProductsHelper.TableProducts.PRODUCTS_URI;
import static com.recoded.estock.data.ProductsHelper.TableProducts.createProductValues;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList[]> {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ActivityDetailsBinding binding;
    Product product = null;
    ArrayList<Long> catsIds;
    ArrayList<String> cats;
    ArrayAdapter<String> catsAdapter;
    boolean editMode;
    boolean validRequiredValue, formNotChanged, formEmpty;
    String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_details);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        binding.productImage.setTag(""); //Prevent NullPointerException
        imagePath = "";
        if (getIntent().hasExtra("product")) {
            product = getIntent().getParcelableExtra("product");
            editMode = true;
            binding.setProduct(product);
            if (!product.getImagePath().isEmpty()) {
                //Implementing runnable because view.getWidth() returns 0 because it has not been laid out yet
                binding.productImage.post(new Runnable() {
                    @Override
                    public void run() {
                        setPic(binding.productImage, product.getImagePath());
                    }
                });
            }
            binding.addProductButton.setText(R.string.update);
            binding.deleteProductButton.setVisibility(View.VISIBLE);
        } else {
            product = new Product();
            binding.setProduct(product);
            editMode = false;
        }
        fetchCategories();

        View.OnClickListener quantityButtons = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentQuantity = Integer.parseInt(binding.productQuantity.getText().toString());
                if (v.getId() == R.id.quantity_inc_button) {
                    currentQuantity++;
                } else {
                    if (currentQuantity != 0) {
                        currentQuantity--;
                    }
                }
                binding.productQuantity.setText(String.valueOf(currentQuantity));
            }
        };

        binding.quantityIncButton.setOnClickListener(quantityButtons);
        binding.quantityDecButton.setOnClickListener(quantityButtons);
        if (editMode) {
            binding.orderMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "supplier@gmail.com", null));
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Order: More items of " + product.getProductName());
                    intent.putExtra(Intent.EXTRA_TEXT, "Hello Sir,\n\nI would like to order more items of the product in the details:\n\nProduct Name: " + product.getProductName() + "\nProduct Price: " + product.getPrice() + "\n Quantity: 100\n\nRegards.");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(Intent.createChooser(intent, "Order More"));
                    }
                }
            });
            binding.orderMore.setVisibility(View.VISIBLE);
        }

        binding.productImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchCameraIntent();
            }
        });

        binding.addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkForm(true);
                if (editMode) {
                    if (formNotChanged) {
                        Toast.makeText(DetailsActivity.this, "No change to update!", Toast.LENGTH_LONG).show();
                        return;
                    } else if (validRequiredValue) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(DetailsActivity.this);
                        dialog.setTitle("Overwrite product?");
                        dialog.setMessage("Are you sure you want to update this entry? Current data will be lost!");
                        dialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri uri = Uri.withAppendedPath(PRODUCTS_URI, "/" + product.getStockId());
                                getContentResolver().update(uri, createProductValues(createNewProductFromForm()), null, null);
                                DetailsActivity.this.finish();
                            }
                        });
                        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        dialog.create().show();
                    }
                } else {
                    if (!validRequiredValue) {
                        Toast.makeText(DetailsActivity.this, "Required fields are empty!", Toast.LENGTH_LONG).show();
                        return;
                    }
                    getContentResolver().insert(PRODUCTS_URI, createProductValues(createNewProductFromForm()));
                    DetailsActivity.this.finish();
                }

            }
        });

        binding.cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                checkForm(false);
                if (editMode) {
                    if (formNotChanged) {
                        DetailsActivity.this.finish();
                        return;
                    }
                } else {
                    if (formEmpty) {
                        DetailsActivity.this.finish();
                        return;
                    }
                }
                AlertDialog.Builder dialog = new AlertDialog.Builder(DetailsActivity.this);
                dialog.setTitle("Cancel");
                dialog.setMessage("Product won't be saved. Sure?");
                dialog.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DetailsActivity.this.finish();
                    }
                });
                dialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        binding.addProductButton.callOnClick();
                    }
                });
                dialog.create().show();
            }
        });

        binding.deleteProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(DetailsActivity.this);
                dialog.setTitle("Delete product");
                dialog.setMessage("Are you sure you want to delete this product from database?");
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.withAppendedPath(PRODUCTS_URI, "/" + product.getStockId());
                        getContentResolver().delete(uri, null, null);
                        DetailsActivity.this.finish();
                    }
                });
                dialog.create().show();
            }
        });
    }

    private void checkForm(boolean validate) {
        /*
        These booleans are used for validation, needless update and unwanted alert dialogs.
        For instance, if the user came to edit a product but didn't change any thing, there is no need to update the database again with same values.
        Also, if he pressed back or cancel, there is no need to alert him about losing his edits.
        An extra is validation in edit mode: Like if the user edited the product and set the name to empty which is required to be filled.

        Add a product mode, if the form is empty and no fields were filled and the user pressed back or cancel, it will go back without alerting.
        However if he filled any of the fields the app will ask him for confirmation onBackPressed.
        If he added details and there were nulls or empty values in the required fields ONLY, the app will refuse to add the product.

        requird fields are: @Name, @price and @quantity and @Image.
        optional fields: @Description.
         */
        formNotChanged = validRequiredValue = formEmpty = true;

        //Check product name != null
        if (binding.productName.getText().toString().isEmpty()) {

            validRequiredValue = false;
            if (validate) binding.productName.setError("Required");

        } else if (editMode) { //Checks whether the form has been altered
            if (!binding.productName.getText().toString().equals(product.getProductName())) {
                formNotChanged = false;
            }
        } else formEmpty = false; //empty form

        //Check product price != null
        if (binding.productPrice.getText().toString().isEmpty()
                || binding.productPrice.getText().toString().equals("0.0")) {

            validRequiredValue = false;
            if (validate) binding.productPrice.setError("Required");

        } else if (editMode && formNotChanged) {
            if (!binding.productPrice.getText().toString().equals(String.valueOf(product.getPriceD()))) {

                formNotChanged = false;
            }
        } else formEmpty = false;

        //Check product quantity != null
        if (binding.productQuantity.getText().toString().isEmpty()
                || binding.productQuantity.getText().toString().equals("0")) {

            validRequiredValue = false;
            if (validate) binding.productQuantity.setError("Required");

        } else if (editMode && formNotChanged) {
            if (!binding.productQuantity.getText().toString().equals(String.valueOf(product.getQuantity()))) {

                formNotChanged = false;
            }
        } else formEmpty = false;

        //Check product category != null
        if (binding.categorySelector.getSelectedItemId() == -1L) {

            validRequiredValue = false;
            if (validate) binding.categorySelector.setBackgroundResource(R.color.invalid);

        } else if (editMode && formNotChanged) {
            if (binding.categorySelector.getSelectedItemId() != product.getCategory()) {

                formNotChanged = false;
            }
        } else formEmpty = false;

        //Check product image != null
        if (binding.productImage.getTag().toString().isEmpty()) {

            validRequiredValue = false;
            if (validate) binding.productImage.setImageResource(R.drawable.image_placeholder_req);

        } else if (editMode && formNotChanged) {
            if (!binding.productImage.getTag().toString().equals(product.getImagePath())) {

                formNotChanged = false;
            }
        } else formEmpty = false;

        //check whether form has been altered for non required details
        if (editMode && formNotChanged) {
            if (!binding.productDesc.getText().toString().equals(product.getProductDesc())) {
                formNotChanged = false;
            }

        } else {
            if (!binding.productDesc.getText().toString().isEmpty()) {
                formEmpty = false;
            }

        }
    }


    private Product createNewProductFromForm() {
        Product product = new Product();
        product.setProductName(binding.productName.getText().toString());
        product.setProductDesc(binding.productDesc.getText().toString());
        product.setCategory(binding.categorySelector.getSelectedItemId());
        product.setPrice(Double.parseDouble(binding.productPrice.getText().toString()));
        product.setQuantity(Integer.parseInt(binding.productQuantity.getText().toString()));
        product.setImagePath(binding.productImage.getTag().toString());
        return product;
    }

    //Decrease memory usage
    private void setPic(ImageView mImageView, String imagePath) {
        mImageView.setTag(imagePath);
        new PictureLoader(mImageView, imagePath).execute();
    }

    private void dispatchCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getPackageName().concat(".fileprovider"),
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Product_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        imagePath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPic(binding.productImage, imagePath);
        } else {
            imagePath = "";
        }
    }

    @Override
    public void onBackPressed() {
        binding.cancelButton.callOnClick();
    }

    private void fetchCategories() {
        binding.categorySelector.setEnabled(false);
        getSupportLoaderManager().initLoader(0, null, this).forceLoad();
    }

    private void setCategories() {
        if (cats.isEmpty()) return;
        binding.categorySelector.setEnabled(true);
        catsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cats) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public long getItemId(int position) {
                return catsIds.get(position);
            }
        };
        catsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.categorySelector.setAdapter(catsAdapter);

        if (editMode) {
            binding.categorySelector.setSelection(catsIds.indexOf(product.getCategory()));
        }

        binding.categorySelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (id == -2L) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(DetailsActivity.this);
                    dialog.setTitle("Category Name");
                    final View layout = LayoutInflater.from(DetailsActivity.this).inflate(R.layout.add_cat_dialog, null);
                    dialog.setView(layout);
                    dialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText edit = layout.findViewById(R.id.cat_name_et);
                            String catName = edit.getText().toString();
                            dialog.dismiss();
                            ContentValues cv = new ContentValues();
                            cv.put(ProductsHelper.TableCategories.NAME, catName);
                            getContentResolver().insert(CATEGORIES_URI, cv);
                            fetchCategories();
                            binding.categorySelector.setSelection(catsIds.size() - 2);
                        }
                    });
                    dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    dialog.create().show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public Loader<ArrayList[]> onCreateLoader(int id, Bundle args) {
        return new CategoriesLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList[]> loader, ArrayList[] data) {
        catsIds = data[0];
        cats = data[1];
        setCategories();
    }

    @Override
    public void onLoaderReset(Loader<ArrayList[]> loader) {

    }
}
