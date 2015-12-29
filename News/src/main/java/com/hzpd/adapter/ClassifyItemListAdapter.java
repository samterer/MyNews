package com.hzpd.adapter;import android.content.Context;import android.content.Intent;import android.graphics.drawable.Drawable;import android.support.v7.widget.RecyclerView;import android.view.LayoutInflater;import android.view.View;import android.view.ViewGroup;import android.widget.ImageView;import android.widget.TextView;import com.alibaba.fastjson.JSONObject;import com.hzpd.hflt.R;import com.hzpd.modle.TagBean;import com.hzpd.modle.event.TagEvent;import com.hzpd.ui.activity.TagActivity;import com.hzpd.url.InterfaceJsonfile;import com.hzpd.utils.AvoidOnClickFastUtils;import com.hzpd.utils.DisplayOptionFactory;import com.hzpd.utils.Log;import com.hzpd.utils.RequestParamsUtils;import com.hzpd.utils.SPUtil;import com.lidroid.xutils.HttpUtils;import com.lidroid.xutils.ViewUtils;import com.lidroid.xutils.exception.HttpException;import com.lidroid.xutils.http.RequestParams;import com.lidroid.xutils.http.ResponseInfo;import com.lidroid.xutils.http.callback.RequestCallBack;import com.lidroid.xutils.http.client.HttpRequest;import com.lidroid.xutils.util.LogUtils;import com.news.update.Utils;import java.util.ArrayList;import java.util.List;import de.greenrobot.event.EventBus;public class ClassifyItemListAdapter extends RecyclerView.Adapter {    private LayoutInflater mInflater;    List<TagBean> list = null;    private Context context;    public boolean showLoading = false;    protected HttpUtils httpUtils;    private SPUtil spu;    private View.OnClickListener onClickListener;    public void setShowLoading(boolean showLoading) {        this.showLoading = showLoading;        notifyDataSetChanged();    }    public ClassifyItemListAdapter(Context context, View.OnClickListener onClickListener) {        this.context = context;        this.mInflater = LayoutInflater.from(context);        list = new ArrayList<>();        httpUtils = SPUtil.getHttpUtils();        spu = SPUtil.getInstance();        this.onClickListener = onClickListener;    }    public class ItemViewHolder extends RecyclerView.ViewHolder {        public ItemViewHolder(View v) {            super(v);        }        View classify_item;        ImageView mImg;        TextView mTxt;        TextView mTxt_num;        TextView tv_subscribe;    }    public void appendData(List<TagBean> data, boolean isClearOld) {        if (data == null) {            return;        }        if (isClearOld) {            list.clear();        }        list.addAll(data);        notifyDataSetChanged();    }    final static int TYPE_NORMAL = 0xCC;    final static int TYPE_LOADING = 0xDD;    @Override    public int getItemCount() {        int count = list.size();        if (showLoading) {            count = list.size() + 1;        }        return count;    }    @Override    public int getItemViewType(int position) {        if (showLoading && position + 1 == getItemCount()) {            return TYPE_LOADING;        } else {            return TYPE_NORMAL;        }    }    public class LoadingHolder extends RecyclerView.ViewHolder {        public LoadingHolder(View itemView) {            super(itemView);            ViewUtils.inject(this, itemView);        }    }    @Override    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {        if (viewType == TYPE_NORMAL) {            View view = mInflater.inflate(R.layout.classify_item_list_layout,                    parent, false);            ItemViewHolder viewHolder = new ItemViewHolder(view);            viewHolder.classify_item = view.findViewById(R.id.classify_item);            viewHolder.mImg = (ImageView) view                    .findViewById(R.id.id_index_gallery_item_image);            viewHolder.mTxt = (TextView) view.findViewById(R.id.id_index_gallery_item_text);            viewHolder.mTxt_num = (TextView) view.findViewById(R.id.id_index_gallery_item_num_text);            viewHolder.tv_subscribe = (TextView) view.findViewById(R.id.tv_subscribe);            return viewHolder;        } else if (viewType == TYPE_LOADING) {            View view = mInflater.inflate(R.layout.list_load_more_layout,                    parent, false);            LoadingHolder loadingHolder = new LoadingHolder(view);            return loadingHolder;        }        return null;    }    @Override    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {        int type = getItemViewType(position);        switch (type) {            case TYPE_NORMAL:                final ItemViewHolder viewHolder = (ItemViewHolder) holder;                final TagBean tagBean = list.get(position);                if (tagBean.getName() != null) {                    viewHolder.mTxt.setText(tagBean.getName());                }                if (tagBean.getIcon() != null) {                    SPUtil.displayImage(tagBean.getIcon(), viewHolder.mImg                            , DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Personal_center_News));                }                if (tagBean.getNum() != null) {                    if (Integer.parseInt(tagBean.getNum()) > 1) {                        viewHolder.mTxt_num.setVisibility(View.VISIBLE);                        viewHolder.mTxt_num.setText("" + tagBean.getNum() + "" + context.getResources().getString(R.string.follow_num));                    } else {                        viewHolder.mTxt_num.setVisibility(View.GONE);                    }                }                viewHolder.tv_subscribe.setOnClickListener(new View.OnClickListener() {                    @Override                    public void onClick(View v) {                        viewHolder.tv_subscribe.setBackgroundResource(R.drawable.corners_bg);                        viewHolder.tv_subscribe.setTextColor(context.getResources().getColor(R.color.details_tv_check_color));                        Drawable nav_up = context.getResources().getDrawable(R.drawable.discovery_image_select);                        nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());                        viewHolder.tv_subscribe.setCompoundDrawables(nav_up, null, null, null);                        viewHolder.tv_subscribe.setText(context.getString(R.string.discovery_followed));                        EventBus.getDefault().post(new TagEvent(tagBean));                        if (Utils.isNetworkConnected(context)) {                            RequestParams params = RequestParamsUtils.getParamsWithU();                            if (spu.getUser() != null) {                                params.addBodyParameter("uid", spu.getUser().getUid() + "");                            }                            params.addBodyParameter("tagId", tagBean.getId() + "");                            SPUtil.addParams(params);                            httpUtils.send(HttpRequest.HttpMethod.POST, InterfaceJsonfile.tag_click_url, params, new RequestCallBack<String>() {                                @Override                                public void onSuccess(ResponseInfo<String> responseInfo) {                                    JSONObject obj = null;                                    try {                                        obj = JSONObject.parseObject(responseInfo.result);                                    } catch (Exception e) {                                        return;                                    }                                    if (200 == obj.getIntValue("code")) {                                    }                                }                                @Override                                public void onFailure(HttpException error, String msg) {                                }                            });                        }                    }                });                if (SPUtil.checkTag(tagBean)) {                    viewHolder.tv_subscribe.setBackgroundResource(R.drawable.corners_bg);                    viewHolder.tv_subscribe.setTextColor(context.getResources().getColor(R.color.details_tv_check_color));                    Drawable nav_up = context.getResources().getDrawable(R.drawable.discovery_image_select);                    nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());                    viewHolder.tv_subscribe.setCompoundDrawables(nav_up, null, null, null);                    viewHolder.tv_subscribe.setText(context.getString(R.string.discovery_followed));                } else {                    viewHolder.tv_subscribe.setBackgroundResource(R.drawable.discovery_item_corners_bg);                    viewHolder.tv_subscribe.setTextColor(context.getResources().getColor(R.color.pager_sliding_tab_indicator_color));                    Drawable nav_up = context.getResources().getDrawable(R.drawable.discovery_image_nor);                    nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());                    viewHolder.tv_subscribe.setCompoundDrawables(nav_up, null, null, null);                    viewHolder.tv_subscribe.setText(context.getString(R.string.discovery_follow));                }                viewHolder.classify_item.setOnClickListener(new View.OnClickListener() {                    @Override                    public void onClick(View v) {                        if (AvoidOnClickFastUtils.isFastDoubleClick()) {                            return;                        }//                EventBus.getDefault().post(new ClassifItemEvent("" + tagBean.getId()));//                Toast.makeText(context, "点击了" + tagBean.getId(), Toast.LENGTH_SHORT).show();                        Intent intent = new Intent(context, TagActivity.class);//                        TagBean tagBean=bean.getTag();                        intent.putExtra("tagbean", tagBean);                        context.startActivity(intent);                    }                });                break;            case TYPE_LOADING:                break;        }    }}