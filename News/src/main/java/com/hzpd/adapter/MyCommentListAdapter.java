package com.hzpd.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.custorm.CircleImageView;
import com.hzpd.custorm.PopUpwindowLayout;
import com.hzpd.hflt.R;
import com.hzpd.modle.MyCommentListBean;
import com.hzpd.ui.activity.XF_PInfoActivity;
import com.hzpd.ui.activity.ZQ_ReplyCommentActivity;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AvoidOnClickFastUtils;
import com.hzpd.utils.CalendarUtil;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuqing on 2015/8/13.
 */
public class MyCommentListAdapter extends RecyclerView.Adapter {


    private List<MyCommentListBean> mDataList;
    private SPUtil spu;
    private HttpUtils httpUtils;
    private Context context;
    private LayoutInflater mInflater;
    final static int TYPE_NORMAL = 0xCC;
    final static int TYPE_LOADING = 0xDD;
    public boolean showLoading = false;

    public void setShowLoading(boolean showLoading) {
        this.showLoading = showLoading;
        notifyDataSetChanged();
    }

    public MyCommentListAdapter(Context context) {
        spu = SPUtil.getInstance();
        httpUtils = SPUtil.getHttpUtils();
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        mDataList = new ArrayList<>();
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
        int count = mDataList.size();
        if (showLoading) {
            count = count + 1;
        }
        return count;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_NORMAL) {
            View convertView = mInflater.inflate(R.layout.details_comment_list_item, parent, false);
            ItemViewHolder holder = new ItemViewHolder(convertView);
            holder.comment_user_icon = (CircleImageView) convertView.findViewById(R.id.comment_user_icon);
            holder.comment_user_name = (TextView) convertView.findViewById(R.id.comment_user_name);
            holder.comment_text = (TextView) convertView.findViewById(R.id.comment_text);
            holder.comment_time = (TextView) convertView.findViewById(R.id.comment_time);
            holder.up_icon = (ImageView) convertView.findViewById(R.id.up_icon);
            holder.comment_up_num = (TextView) convertView.findViewById(R.id.comment_up_num);
            holder.rl_comment_text = convertView.findViewById(R.id.rl_comment_text);
            holder.line = convertView.findViewById(R.id.line);
            return holder;
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
            case TYPE_NORMAL: {
                final MyCommentListBean item = mDataList.get(position);
                final ItemViewHolder viewHolder = (ItemViewHolder) holder;
//                if (position == 1) {
//                    viewHolder.line.setVisibility(View.GONE);
//                }

                viewHolder.userId = item.getUid();
                // 显示头像

                SPUtil.displayImage(item.getAvatar_path()
                        , viewHolder.comment_user_icon
                        , DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.XF_Avatar));
                viewHolder.comment_user_icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent mIntent = new Intent();
                        mIntent.putExtra("uid", item.getUid()); //TODO
                        mIntent.setClass(context, XF_PInfoActivity.class);
                        context.startActivity(mIntent);
                    }
                });

                // 用户名
                viewHolder.comment_user_name.setText(item.getNickname());
                viewHolder.comment_user_name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent mIntent = new Intent();
                        mIntent.putExtra("uid", item.getUid()); //TODO
                        mIntent.setClass(context, XF_PInfoActivity.class);
                        context.startActivity(mIntent);
                    }
                });

                // 评论内容
                viewHolder.comment_text.setText(item.getContent());
//            holder.rl_comment_text.setOnClickListener(new View.OnClickListener() {
                viewHolder.comment_text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (AvoidOnClickFastUtils.isFastDoubleClick(v)) {
                            return;
                        }
                        ArrayList<String> titles = new ArrayList<String>();
                        titles.add(context.getResources().getString(R.string.reply_comment));
