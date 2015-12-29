package com.hzpd.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.modle.DiscoveryItemBean;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.TagBean;
import com.hzpd.modle.event.TagEvent;
import com.hzpd.ui.App;
import com.hzpd.ui.activity.NewsDetailActivity;
import com.hzpd.ui.activity.TagActivity;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.CalendarUtil;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.LogUtils;
import com.news.update.Utils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class DiscoveryItemNewAdapter extends RecyclerView.Adapter {

    private Context context;
    private LayoutInflater mInflater;
    List<DiscoveryItemBean> list = null;
    final static int TYPE_NORMAL = 0xCC;
    final static int TYPE_LOADING = 0xDD;
    public boolean showLoading = false;
    protected HttpUtils httpUtils;
    private SPUtil spu;

    public void setShowLoading(boolean showLoading) {
        this.showLoading = showLoading;
        notifyDataSetChanged();
    }

    public DiscoveryItemNewAdapter(Context context) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        list = new ArrayList<>();
        httpUtils = SPUtil.getHttpUtils();
        spu = SPUtil.getInstance();
    }

    public void appendData(List<DiscoveryItemBean> data, boolean isClearOld) {
        if (data == null) {
            return;
        }
        if (isClearOld) {
            list.clear();
        }
        list.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (showLoading && position + 1 == getItemCount()) {
            return TYPE_LOADING;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        int count = list.size();
        if (showLoading) {
            count = count + 1;
        }
        return count;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        public ItemViewHolder(View v) {
            super(v);
        }

        public TagBean tagBean;
        ImageView discovery_iv_tag;
        TextView discovery_tag_name;
        TextView tv_subscribe;
        LinearLayout news_ll;
        LinearLayout tag_layout;

    }

    public class LoadingHolder extends RecyclerView.ViewHolder {
        public LoadingHolder(View itemView) {
            super(itemView);
            ViewUtils.inject(this, itemView);
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_NORMAL) {
            View view = mInflater.inflate(R.layout.discovery_item_layout,
                    parent, false);
            ItemViewHolder viewHolder = new ItemViewHolder(view);
            viewHolder.discovery_iv_tag = (ImageView) view.findViewById(R.id.discovery_iv_tag);
            viewHolder.discovery_tag_name = (TextView) view.findViewById(R.id.discovery_tag_name);
            viewHolder.tv_subscribe = (TextView) view.findViewById(R.id.tv_subscribe);
            viewHolder.tv_subscribe.setOnClickListener(onClickListener);
            viewHolder.tag_layout = (LinearLayout) view.findViewById(R.id.tag_layout);
            viewHolder.news_ll = (LinearLayout) view.findViewById(R.id.news_ll);
            return viewHolder;
        } else if (viewType == TYPE_LOADING) {
            View view = mInflater.inflate(R.layout.list_load_more_layout,
                    parent, false);
            LoadingHolder loadingHolder = new LoadingHolder(view);
            return loadingHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type) {
            case TYPE_NORMAL:
                final ItemViewHolder viewHolder = (ItemViewHolder) holder;
                viewHolder.news_ll.removeAllViews();
                final DiscoveryItemBean bean = list.get(position);
                if (bean.getTag() == null || bean.getNews() == null) {
                    return;
                }
                if (!TextUtils.isEmpty(bean.getTag().getName())) {
                    viewHolder.discovery_tag_name.setText(bean.getTag().getName());
                }
                String tagIcon = bean.getTag().getIcon();
                if (tagIcon != null) {
                    SPUtil.displayImage(tagIcon
                            , viewHolder.discovery_iv_tag
                            , DisplayOptionFactory.getOption(OptionTp.Personal_center_News));
                }
                viewHolder.tv_subscribe.setTag(viewHolder);
                viewHolder.tagBean = bean.getTag();

                viewHolder.tag_layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("DiscoveryItemNewAdapter", "DiscoveryItemNewAdapter   viewHolder.tag_layout   onClick");
                        Intent intent = new Intent(context, TagActivity.class);
                        TagBean tagBean = bean.getTag();
                        intent.putExtra("tagbean", tagBean);
                        context.startActivity(intent);

                    }
                });
                if (SPUtil.checkTag(bean.getTag())) {
                    viewHolder.tv_subscribe.setBackgroundResource(R.drawable.corners_bg);
                    viewHolder.tv_subscribe.setTextColor(context.getResources().getColor(R.color.details_tv_check_color));
                    Drawable nav_up = context.getResources().getDrawable(R.drawable.discovery_image_select);
                    nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
                    viewHolder.tv_subscribe.setCompoundDrawables(nav_up, null, null, null);
                    viewHolder.tv_subscribe.setText(context.getString(R.string.discovery_followed));
                    viewHolder.tv_subscribe.setEnabled(false);
                } else {
                    viewHolder.tv_subscribe.setBackgroundResource(R.drawable.discovery_item_corners_bg);
                    viewHolder.tv_subscribe.setTextColor(context.getResources().getColor(R.color.pager_sliding_tab_indicator_color));
                    Drawable nav_up = context.getResources().getDrawable(R.drawable.discovery_image_nor);
                    nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
                    viewHolder.tv_subscribe.setCompoundDrawables(nav_up, null, null, null);
                    viewHolder.tv_subscribe.setText(context.getString(R.string.discovery_follow));
                    viewHolder.tv_subscribe.setEnabled(true);
                }

                for (final NewsBean itembean : bean.getNews()) {
                    if (itembean.getTitle() == null) {
                        return;
                    }
                    View vi = mInflater.inflate(R.layout.news_list_item_layout, null);
                    ImageView newsitem_img = (ImageView) vi.findViewById(R.id.newsitem_img);
                    TextView newsitem_title = (TextView) vi.findViewById(R.id.newsitem_title);
                    TextView newsitem_time = (TextView) vi.findViewById(R.id.newsitem_time);
                    LinearLayout ll_tag = (LinearLayout) vi.findViewById(R.id.ll_tag);
                    if (CalendarUtil.friendlyTime(itembean.getUpdate_time(), context) == null) {
                        newsitem_time.setText(" ");
                    } else {
                        newsitem_time.setText(CalendarUtil.friendlyTime(itembean.getUpdate_time(), context));
                    }

                    TypedValue typedValue = new TypedValue();
                    context.getTheme().resolveAttribute(R.attr.item_title, typedValue, true);
                    int color = typedValue.data;
                    newsitem_title.setTextColor(color);
                    newsitem_title.setText(itembean.getTitle());
                    if (null != itembean.getImgs()
                            && itembean.getImgs().length > 0) {
                        newsitem_title.setPadding(App.px_15dp, 0, 0, 0);
                        ll_tag.setPadding(App.px_15dp, 0, 0, 0);
                        SPUtil.displayImage(itembean.getImgs()[0], newsitem_img,
                                DisplayOptionFactory.getOption(OptionTp.Small));
                    } else {
                        newsitem_img.setVisibility(View.GONE);
                        newsitem_title.setPadding(0, 0, 0, App.px_15dp);
                        ll_tag.setPadding(0, 0, 0, 0);
                    }
                    TextView newsitem_collectcount = (TextView) vi.findViewById(R.id.newsitem_collectcount);
                    String fav = itembean.getFav();
                    if (!TextUtils.isEmpty(fav)) {
                        int fav_counts = Integer.parseInt(fav);
                        if (fav_counts > 0) {
                            newsitem_collectcount.setVisibility(View.VISIBLE);
                            newsitem_collectcount.setText(fav_counts + "");
                        } else {
                            newsitem_collectcount.setVisibility(View.GONE);
                        }
                    } else {
                        newsitem_collectcount.setVisibility(View.GONE);
                    }
                    TextView newsitem_source = (TextView) vi.findViewById(R.id.newsitem_source);
                    String from = itembean.getCopyfrom();
                    if (!TextUtils.isEmpty(from)) {
                        newsitem_source.setVisibility(View.VISIBLE);
                        newsitem_source.setText(from);
                    } else {
                        newsitem_source.setVisibility(View.GONE);
                    }
                    TextView newsitem_commentcount = (TextView) vi.findViewById(R.id.newsitem_commentcount);
                    String comcount = itembean.getComcount();
                    if (!TextUtils.isEmpty(comcount)) {
                        int counts = Integer.parseInt(comcount);
                        if (counts > 0) {
                            newsitem_commentcount.setVisibility(View.VISIBLE);
                            itembean.setComcount(counts + "");
                            newsitem_commentcount.setText(counts + "");
                        } else {
                            newsitem_commentcount.setVisibility(View.GONE);
                        }
                    } else {
                        newsitem_commentcount.setVisibility(View.GONE);
                    }


                    vi.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            Toast.makeText(context, "" + itembean.getNid(), Toast.LENGTH_SHORT).show();
                            Intent mIntent = new Intent(context, NewsDetailActivity.class);
                            mIntent.putExtra("newbean", itembean);
                            mIntent.putExtra("from", "newsitem");
                            context.startActivity(mIntent);
                        }
                    });

                    viewHolder.news_ll.addView(vi);
                }
                break;
            case TYPE_LOADING:


                break;

        }

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.i("DiscoveryItemNewAdapter", "DiscoveryItemNewAdapter  viewHolder.tv_subscribe  onClick");
            try {
                if (view.getTag() instanceof ItemViewHolder) {
                    ItemViewHolder viewHolder = (ItemViewHolder) view.getTag();
                    viewHolder.tv_subscribe.setBackgroundResource(R.drawable.corners_bg);
                    viewHolder.tv_subscribe.setTextColor(context.getResources().getColor(R.color.details_tv_check_color));
                    Drawable nav_up = context.getResources().getDrawable(R.drawable.discovery_image_select);
                    nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
                    viewHolder.tv_subscribe.setCompoundDrawables(nav_up, null, null, null);
                    viewHolder.tv_subscribe.setText(context.getString(R.string.discovery_followed));
                    EventBus.getDefault().post(new TagEvent(viewHolder.tagBean));
                    if (Utils.isNetworkConnected(context)) {
                        RequestParams params = RequestParamsUtils.getParamsWithU();
                        if (spu.getUser() != null) {
                            params.addBodyParameter("uid", spu.getUser().getUid() + "");
                        }
                        params.addBodyParameter("tagId", viewHolder.tagBean.getId() + "");
                        SPUtil.addParams(params);

                        httpUtils.send(HttpRequest.HttpMethod.POST, InterfaceJsonfile.tag_click_url, params, new RequestCallBack<String>() {
                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                JSONObject obj = null;
                                try {
                                    obj = JSONObject.parseObject(responseInfo.result);
                                } catch (Exception e) {
                                    return;
                                }
                                if (200 == obj.getIntValue("code")) {

                                }
                            }

                            @Override
                            public void onFailure(HttpException error, String msg) {
                                LogUtils.i("isCollection failed");
                            }
                        });
                    }
                    Log.i("viewHolder.tv_subscribe", "viewHolder.tv_subscribe");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}