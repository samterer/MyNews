package com.hzpd.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.custorm.CircleImageView;
import com.hzpd.custorm.PopUpwindowLayout;
import com.hzpd.hflt.R;
import com.hzpd.modle.CommentzqzxBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.ui.activity.XF_PInfoActivity;
import com.hzpd.ui.activity.ZQ_ReplyCommentActivity;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.CalendarUtil;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
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
public class CommentListAdapter extends BaseAdapter {

    private List<CommentzqzxBean> mDataList = new ArrayList<>();
    private SPUtil spu;
    private HttpUtils httpUtils;
    private String nid;

    public CommentListAdapter() {
        spu = SPUtil.getInstance();
        httpUtils = new HttpUtils();
    }

    public CommentListAdapter(String nid) {
        spu = SPUtil.getInstance();
        httpUtils = new HttpUtils();
        this.nid = nid;
    }

    @Override
    public int getCount() {
        return mDataList.isEmpty() ? 0 : (mDataList.size() + 1);
    }


    @Override
    public CommentzqzxBean getItem(int position) {
        if (position == 0) {
            return null;
        } else {
            return mDataList.get(position - 1);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        if (position == 0) {
            if (convertView == null || convertView.getId() != R.id.details_related_comments) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.details_related_comments, parent, false);
                TextView comcount_adapter_tv = (TextView) convertView.findViewById(R.id.comcount_adapter_tv);
                try {
                    DBHelper dbHelper;
                    NewsBeanDB nbfc = DBHelper.getInstance(parent.getContext()).getNewsListDbUtils().findFirst(
                            Selector.from(NewsBeanDB.class).where("nid", "=", nid));
                    if (null != nbfc) {
//                        com.hzpd.utils.Log.e("NewsBeanDB", "NewsBeanDB--->" + nbfc.getFav() + "::::" + nbfc.getComcount());
                        comcount_adapter_tv.setText("("+nbfc.getComcount()+")");
//                        if (Integer.parseInt(nbfc.getComcount()) > Integer.parseInt(comcount)) {
//                            comcount = nbfc.getComcount();
//                        }
                    } else {
                    }

                } catch (DbException e) {
                    e.printStackTrace();
                }

            }
        } else {
            final ViewHolder holder;
            if (convertView == null || convertView.getId() != R.id.root_layout) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.details_comment_list_item, parent, false);
                holder = new ViewHolder();
                holder.comment_user_icon = (CircleImageView) convertView.findViewById(R.id.comment_user_icon);
                holder.comment_user_name = (TextView) convertView.findViewById(R.id.comment_user_name);
                holder.comment_text = (TextView) convertView.findViewById(R.id.comment_text);
                holder.comment_time = (TextView) convertView.findViewById(R.id.comment_time);
                holder.up_icon = (ImageView) convertView.findViewById(R.id.up_icon);
                holder.comment_up_num = (TextView) convertView.findViewById(R.id.comment_up_num);
                holder.rl_comment_text=convertView.findViewById(R.id.rl_comment_text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final CommentzqzxBean item = getItem(position);

//            Log.e("test","test"+item.toString());
            holder.userId = item.getUid();
            // 显示头像

            SPUtil.displayImage(item.getAvatar_path()
                    , holder.comment_user_icon
                    , DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.XF_Avatar));
//            ImageLoader.getInstance().displayImage(item.getAvatar_path(), holder.avatar);
            holder.comment_user_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mIntent = new Intent();
                    mIntent.putExtra("uid", item.getUid()); //TODO
                    mIntent.setClass(parent.getContext(), XF_PInfoActivity.class);
                    parent.getContext().startActivity(mIntent);
                }
            });

            // 用户名
            holder.comment_user_name.setText(item.getNickname());
            holder.comment_user_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mIntent = new Intent();
                    mIntent.putExtra("uid", item.getUid()); //TODO
                    mIntent.setClass(parent.getContext(), XF_PInfoActivity.class);
                    parent.getContext().startActivity(mIntent);
                }
            });

            // 评论内容
            holder.comment_text.setText(item.getContent());
//            holder.rl_comment_text.setOnClickListener(new View.OnClickListener() {
            holder.comment_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ArrayList<String> titles = new ArrayList<String>();
                    titles.add(parent.getContext().getResources().getString(R.string.reply_comment));
