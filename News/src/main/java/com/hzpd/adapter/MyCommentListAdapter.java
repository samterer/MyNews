package com.hzpd.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.modle.MyCommentListBean;
import com.hzpd.ui.activity.ZQ_ReplyCommentActivity;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.CalendarUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
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
public class MyCommentListAdapter extends BaseAdapter {


    private List<MyCommentListBean> mDataList = new ArrayList<>();
    private SPUtil spu;
    private HttpUtils httpUtils;

    public MyCommentListAdapter() {
        spu = SPUtil.getInstance();
        httpUtils = SPUtil.getHttpUtils();
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }


    @Override
    public MyCommentListBean getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        try {
            ViewHolder holder;
            if (convertView == null || convertView.getId() != R.id.my_comments) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.mycomment_list_item, parent, false);
                holder = new ViewHolder();
                holder.avatar = (ImageView) convertView.findViewById(R.id.comment_user_avatar);
                holder.userName = (TextView) convertView.findViewById(R.id.comment_user_name);
                holder.content = (TextView) convertView.findViewById(R.id.comment_content);
                holder.time = (TextView) convertView.findViewById(R.id.comment_tv_time);
                holder.digNum = (TextView) convertView.findViewById(R.id.comment_dig_num);
                holder.iv_replay = (ImageView) convertView.findViewById(R.id.iv_replay);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final  MyCommentListBean item = getItem(position);
            LogUtils.e("" + item.toString());
            holder.userId = item.getUid();
            // 显示头像

            SPUtil.getInstance().displayImage(item.getAvatar_path(), holder.avatar);

            // 用户名
            holder.userName.setText(item.getNickname());


            // 评论内容
            holder.content.setText(item.getContent());

            LogUtils.e("time" + item.getDateline());


            holder.iv_replay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(parent.getContext(), "回复评论", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(parent.getContext(), ZQ_ReplyCommentActivity.class);
                    intent.putExtra("USER_UID", item.getCid());
                    parent.getContext().startActivity(intent);
//                    holder.ll_reply_com.setVisibility(View.VISIBLE);

                }
            });

            // 评论时间
            holder.time.setText(CalendarUtil.friendlyTime1(item.getDateline(), parent.getContext()));

            if (SharePreferecesUtils.getParam(parent.getContext(),""+item.getCid(),"0").toString().equals("1"))
            {
                Drawable img = parent.getContext().getResources().getDrawable(R.drawable.digupicon_comment_select);
                img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
                holder.digNum.setCompoundDrawables(null, null, img, null);
            }

            // 点赞数
            holder.digNum.setText(item.getPraise());
            holder.digNum.setTag(item);
            holder.digNum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        TextView digNum = (TextView) v;
                        MyCommentListBean item = (MyCommentListBean) v.getTag();
                        Log.e("holder.digNum","holder.digNum");
                        praise(digNum, item);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

//            if (position == getCount() - 1) {
//                holder.bottom.setVisibility(View.GONE);
//            } else {
//                holder.bottom.setVisibility(View.VISIBLE);
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return convertView;
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
        String station = SharePreferecesUtils.getParam(context, StationConfig.STATION, "def").toString();
        String siteid = null;
        String prise_url = null;
        if (station.equals(StationConfig.DEF)) {
            siteid = InterfaceJsonfile.SITEID;
            prise_url = InterfaceJsonfile.PRISE1;
        } else if (station.equals(StationConfig.YN)) {
            siteid = InterfaceJsonfile_YN.SITEID;
            prise_url = InterfaceJsonfile_YN.PRISE1;
        } else if (station.equals(StationConfig.TW)) {
            siteid = InterfaceJsonfile_TW.SITEID;
            prise_url = InterfaceJsonfile_TW.PRISE1;
        }
        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("uid", spu.getUser().getUid());
        params.addBodyParameter("type", "News");
        params.addBodyParameter("nid", cb.getCid());
        params.addBodyParameter("siteid", siteid);

        httpUtils.send(HttpRequest.HttpMethod.POST
                , prise_url//InterfaceApi.mPraise
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
                    SharePreferecesUtils.setParam(context,""+cb.getCid(),"1");
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

    public  static class ViewHolder {
        public String userId = "";
        public ImageView avatar;
        public TextView userName;
        public TextView content;
        public TextView time;
        public TextView digNum;
        public ImageView iv_replay;
    }
}
