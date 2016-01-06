package com.hzpd.ui.fragments.action;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hzpd.adapter.ZY_Tsbl_blAdapter;
import com.hzpd.custorm.GridViewInScrollView;
import com.hzpd.hflt.R;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.CODE;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.ResponseStream;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.squareup.okhttp.Request;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.greenrobot.event.EventBus;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class ActionRegisterFragment extends BaseFragment implements View.OnClickListener {

    private GridViewInScrollView action_reg_photo_gv;
    private TextView action_reg_item_lable9_tv;
    private EditText action_reg_item_content9_et;
    private TextView action_reg_item_lable8_tv;
    private EditText action_reg_item_content8_et;
    private TextView action_reg_item_lable7_tv;
    private EditText action_reg_item_content7_et;
    private TextView action_reg_item_lable6_tv;
    private EditText action_reg_item_content6_et;
    private TextView action_reg_item_lable5_tv;
    private EditText action_reg_item_content5_et;
    private TextView action_reg_item_lable4_tv;
    private EditText action_reg_item_content4_et;
    private TextView action_reg_item_lable3_tv;
    private EditText action_reg_item_content3_et;
    private TextView action_reg_item_lable2_tv;
    private EditText action_reg_item_content2_et;
    private TextView action_reg_item_lable1_tv;
    private EditText action_reg_item_content1_et;

    private LinearLayout action_reg_ll1;
    private LinearLayout action_reg_ll2;
    private LinearLayout action_reg_ll3;
    private LinearLayout action_reg_ll4;
    private LinearLayout action_reg_ll5;
    private LinearLayout action_reg_ll6;
    private LinearLayout action_reg_ll7;
    private LinearLayout action_reg_ll8;
    private LinearLayout action_reg_ll9;

    private ImageView action_reg_item_star1_iv;
    private ImageView action_reg_item_star2_iv;
    private ImageView action_reg_item_star3_iv;
    private ImageView action_reg_item_star4_iv;
    private ImageView action_reg_item_star5_iv;
    private ImageView action_reg_item_star6_iv;
    private ImageView action_reg_item_star7_iv;
    private ImageView action_reg_item_star8_iv;
    private ImageView action_reg_item_star9_iv;

    private View registration_submit_bt;

    private ArrayList<String> mSelectPath;// 图片路径
    private ZY_Tsbl_blAdapter adapter;
    private String activityid;
    private JSONObject conf;
    private MyasyncUpload myasyncUpload;
    private Object tag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.action_register_fm_layout, container, false);
        initViews(view);
