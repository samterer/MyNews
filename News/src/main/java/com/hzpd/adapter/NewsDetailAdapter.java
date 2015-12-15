package com.hzpd.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ListView;

import com.hzpd.hflt.R;
import com.hzpd.ui.activity.NewsDetailActivity;
import com.hzpd.ui.widget.FixedListView;

/**
 */
public class NewsDetailAdapter extends RecyclerView.Adapter {

    final int TYPE_WEBVIEW = 0x56;
    final int TYPE_ITEM = 0x121;

    NewsDetailActivity context;

    public NewsDetailAdapter(Context context) {
        this.context = (NewsDetailActivity) context;
    }

    class WebViewHolder extends RecyclerView.ViewHolder {

        public WebViewHolder(View itemView) {
            super(itemView);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        public ItemViewHolder(View itemView) {
            super(itemView);
        }
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case TYPE_WEBVIEW:
                WebView webView = (WebView) LayoutInflater.from(context).inflate(R.layout.webview_layout, parent, false);
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                webView.setLayoutParams(layoutParams);
                (context).setWebview(webView);
                viewHolder = new WebViewHolder(webView);
                break;
            case TYPE_ITEM:
                FixedListView view = new FixedListView(context);
                view.setDividerHeight(0); //  dividerHeight
                context.setListView(view);
                viewHolder = new ItemViewHolder(view);
                break;

        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case TYPE_WEBVIEW:
                break;
            case TYPE_ITEM:
                break;

        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_WEBVIEW : TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