//                titles.add("删除");
                        View view = mInflater.inflate(R.layout.layout_popupwindow, null);
                        PopUpwindowLayout popUpwindowLayout = (PopUpwindowLayout) view.findViewById(R.id.llayout_popupwindow);
                        popUpwindowLayout.initViews(context, titles, false);
                        final PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                        int popupWidth = view.getMeasuredWidth();
                        int popupHeight = view.getMeasuredHeight();
                        int[] location = new int[2];
                        // 允许点击外部消失
                        popupWindow.setBackgroundDrawable(new BitmapDrawable());
                        popupWindow.setOutsideTouchable(true);
                        popupWindow.setFocusable(true);
                        // 获得位置
                        v.getLocationOnScreen(location);
                        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
                        popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, (location[0] + v.getWidth() / 2) - popupWidth / 2, location[1] - popupHeight);
                        popUpwindowLayout.setClickListener(new PopUpwindowLayout.OnClickCallback() {

                            @Override
                            public void onItemClick(LinearLayout parentView, int size, int index) {
                                switch (index) {
                                    case 0:
//                                    Toast.makeText(parent.getContext(), "回复评论", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(context, ZQ_ReplyCommentActivity.class);
                                        intent.putExtra("USER_UID", item.getCid());
                                        context.startActivity(intent);
                                        popupWindow.dismiss();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });

                    }
                });


                // 评论时间
                viewHolder.comment_time.setText(CalendarUtil.friendlyTime1(item.getDateline(), context));


                if (SharePreferecesUtils.getParam(context, "" + item.getCid(), "0").toString().equals("1")) {
                    viewHolder.up_icon.setImageResource(R.drawable.details_icon_likeit);
                    viewHolder.up_icon.setEnabled(false);
                }

                // 点赞数
                viewHolder.comment_up_num.setText(item.getPraise());
                viewHolder.up_icon.setTag(item);
                viewHolder.up_icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            viewHolder.up_icon.setEnabled(false);
                            Log.e("holder.digNum", "holder.digNum");
                            if (null == spu.getUser()) {
                                return;
                            }
                            Log.e("test", "点赞" + item.getCid());
                            Log.i(getLogTag(), "uid-" + spu.getUser().getUid() + "  mType-News" + " nid-" + item.getCid());
                            final RequestParams params = RequestParamsUtils.getParamsWithU();
                            params.addBodyParameter("uid", spu.getUser().getUid());
                            params.addBodyParameter("type", "News");
                            params.addBodyParameter("nid", item.getCid());
                            params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
                            SPUtil.addParams(params);
                            httpUtils.send(HttpRequest.HttpMethod.POST
                                    , InterfaceJsonfile.PRISE1//InterfaceApi.mPraise
                                    , params
                                    , new RequestCallBack<String>() {
                                @Override
                                public void onFailure(HttpException arg0, String arg1) {
                                    Log.e(getLogTag(), "赞failed!");
                                    TUtils.toast(context.getString(R.string.toast_server_no_response));
                                    viewHolder.up_icon.setEnabled(true);
                                }

                                @Override
                                public void onSuccess(ResponseInfo<String> arg0) {
                                    Log.d(getLogTag(), "赞-->" + arg0.result);
                                    JSONObject obj = JSONObject.parseObject(arg0.result);

                                    if (200 == obj.getInteger("code")) {
                                        Log.e("", "m---->" + item.getPraise());
                                        SharePreferecesUtils.setParam(context, "" + item.getCid(), "1");
                                        if (TextUtils.isDigitsOnly(item.getPraise())) {
                                            viewHolder.up_icon.setImageResource(R.drawable.details_icon_likeit);
                                            int i = Integer.parseInt(item.getPraise());
                                            i++;
                                            LogUtils.i("i---->" + i);
                                            viewHolder.comment_up_num.setText(i + "");
                                            item.setPraise(i + "");
                                            notifyDataSetChanged();
                                            viewHolder.up_icon.setEnabled(false);
                                        }
                                    } else {
                                        viewHolder.up_icon.setEnabled(true);
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            break;
            case TYPE_LOADING: {

            }
            break;
        }


    }


    public void appendData(List<MyCommentListBean> data) {
        if (data != null && !data.isEmpty()) {
            mDataList.addAll(data);
            notifyDataSetChanged();
        }
    }

    public void appendData(List<MyCommentListBean> data, boolean isClearOld) {
        if (isClearOld) {
            mDataList.clear();
        }
        if (data != null && !data.isEmpty()) {
            mDataList.addAll(data);
            notifyDataSetChanged();
        }
    }

    private void praise(final TextView tv, final MyCommentListBean cb) {
        final Context context = tv.getContext();
        if (null == spu.getUser()) {
            return;
        }
        Log.i(getLogTag(), "uid-" + spu.getUser().getUid() + "  mType-News" + " nid-" + cb.getCid());
        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("uid", spu.getUser().getUid());
        params.addBodyParameter("type", "News");
        params.addBodyParameter("nid", cb.getCid());
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
        SPUtil.addParams(params);
        httpUtils.send(HttpRequest.HttpMethod.POST
                , InterfaceJsonfile.PRISE1//InterfaceApi.mPraise
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException arg0, String arg1) {
                Log.e(getLogTag(), "赞failed!");
                TUtils.toast(context.getString(R.string.toast_server_no_response));
            }

            @Override
            public void onSuccess(ResponseInfo<String> arg0) {
                Log.d(getLogTag(), "赞-->" + arg0.result);
                JSONObject obj = JSONObject.parseObject(arg0.result);
//                TUtils.toast(obj.getString("msg"));

                if (200 == obj.getInteger("code")) {
                    LogUtils.i("m---->" + cb.getPraise());
                    SharePreferecesUtils.setParam(context, "" + cb.getCid(), "1");
                    if (TextUtils.isDigitsOnly(cb.getPraise())) {
                        Drawable img = context.getResources().getDrawable(R.drawable.digupicon_comment_select);
                        img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
                        tv.setCompoundDrawables(null, null, img, null);
                        int i = Integer.parseInt(cb.getPraise());
                        i++;
                        LogUtils.i("i---->" + i);
                        tv.setText(i + "");
                        cb.setPraise(i + "");
                        notifyDataSetChanged();
                    }
                }
            }
        });
    }

    public String getLogTag() {
        return getClass().getSimpleName();
    }


    public class LoadingHolder extends RecyclerView.ViewHolder {
        public LoadingHolder(View itemView) {
            super(itemView);
        }

    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        public String userId = "";
        public CircleImageView comment_user_icon;
        public TextView comment_user_name;
        public TextView comment_text;
        public TextView comment_time;
        public ImageView up_icon;
        private TextView comment_up_num;
        private View rl_comment_text;
        private View line;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }
    }
}
