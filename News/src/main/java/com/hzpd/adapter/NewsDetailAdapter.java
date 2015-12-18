package com.hzpd.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.hzpd.hflt.R;
import com.hzpd.ui.activity.NewsDetailActivity;
import com.hzpd.ui.widget.FixedListView;

/**
 */
public class NewsDetailAdapter extends RecyclerView.Adapter {

    final int TYPE_HEAD = 0x11;
    final int TYPE_WEBVIEW = 0x56;
    final int TYPE_ITEM = 0x121;

    NewsDetailActivity context;

    public NewsDetailAdapter(Context context) {
        this.context = (NewsDetailActivity) context;
    }

    class HeadViewHolder extends RecyclerView.ViewHolder {

        public HeadViewHolder(View itemView) {
            super(itemView);
        }
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
            case TYPE_HEAD:
                View viewHead = LayoutInflater.from(context).inflate(R.layout.details_news_head, parent, false);
                context.setDetailsHead(viewHead);
                viewHolder = new HeadViewHolder(viewHead);
                break;
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
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEAD;
        }
        return position == 1 ? TYPE_WEBVIEW : TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