//                titles.add("删除");
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_popupwindow, null);
                    PopUpwindowLayout popUpwindowLayout = (PopUpwindowLayout) view.findViewById(R.id.llayout_popupwindow);
                    popUpwindowLayout.initViews(parent.getContext(), titles, false);
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
                                    Intent intent = new Intent(parent.getContext(), ZQ_ReplyCommentActivity.class);
                                    intent.putExtra("USER_UID", item.getCid());
                                    parent.getContext().startActivity(intent);
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
            holder.comment_time.setText(CalendarUtil.friendlyTime1(item.getDateline(), parent.getContext()));


            if (SharePreferecesUtils.getParam(parent.getContext(), "" + item.getCid(), "0").toString().equals("1")) {
                holder.up_icon.setImageResource(R.drawable.details_icon_likeit);
            }

            // 点赞数
            holder.comment_up_num.setText(item.getPraise());
            holder.up_icon.setTag(item);
            holder.up_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Log.e("holder.digNum", "holder.digNum");
                        if (null == spu.getUser()) {
                            return;
                        }
                        Log.i(getLogTag(), "uid-" + spu.getUser().getUid() + "  mType-News" + " nid-" + item.getCid());
                        String station = SharePreferecesUtils.getParam(parent.getContext(), StationConfig.STATION, "def").toString();
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
                        final RequestParams params = RequestParamsUtils.getParamsWithU();
                        params.addBodyParameter("uid", spu.getUser().getUid());
                        params.addBodyParameter("type", "News");
                        params.addBodyParameter("nid", item.getCid());
                        params.addBodyParameter("siteid", siteid);

                        httpUtils.send(HttpRequest.HttpMethod.POST
                                , prise_url//InterfaceApi.mPraise
                                , params
                                , new RequestCallBack<String>() {
                            @Override
                            public void onFailure(HttpException arg0, String arg1) {
                                Log.e(getLogTag(), "赞failed!");
                                TUtils.toast(parent.getContext().getString(R.string.toast_server_no_response));
                            }

                            @Override
                            public void onSuccess(ResponseInfo<String> arg0) {
                                Log.d(getLogTag(), "赞-->" + arg0.result);
                                JSONObject obj = JSONObject.parseObject(arg0.result);

                                if (200 == obj.getInteger("code")) {
                                    LogUtils.i("m---->" + item.getPraise());
                                    SharePreferecesUtils.setParam(parent.getContext(), "" + item.getCid(), "1");
                                    if (TextUtils.isDigitsOnly(item.getPraise())) {
                                        holder.up_icon.setImageResource(R.drawable.details_icon_likeit);
                                        int i = Integer.parseInt(item.getPraise());
                                        i++;
                                        LogUtils.i("i---->" + i);
                                        holder.comment_up_num.setText(i + "");
                                        item.setPraise(i + "");
                                        notifyDataSetChanged();
                                    }
                                } else {

                                }
                            }
                        });
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
        }


        return convertView;
    }

    public void appendData(List<CommentzqzxBean> data) {
        if (data != null && !data.isEmpty()) {
            mDataList.addAll(data);
            notifyDataSetChanged();
        }
    }

    public void appendData(List<CommentzqzxBean> data, boolean isClearOld) {
        if (isClearOld) {
            mDataList.clear();
        }
        if (data != null && !data.isEmpty()) {
            mDataList.addAll(data);
            notifyDataSetChanged();
        }
    }

    public void insertData(List<CommentzqzxBean> data, int positon) {
        if (data != null && !data.isEmpty()) {
            mDataList.addAll(positon, data);
            notifyDataSetChanged();
        }
    }

    private void praise(final ImageView image, final TextView text, final CommentzqzxBean cb) {
        final Context context = image.getContext();
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
                    SharePreferecesUtils.setParam(context, "" + cb.getCid(), "1");
                    if (TextUtils.isDigitsOnly(cb.getPraise())) {
                        image.setImageResource(R.drawable.details_icon_likeit);
                        int i = Integer.parseInt(cb.getPraise());
                        i++;
                        LogUtils.i("i---->" + i);
                        text.setText(i + "");
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

    public static class ViewHolder {
        public String userId = "";
        public CircleImageView comment_user_icon;
        public TextView comment_user_name;
        public TextView comment_text;
        public TextView comment_time;
        public ImageView up_icon;
        private TextView comment_up_num;
        private View rl_comment_text;

    }
}