/*
 * Created by Wisam Naji on 11/7/17 6:19 AM.
 * Copyright (c) 2017. All rights reserved.
 * Copying, redistribution or usage of material used in this file is free for educational purposes ONLY and should not be used in profitable context.
 *
 * Last modified on 11/7/17 6:19 AM
 */

package com.recoded.estock;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.recoded.estock.databinding.ProductItemBinding;

import java.util.ArrayList;
import java.util.List;

import static com.recoded.estock.data.ProductsHelper.TableProducts.PRODUCTS_URI;
import static com.recoded.estock.data.ProductsHelper.TableProducts.createProductValues;

/**
 * Created by wisam on Nov 7 17.
 */

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.MyViewHolder> {
    private Context ctx;
    private List<Product> dataSet;
    private List<Product> backUpData;
    private boolean isBackUped;

    private boolean showCheckboxes;
    private ArrayList<Product> deleteList;
    private View.OnClickListener addProductListener;
    private View.OnClickListener checkBoxSetListener;

    public void setShowCheckboxes(boolean showCheckboxes) {
        this.showCheckboxes = showCheckboxes;
    }

    public ArrayList<Product> getDeleteList() {
        return deleteList;
    }

    public void resetDeleteList() {
        deleteList.clear();
    }

    ProductsAdapter(Context c, List<Product> list) {
        ctx = c;
        dataSet = list;
        deleteList = new ArrayList<>();
        isBackUped = false;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.product_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Product p = dataSet.get(position);
        holder.binding.saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                p.sell();
                notifyDataSetChanged();
                Uri updateUri = Uri.withAppendedPath(PRODUCTS_URI, "/" + p.getStockId());
                ctx.getContentResolver().update(updateUri,
                        createProductValues(p), null, null);
            }
        });

        holder.binding.selectBox.setTag(p);

        holder.binding.selectBox.setChecked(deleteList.contains(holder.binding.selectBox.getTag()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showCheckboxes) {
                    holder.binding.selectBox.setChecked(!holder.binding.selectBox.isChecked());
                } else {
                    Intent intent = new Intent(ctx, DetailsActivity.class);
                    intent.putExtra("product", p);
                    ctx.startActivity(intent);
                }
            }
        });

        holder.setProduct(dataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    void setDataSet(List<Product> dataSet) {
        this.dataSet = dataSet;
        if (!isBackUped) {
            backUpData = dataSet;
            isBackUped = true;
        }
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (!isBackUped) return;
        cancelFilter();
        if(query.isEmpty()) return;
        query = query.toLowerCase();
        List<Product> filteredData = new ArrayList<>();
        for (Product p : dataSet) {
            if (p.getProductName().toLowerCase().contains(query) || p.getCatName().toLowerCase().contains(query) || p.getProductDesc().toLowerCase().contains(query)) {
                filteredData.add(p);
            }
        }
        setDataSet(filteredData);
    }

    public void cancelFilter() {
        setDataSet(backUpData);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ProductItemBinding binding;

        MyViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!showCheckboxes) {
                        showCheckboxes = true;
                        notifyDataSetChanged();
                        ((MainActivity) ctx).menu.getItem(0).setVisible(false);
                        ((MainActivity) ctx).menu.getItem(1).setVisible(true);
                        ((MainActivity) ctx).menu.getItem(2).setVisible(false);

                        return true;
                    } else {
                        showCheckboxes = false;
                        resetDeleteList();
                        notifyDataSetChanged();
                        ((MainActivity) ctx).menu.getItem(0).setVisible(true);
                        ((MainActivity) ctx).menu.getItem(1).setVisible(false);
                        ((MainActivity) ctx).menu.getItem(2).setVisible(true);
                        return true;
                    }
                }
            });

            binding.selectBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        deleteList.add((Product) buttonView.getTag());
                    } else {
                        deleteList.remove(buttonView.getTag());
                    }
                }
            });
        }

        void setProduct(Product p) {
            binding.setProduct(p);
            binding.setShowCheckbox(showCheckboxes);
        }
    }
}
