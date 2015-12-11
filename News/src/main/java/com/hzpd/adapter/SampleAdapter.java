package com.hzpd.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.modle.TagBean;

import java.util.ArrayList;
import java.util.List;

public class SampleAdapter extends RecyclerView.Adapter<ViewHolder> {
    private List<TagBean> list;

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    public List<TagBean> getList() {
        return list;
    }

    public SampleAdapter() {
        list = new ArrayList<TagBean>();
    }

    @Override
    public int getItemCount() {
        return list.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if (holder instanceof ItemViewHolder) {
            if (list.get(position).getName()!=null)
            ((ItemViewHolder) holder).textView.setText(list.get(position).getName());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_text, null);
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            return new ItemViewHolder(view);
        } else if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.footerview, null);
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            return new FooterViewHolder(view);
        }
        return null;
    }

    class FooterViewHolder extends ViewHolder {

        public FooterViewHolder(View view) {
            super(view);
        }

    }

    class ItemViewHolder extends ViewHolder {
        TextView textView;

        public ItemViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.text);
        }

    }

}