tag= OkHttpClientManager.getTag();
        return view;
    }

    private void initViews(View view) {
        action_reg_photo_gv = (GridViewInScrollView) view.findViewById(R.id.action_reg_photo_gv);
        action_reg_item_lable9_tv = (TextView) view.findViewById(R.id.action_reg_item_lable9_tv);
        action_reg_item_content9_et = (EditText) view.findViewById(R.id.action_reg_item_content9_et);
        action_reg_item_lable8_tv = (TextView) view.findViewById(R.id.action_reg_item_lable8_tv);
        action_reg_item_content8_et = (EditText) view.findViewById(R.id.action_reg_item_content8_et);
        action_reg_item_lable7_tv = (TextView) view.findViewById(R.id.action_reg_item_lable7_tv);
        action_reg_item_content7_et = (EditText) view.findViewById(R.id.action_reg_item_content7_et);
        action_reg_item_lable6_tv = (TextView) view.findViewById(R.id.action_reg_item_lable6_tv);
        action_reg_item_content6_et = (EditText) view.findViewById(R.id.action_reg_item_content6_et);
        action_reg_item_lable5_tv = (TextView) view.findViewById(R.id.action_reg_item_lable5_tv);
        action_reg_item_content5_et = (EditText) view.findViewById(R.id.action_reg_item_content5_et);
        action_reg_item_lable4_tv = (TextView) view.findViewById(R.id.action_reg_item_lable4_tv);
        action_reg_item_content4_et = (EditText) view.findViewById(R.id.action_reg_item_content4_et);
        action_reg_item_lable3_tv = (TextView) view.findViewById(R.id.action_reg_item_lable3_tv);
        action_reg_item_content3_et = (EditText) view.findViewById(R.id.action_reg_item_content3_et);
        action_reg_item_lable2_tv = (TextView) view.findViewById(R.id.action_reg_item_lable2_tv);
        action_reg_item_content2_et = (EditText) view.findViewById(R.id.action_reg_item_content2_et);
        action_reg_item_lable1_tv = (TextView) view.findViewById(R.id.action_reg_item_lable1_tv);
        action_reg_item_content1_et = (EditText) view.findViewById(R.id.action_reg_item_content1_et);

        action_reg_ll1 = (LinearLayout) view.findViewById(R.id.action_reg_ll1);
        action_reg_ll2 = (LinearLayout) view.findViewById(R.id.action_reg_ll2);
        action_reg_ll3 = (LinearLayout) view.findViewById(R.id.action_reg_ll3);
        action_reg_ll4 = (LinearLayout) view.findViewById(R.id.action_reg_ll4);
        action_reg_ll5 = (LinearLayout) view.findViewById(R.id.action_reg_ll5);
        action_reg_ll6 = (LinearLayout) view.findViewById(R.id.action_reg_ll6);
        action_reg_ll7 = (LinearLayout) view.findViewById(R.id.action_reg_ll7);
        action_reg_ll8 = (LinearLayout) view.findViewById(R.id.action_reg_ll8);
        action_reg_ll9 = (LinearLayout) view.findViewById(R.id.action_reg_ll9);

        action_reg_item_star1_iv = (ImageView) view.findViewById(R.id.action_reg_item_star1_iv);
        action_reg_item_star2_iv = (ImageView) view.findViewById(R.id.action_reg_item_star2_iv);
        action_reg_item_star3_iv = (ImageView) view.findViewById(R.id.action_reg_item_star3_iv);
        action_reg_item_star4_iv = (ImageView) view.findViewById(R.id.action_reg_item_star4_iv);
        action_reg_item_star5_iv = (ImageView) view.findViewById(R.id.action_reg_item_star5_iv);
        action_reg_item_star6_iv = (ImageView) view.findViewById(R.id.action_reg_item_star6_iv);
        action_reg_item_star7_iv = (ImageView) view.findViewById(R.id.action_reg_item_star7_iv);
        action_reg_item_star8_iv = (ImageView) view.findViewById(R.id.action_reg_item_star8_iv);
        action_reg_item_star9_iv = (ImageView) view.findViewById(R.id.action_reg_item_star9_iv);

        registration_submit_bt = view.findViewById(R.id.registration_submit_bt);
        registration_submit_bt.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);

        Bundle args = getArguments();
        if (null == args) {
            return;
        }
        activityid = args.getString("id");
        if (TextUtils.isEmpty(activityid)) {
            return;
        }

        adapter = new ZY_Tsbl_blAdapter(activity);
        adapter.setMaxSize(1);
        action_reg_photo_gv.setAdapter(adapter);

        getInfoFromSever();
    }

    private void getInfoFromSever() {
        Map<String,String> params = RequestParamsUtils.getMaps();
        params.put("activityid", activityid);
        OkHttpClientManager.postAsyn(tag, InterfaceJsonfile.actionConf, new OkHttpClientManager.ResultCallback() {
            @Override
            public void onSuccess(Object response) {
                LogUtils.i("action regconf result-->" + response.toString());
                JSONObject obj = null;
                try {
                    obj = JSONObject.parseObject(response.toString());
                } catch (Exception e) {
                    return;
                }
                if (200 == obj.getIntValue("code")) {
                    conf = obj.getJSONObject("data");
                    conf();

                } else {
                    TUtils.toast(obj.getString("msg"));
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                LogUtils.i("action regconf failed");
            }
        }, params);
    }

    private void conf() {
        String s = conf.getString("username");
        if (null != s && !"".equals(s)) {
            action_reg_ll1.setVisibility(View.VISIBLE);
            action_reg_item_lable1_tv.setText(s + "：");
        }
        s = conf.getString("tel");
        if (null != s && !"".equals(s)) {
            action_reg_ll2.setVisibility(View.VISIBLE);
            action_reg_item_lable2_tv.setText(s + "：");
        }
        s = conf.getString("title");
        if (null != s && !"".equals(s)) {
            action_reg_ll3.setVisibility(View.VISIBLE);
            action_reg_item_lable3_tv.setText(s + "：");
        }
        s = conf.getString("extra1");
        if (null != s && !"".equals(s)) {
            action_reg_ll4.setVisibility(View.VISIBLE);
            action_reg_item_lable4_tv.setText(s + "：");
        }
        s = conf.getString("extra2");
        if (null != s && !"".equals(s)) {
            action_reg_ll5.setVisibility(View.VISIBLE);
            action_reg_item_lable5_tv.setText(s + "：");
        }
        s = conf.getString("extra3");
        if (null != s && !"".equals(s)) {
            action_reg_ll6.setVisibility(View.VISIBLE);
            action_reg_item_lable6_tv.setText(s + "：");
        }
        s = conf.getString("extra4");
        if (null != s && !"".equals(s)) {
            action_reg_ll7.setVisibility(View.VISIBLE);
            action_reg_item_lable7_tv.setText(s + "：");
        }
        s = conf.getString("extra5");
        if (null != s && !"".equals(s)) {
            action_reg_ll8.setVisibility(View.VISIBLE);
            action_reg_item_lable8_tv.setText(s + "：");
        }
        s = conf.getString("content");
        if (null != s && !"".equals(s)) {
            action_reg_ll9.setVisibility(View.VISIBLE);
            action_reg_item_lable9_tv.setText(s + "：");
        }
        s = conf.getString("username");
        if (null != s && !"".equals(s)) {
            action_reg_ll1.setVisibility(View.VISIBLE);
            action_reg_item_lable1_tv.setText(s + "：");
        }
        int maxP = conf.getIntValue("picmax");
        if (maxP > 0) {
            action_reg_photo_gv.setVisibility(View.VISIBLE);
            adapter.setMaxSize(maxP);
        }

        action_reg_item_star1_iv.setVisibility(View.VISIBLE);
        action_reg_item_star2_iv.setVisibility(View.VISIBLE);
        action_reg_item_star3_iv.setVisibility(View.VISIBLE);
        action_reg_item_content2_et.setHint(R.string.hint_phone_number);

        JSONArray array = conf.getJSONArray("need");
        for (int i = 0; i < array.size(); i++) {
            s = array.getString(i);

            if ("username".equals(s)) {
                action_reg_item_star1_iv.setVisibility(View.VISIBLE);
                continue;
            }
            if ("tel".equals(s)) {
                action_reg_item_star2_iv.setVisibility(View.VISIBLE);
                continue;
            }
            if ("title".equals(s)) {
                action_reg_item_star3_iv.setVisibility(View.VISIBLE);
                continue;
            }
            if ("extra1".equals(s)) {
                action_reg_item_star4_iv.setVisibility(View.VISIBLE);
                String extype = conf.getString("extra1_type");
                if ("4".equals(extype)) {
                    action_reg_item_content4_et.setHint(R.string.hint_date);
                }
                if (conf.getString("extra1").contains("性别")) {
                    action_reg_item_content4_et.setHint(R.string.hint_sex);
                }
                continue;
            }
            if ("extra2".equals(s)) {
                action_reg_item_star5_iv.setVisibility(View.VISIBLE);
                String extype = conf.getString("extra2_type");
                if ("4".equals(extype)) {
                    action_reg_item_content5_et.setHint(R.string.hint_date);
                }
                if (conf.getString("extra2").contains("性别")) {
                    action_reg_item_content5_et.setHint(R.string.hint_sex);
                }
                continue;
            }
            if ("extra3".equals(s)) {
                action_reg_item_star6_iv.setVisibility(View.VISIBLE);
                String extype = conf.getString("extra3_type");
                if ("4".equals(extype)) {
                    action_reg_item_content6_et.setHint(R.string.hint_date);
                }
                if (conf.getString("extra3").contains("性别")) {
                    action_reg_item_content6_et.setHint(R.string.hint_sex);
                }
                continue;
            }
            if ("extra4".equals(s)) {
                action_reg_item_star7_iv.setVisibility(View.VISIBLE);
                String extype = conf.getString("extra4_type");
                if ("4".equals(extype)) {
                    action_reg_item_content7_et.setHint(R.string.hint_date);
                }
                if (conf.getString("extra4").contains("性别")) {
                    action_reg_item_content7_et.setHint(R.string.hint_sex);
                }
                continue;
            }
            if ("extra5".equals(s)) {
                action_reg_item_star8_iv.setVisibility(View.VISIBLE);
                String extype = conf.getString("extra5_type");
                if ("4".equals(extype)) {
                    action_reg_item_content8_et.setHint(R.string.hint_date);
                }
                if (conf.getString("extra5").contains("性别")) {
                    action_reg_item_content8_et.setHint(R.string.hint_sex);
                }
                continue;
            }
            if ("content".equals(s)) {
                action_reg_item_star9_iv.setVisibility(View.VISIBLE);
                continue;
            }
        }
    }


    private void reset() {
        TUtils.toast(getString(R.string.toast_apply_success));

        action_reg_item_content1_et.setText("");
        action_reg_item_content2_et.setText("");
        action_reg_item_content3_et.setText("");
        action_reg_item_content4_et.setText("");
        action_reg_item_content5_et.setText("");
        action_reg_item_content6_et.setText("");
        action_reg_item_content7_et.setText("");
        action_reg_item_content8_et.setText("");
        action_reg_item_content9_et.setText("");

        mSelectPath.clear();
        adapter.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.registration_submit_bt: {

                JSONArray array = conf.getJSONArray("need");
                String extype;

                String co = action_reg_item_content1_et.getText().toString();
                if (null == co || "".equals(co)) {
                    TUtils.toast(getString(R.string.toast_xx_cannot_be_empty, conf.getString("username")));
                    return;
                }
                co = action_reg_item_content2_et.getText().toString();
                if (null == co || "".equals(co)) {
                    TUtils.toast(getString(R.string.toast_xx_cannot_be_empty, conf.getString("tel")));
                    return;
                }
                if (!(isMobile(co) || isPhone(co))) {
                    LogUtils.i("b--->" + co);
                    TUtils.toast(getString(R.string.toast_phone_format_is_wrong));
                    return;
                }
                co = action_reg_item_content3_et.getText().toString();
                if (null == co || "".equals(co)) {
                    TUtils.toast(getString(R.string.toast_xx_cannot_be_empty, conf.getString("title")));
                    return;
                }

                for (int i = 0; i < array.size(); i++) {
                    String s = array.getString(i);

                    if ("username".equals(s)) {
                        String content = action_reg_item_content1_et.getText().toString();
                        if (null == content || "".equals(content)) {
                            TUtils.toast(getString(R.string.toast_xx_cannot_be_empty, conf.getString(s)));
                            return;
                        }
                    }
                    if ("tel".equals(s)) {
                        String content = action_reg_item_content2_et.getText().toString();
                        if (null == content || "".equals(content)) {
                            TUtils.toast(getString(R.string.toast_xx_cannot_be_empty, conf.getString(s)));
                            return;
                        }
                        if (!(isMobile(content) || isPhone(content))) {
                            LogUtils.i("--s->" + co);
                            TUtils.toast(getString(R.string.toast_phone_format_is_wrong));
                            return;
                        }
                    }
                    if ("title".equals(s)) {
                        String content = action_reg_item_content3_et.getText().toString();
                        if (null == content || "".equals(content)) {
                            TUtils.toast(getString(R.string.toast_xx_cannot_be_empty, conf.getString(s)));
                            return;
                        }
                    }
                    if ("extra1".equals(s)) {
                        String content = action_reg_item_content4_et.getText().toString();
                        if (null == content || "".equals(content)) {
                            TUtils.toast(getString(R.string.toast_xx_cannot_be_empty, conf.getString(s)));
                            return;
                        }
                        extype = conf.getString("extra1_type");
                        if (!validateS(extype, content)) {
                            popStr(extype, content);
                            return;
                        }
                    }
                    if ("extra2".equals(s)) {
                        String content = action_reg_item_content5_et.getText().toString();
                        if (null == content || "".equals(content)) {
                            TUtils.toast(getString(R.string.toast_xx_cannot_be_empty, conf.getString(s)));
                            return;
                        }
                        extype = conf.getString("extra2_type");
                        if (!validateS(extype, content)) {
                            popStr(extype, content);
                            return;
                        }
                    }
                    if ("extra3".equals(s)) {
                        String content = action_reg_item_content6_et.getText().toString();
                        if (null == content || "".equals(content)) {
                            TUtils.toast(getString(R.string.toast_xx_cannot_be_empty, conf.getString(s)));
                            return;
                        }
                        extype = conf.getString("extra3_type");
                        if (!validateS(extype, content)) {
                            popStr(extype, content);
                            return;
                        }
                    }
                    if ("extra4".equals(s)) {
                        String content = action_reg_item_content7_et.getText().toString();
                        if (null == content || "".equals(content)) {
                            TUtils.toast(getString(R.string.toast_xx_cannot_be_empty, conf.getString(s)));
                            return;
                        }
                        extype = conf.getString("extra4_type");
                        if (!validateS(extype, content)) {
                            popStr(extype, content);
                            return;
                        }
                    }
                    if ("extra5".equals(s)) {
                        String content = action_reg_item_content8_et.getText().toString();
                        if (null == content || "".equals(content)) {
                            TUtils.toast(getString(R.string.toast_xx_cannot_be_empty, conf.getString(s)));
                            return;
                        }
                        extype = conf.getString("extra5_type");
                        if (!validateS(extype, content)) {
                            popStr(extype, content);
                            return;
                        }
                    }
                    if ("content".equals(s)) {
                        String content = action_reg_item_content9_et.getText().toString();
                        if (null == content || "".equals(content)) {
                            TUtils.toast(getString(R.string.toast_xx_cannot_be_empty, conf.getString(s)));
                            return;
                        }
                    }
                }

                myasyncUpload = new MyasyncUpload();
                myasyncUpload.execute("");
            }
            break;
        }

    }

    class MyasyncUpload extends AsyncTask<String, String, String> {

        private String[] mParams;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showUploadDialog();
            mParams = new String[]{
                    action_reg_item_content1_et.getText().toString(),
                    action_reg_item_content2_et.getText().toString(),
                    action_reg_item_content3_et.getText().toString(),
                    action_reg_item_content4_et.getText().toString(),
                    action_reg_item_content5_et.getText().toString(),
                    action_reg_item_content6_et.getText().toString(),
                    action_reg_item_content7_et.getText().toString(),
                    action_reg_item_content8_et.getText().toString(),
                    action_reg_item_content9_et.getText().toString(),
            };
        }

        @Override
        protected String doInBackground(String... params) {
            StringBuilder pics = new StringBuilder();
//            for (int i = 0; i < mSelectPath.size(); i++) {
//                File f = new File(mSelectPath.get(i));
//
//                RequestParams para = RequestParamsUtils.getParams();
//                para.addBodyParameter("fpic", f);
//                para.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
//                ResponseStream rs;
//                try {
//                    rs = httpUtils.sendSync(HttpMethod.POST, InterfaceJsonfile.TSBLADDIMG, para);
//                    JSONObject obj = FjsonUtil.parseObject(rs.readString());
//                    LogUtils.i(obj.toJSONString());
//                    if (200 == obj.getIntValue("code")) {
//                        String id = obj.getString("data");
//                        LogUtils.i("id-->" + id);
//                        pics.append(id);
//                        pics.append(",");
//                    } else {
//                        return getString(R.string.toast_failed_to_upload_image);
//                    }
//                } catch (HttpException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
            String pic = "";
            if (pics.length() > 0) {
                pic = pics.substring(0, pics.length() - 1);
            }
            RequestParams para1 = RequestParamsUtils.getParams();
            para1.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
            para1.addBodyParameter("activityid", activityid);
            para1.addBodyParameter("pic", pic);
            LogUtils.i("pics-->" + pic);

            para1.addBodyParameter("username", mParams[0]);
            LogUtils.i("username-->" + mParams[0]);

            para1.addBodyParameter("tel", mParams[1]);
            LogUtils.i("tel-->" + mParams[1]);

            para1.addBodyParameter("title", mParams[2]);
            LogUtils.i("title-->" + mParams[2]);

            para1.addBodyParameter("extra1", mParams[3]);
            LogUtils.i("extra1-->" + mParams[3]);

            para1.addBodyParameter("extra2", mParams[4]);
            LogUtils.i("extra2-->" + mParams[4]);

            para1.addBodyParameter("extra3", mParams[5]);
            LogUtils.i("extra3-->" + mParams[5]);

            para1.addBodyParameter("extra4", mParams[6]);
            LogUtils.i("extra4-->" + mParams[6]);

            para1.addBodyParameter("extra5", mParams[7]);
            LogUtils.i("extra5-->" + mParams[7]);

            para1.addBodyParameter("content", mParams[8]);
            LogUtils.i("content-->" + mParams[8]);

            String result = null;
//            try {
//                ResponseStream rs = httpUtils.sendSync(HttpMethod.POST, InterfaceJsonfile.actionRegSubm, para1);
//                result = rs.readString();
//            } catch (HttpException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            JSONObject obj = FjsonUtil.parseObject(result);
            if (null != obj) {
                if (200 == obj.getIntValue("code")) {// 提交成功
                    reset();
                }
                TUtils.toast(obj.getString("msg"));
            } else {
                TUtils.toast("" + result);
            }
        }

    }

    private boolean validateS(String type, String con) {
        // 1:邮箱,2:字符,3:数字,4:日期,5:qq,6:密码,7:单选,8:多选
        boolean flag = true;
        if ("1".equals(type)) {
            flag = isEmail(con);
        } else if ("2".equals(type)) {
            flag = TextUtils.isEmpty(con);
        } else if ("3".equals(type)) {
            Pattern pattern = Pattern.compile("[0-9]*");
            flag = pattern.matcher(con).matches();
        } else if ("4".equals(type)) {
            flag = isDate(con);
        } else if ("5".equals(type)) {
            flag = isQQ(con);
        }

        return flag;
    }

    private void popStr(String type, String s) {
        // 1:邮箱,2:字符,3:数字,4:日期,5:qq,6:密码,7:单选,8:多选
        LogUtils.i("type-->" + type + "--s-->" + s);
        if ("1".equals(type)) {
            TUtils.toast(getString(R.string.toast_email_format_is_wrong));
        } else if ("2".equals(type)) {
            TUtils.toast(getString(R.string.toast_xx_cannot_be_empty, s));
        } else if ("3".equals(type)) {
            TUtils.toast(getString(R.string.toast_input_number));
        } else if ("4".equals(type)) {
            TUtils.toast(getString(R.string.toast_date_format_is_wrong));
        } else if ("5".equals(type)) {
            TUtils.toast(getString(R.string.toast_qq_format_is_wrong));
        }
    }

    // 进度对话框
    private void showUploadDialog() {
    }

    public void onEventMainThread(Integer id) {
        if (R.id.zy_tsbl_bl_item_iv != id) {
            return;
        }
        Intent intent = new Intent(activity, MultiImageSelectorActivity.class);
        // 是否显示拍摄图片
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        // 最大可选择图片数量
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, adapter.getMaxSize());
        // 选择模式
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
        // 默认选择
        if (mSelectPath != null && mSelectPath.size() > 0) {
            intent.putExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, mSelectPath);
        }
        activity.startActivityForResult(intent, CODE.REQUEST_IMAGE);
        AAnim.ActivityStartAnimation(activity);
    }

    public void onEventMainThread(ArrayList<String> mSelectPath) {
        this.mSelectPath = mSelectPath;
        adapter.appendData(this.mSelectPath, true);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        if (null != myasyncUpload) {
            if (!myasyncUpload.isCancelled()) {
                myasyncUpload.cancel(true);
            }
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public static boolean isMobile(String str) {
        Pattern p = null;
        Matcher m = null;
        boolean flag = false;
        try {
            p = Pattern.compile("^[1][3,4,5,8][0-9]{9}$"); // 验证手机号
            m = p.matcher(str);
            flag = m.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 电话号码验证
     */
    public static boolean isPhone(String str) {
        Pattern p1 = null;
        Matcher m = null;
        boolean flag = false;
        p1 = Pattern.compile("^(0\\d{2}-\\d{5,10}(-\\d{1,4})?)|(0\\d{3}-\\d{7,8}(-\\d{1,4})?)$"); // 验证带区号的
        try {
            m = p1.matcher(str);
            flag = m.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 验证Email地址的正确性
     */
    public static boolean isEmail(String email) {
        try {
            String str = "^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$";
            Pattern p = Pattern.compile(str);
            Matcher m = p.matcher(email);
            return m.matches();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断日期格式和范围
     */
    public static boolean isDate(String date) {
        boolean flag = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // yyyy年
        try {
            sdf.parse(date);
            flag = true;
        } catch (Exception ex) {
            flag = false;
        }
        return flag;
    }

    public static boolean isQQ(String qq) {
        boolean flag = false;
        try {
            Pattern pattern = Pattern.compile("^[1-9][0-9]{4,}$");
            Matcher matcher = pattern.matcher(qq);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }
}
