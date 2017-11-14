/*
 * Created by Wisam Naji on 11/7/17 2:55 AM.
 * Copyright (c) 2017. All rights reserved.
 * Copying, redistribution or usage of material used in this file is free for educational purposes ONLY and should not be used in profitable context.
 *
 * Last modified on 11/7/17 2:54 AM
 */

package com.recoded.estock;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.recoded.estock.databinding.ActivityMainBinding;

import java.util.ArrayList;

import static com.recoded.estock.data.ProductsHelper.TableProducts.PRODUCTS_URI;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Product>> {
    ActivityMainBinding binding;
    ArrayList<Product> products;
    ProductsAdapter adapter;
    Menu menu;
    ArrayList<Product> deleteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        products = new ArrayList<>();
        deleteList = new ArrayList<>();
        adapter = new ProductsAdapter(this, products);
        adapter.setHasStableIds(true);
        binding.productsList.setLayoutManager(new LinearLayoutManager(this));
        binding.productsList.setAdapter(adapter);
        binding.addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DetailsActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.resetDeleteList();
        adapter.setShowCheckboxes(false);
        populateList();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putParcelableArrayList("products", products);
    }

    private void populateList() {
        products.clear();
        getSupportLoaderManager().initLoader(0, null, this).forceLoad();
    }

    private void toggleEmptyView() {
        if (adapter.getItemCount() == 0) {
            binding.emptyView.setVisibility(View.VISIBLE);
            binding.productsList.setVisibility(View.GONE);
        } else {
            binding.emptyView.setVisibility(View.GONE);
            binding.productsList.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        this.menu = menu;
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                adapter.cancelFilter();
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_add:
                startActivity(new Intent(this, DetailsActivity.class));
                return true;
            case R.id.app_bar_delete:
                deleteList = adapter.getDeleteList();
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                StringBuilder st = new StringBuilder("Are you sure you want to delete these products from database?");
                st.append("\n");
                int i = 0;
                for (Product p : deleteList) {
                    if (i == 4) {
                        st.append("\n");
                        st.append("... and ");
                        st.append(deleteList.size() - 4);
                        st.append(" items more.");
                        break;
                    }
                    st.append(p.getStockId());
                    st.append(" - ");
                    st.append(p.getProductName());
                    st.append("\n");
                    i++;
                }
                dialog.setMessage(st.toString());
                dialog.setTitle("Delete " + deleteList.size() + " products?");
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (Product p : deleteList) {
                            getContentResolver().delete(Uri.withAppendedPath(PRODUCTS_URI, "/" + p.getStockId()), null, null);
                        }
                        Toast.makeText(MainActivity.this, deleteList.size() + " items deleted successfully!", Toast.LENGTH_LONG).show();
                        adapter.resetDeleteList();
                        dialog.dismiss();
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.create().show();
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public Loader<ArrayList<Product>> onCreateLoader(int id, Bundle args) {
        return new ProductsLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Product>> loader, ArrayList<Product> products) {
        this.products = products;
        if (!products.isEmpty()) {
            adapter.setDataSet(products);
        }

        toggleEmptyView();
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Product>> loader) {

    }
}
