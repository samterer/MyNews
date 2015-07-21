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
import com.hzpd.custorm.CustomProgressDialog;
import com.hzpd.custorm.GridViewInScrollView;
import com.hzpd.hflt.R;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.CODE;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.ResponseStream;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.greenrobot.event.EventBus;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class ActionRegisterFragment extends BaseFragment {

	@ViewInject(R.id.action_reg_photo_gv)
	private GridViewInScrollView action_reg_photo_gv;

	@ViewInject(R.id.action_reg_item_lable9_tv)
	private TextView action_reg_item_lable9_tv;
	@ViewInject(R.id.action_reg_item_content9_et)
	private EditText action_reg_item_content9_et;
	@ViewInject(R.id.action_reg_item_lable8_tv)
	private TextView action_reg_item_lable8_tv;
	@ViewInject(R.id.action_reg_item_content8_et)
	private EditText action_reg_item_content8_et;
	@ViewInject(R.id.action_reg_item_lable7_tv)
	private TextView action_reg_item_lable7_tv;
	@ViewInject(R.id.action_reg_item_content7_et)
	private EditText action_reg_item_content7_et;
	@ViewInject(R.id.action_reg_item_lable6_tv)
	private TextView action_reg_item_lable6_tv;
	@ViewInject(R.id.action_reg_item_content6_et)
	private EditText action_reg_item_content6_et;
	@ViewInject(R.id.action_reg_item_lable5_tv)
	private TextView action_reg_item_lable5_tv;
	@ViewInject(R.id.action_reg_item_content5_et)
	private EditText action_reg_item_content5_et;
	@ViewInject(R.id.action_reg_item_lable4_tv)
	private TextView action_reg_item_lable4_tv;
	@ViewInject(R.id.action_reg_item_content4_et)
	private EditText action_reg_item_content4_et;
	@ViewInject(R.id.action_reg_item_lable3_tv)
	private TextView action_reg_item_lable3_tv;
	@ViewInject(R.id.action_reg_item_content3_et)
	private EditText action_reg_item_content3_et;
	@ViewInject(R.id.action_reg_item_lable2_tv)
	private TextView action_reg_item_lable2_tv;
	@ViewInject(R.id.action_reg_item_content2_et)
	private EditText action_reg_item_content2_et;
	@ViewInject(R.id.action_reg_item_lable1_tv)
	private TextView action_reg_item_lable1_tv;
	@ViewInject(R.id.action_reg_item_content1_et)
	private EditText action_reg_item_content1_et;
	@ViewInject(R.id.action_reg_ll1)
	private LinearLayout action_reg_ll1;
	@ViewInject(R.id.action_reg_ll2)
	private LinearLayout action_reg_ll2;
	@ViewInject(R.id.action_reg_ll3)
	private LinearLayout action_reg_ll3;
	@ViewInject(R.id.action_reg_ll4)
	private LinearLayout action_reg_ll4;
	@ViewInject(R.id.action_reg_ll5)
	private LinearLayout action_reg_ll5;
	@ViewInject(R.id.action_reg_ll6)
	private LinearLayout action_reg_ll6;
	@ViewInject(R.id.action_reg_ll7)
	private LinearLayout action_reg_ll7;
	@ViewInject(R.id.action_reg_ll8)
	private LinearLayout action_reg_ll8;
	@ViewInject(R.id.action_reg_ll9)
	private LinearLayout action_reg_ll9;

	@ViewInject(R.id.action_reg_item_star1_iv)
	private ImageView action_reg_item_star1_iv;
	@ViewInject(R.id.action_reg_item_star2_iv)
	private ImageView action_reg_item_star2_iv;
	@ViewInject(R.id.action_reg_item_star3_iv)
	private ImageView action_reg_item_star3_iv;
	@ViewInject(R.id.action_reg_item_star4_iv)
	private ImageView action_reg_item_star4_iv;
	@ViewInject(R.id.action_reg_item_star5_iv)
	private ImageView action_reg_item_star5_iv;
	@ViewInject(R.id.action_reg_item_star6_iv)
	private ImageView action_reg_item_star6_iv;
	@ViewInject(R.id.action_reg_item_star7_iv)
	private ImageView action_reg_item_star7_iv;
	@ViewInject(R.id.action_reg_item_star8_iv)
	private ImageView action_reg_item_star8_iv;
	@ViewInject(R.id.action_reg_item_star9_iv)
	private ImageView action_reg_item_star9_iv;

	private ArrayList<String> mSelectPath;// 鍥剧墖璺緞
	private ZY_Tsbl_blAdapter adapter;
	private String activityid;
	private JSONObject conf;
	private CustomProgressDialog dialog;
	private MyasyncUpload myasyncUpload;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.action_register_fm_layout, container, false);
		ViewUtils.inject(this, view);
		return view;
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
		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("activityid", activityid);
		httpUtils.send(HttpMethod.POST, InterfaceJsonfile.actionConf, params, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				LogUtils.i("action regconf result-->" + responseInfo.result);
				JSONObject obj = null;
				try {
					obj = JSONObject.parseObject(responseInfo.result);
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
			public void onFailure(HttpException error, String msg) {
				LogUtils.i("action regconf failed");
			}
		});
	}

	private void conf() {
		String s = conf.getString("username");
		if (null != s && !"".equals(s)) {
			action_reg_ll1.setVisibility(View.VISIBLE);
			action_reg_item_lable1_tv.setText(s + "锛�");
		}
		s = conf.getString("tel");
		if (null != s && !"".equals(s)) {
			action_reg_ll2.setVisibility(View.VISIBLE);
			action_reg_item_lable2_tv.setText(s + "锛�");
		}
		s = conf.getString("title");
		if (null != s && !"".equals(s)) {
			action_reg_ll3.setVisibility(View.VISIBLE);
			action_reg_item_lable3_tv.setText(s + "锛�");
		}
		s = conf.getString("extra1");
		if (null != s && !"".equals(s)) {
			action_reg_ll4.setVisibility(View.VISIBLE);
			action_reg_item_lable4_tv.setText(s + "锛�");
		}
		s = conf.getString("extra2");
		if (null != s && !"".equals(s)) {
			action_reg_ll5.setVisibility(View.VISIBLE);
			action_reg_item_lable5_tv.setText(s + "锛�");
		}
		s = conf.getString("extra3");
		if (null != s && !"".equals(s)) {
			action_reg_ll6.setVisibility(View.VISIBLE);
			action_reg_item_lable6_tv.setText(s + "锛�");
		}
		s = conf.getString("extra4");
		if (null != s && !"".equals(s)) {
			action_reg_ll7.setVisibility(View.VISIBLE);
			action_reg_item_lable7_tv.setText(s + "锛�");
		}
		s = conf.getString("extra5");
		if (null != s && !"".equals(s)) {
			action_reg_ll8.setVisibility(View.VISIBLE);
			action_reg_item_lable8_tv.setText(s + "锛�");
		}
		s = conf.getString("content");
		if (null != s && !"".equals(s)) {
			action_reg_ll9.setVisibility(View.VISIBLE);
			action_reg_item_lable9_tv.setText(s + "锛�");
		}
		s = conf.getString("username");
		if (null != s && !"".equals(s)) {
			action_reg_ll1.setVisibility(View.VISIBLE);
			action_reg_item_lable1_tv.setText(s + "锛�");
		}
		int maxP = conf.getIntValue("picmax");
		if (maxP > 0) {
			action_reg_photo_gv.setVisibility(View.VISIBLE);
			adapter.setMaxSize(maxP);
		}

		action_reg_item_star1_iv.setVisibility(View.VISIBLE);
		action_reg_item_star2_iv.setVisibility(View.VISIBLE);
		action_reg_item_star3_iv.setVisibility(View.VISIBLE);
		action_reg_item_content2_et.setHint("鎵嬫満鎴栧骇鏈�");

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
					action_reg_item_content4_et.setHint("濡傦細2014-12-16");
				}
				if (conf.getString("extra1").contains("鎬у埆")) {
					action_reg_item_content4_et.setHint("鐢锋垨濂�");
				}
				continue;
			}
			if ("extra2".equals(s)) {
				action_reg_item_star5_iv.setVisibility(View.VISIBLE);
				String extype = conf.getString("extra2_type");
				if ("4".equals(extype)) {
					action_reg_item_content5_et.setHint("濡傦細2014-12-16");
				}
				if (conf.getString("extra2").contains("鎬у埆")) {
					action_reg_item_content5_et.setHint("鐢锋垨濂�");
				}
				continue;
			}
			if ("extra3".equals(s)) {
				action_reg_item_star6_iv.setVisibility(View.VISIBLE);
				String extype = conf.getString("extra3_type");
				if ("4".equals(extype)) {
					action_reg_item_content6_et.setHint("濡傦細2014-12-16");
				}
				if (conf.getString("extra3").contains("鎬у埆")) {
					action_reg_item_content6_et.setHint("鐢锋垨濂�");
				}
				continue;
			}
			if ("extra4".equals(s)) {
				action_reg_item_star7_iv.setVisibility(View.VISIBLE);
				String extype = conf.getString("extra4_type");
				if ("4".equals(extype)) {
					action_reg_item_content7_et.setHint("濡傦細2014-12-16");
				}
				if (conf.getString("extra4").contains("鎬у埆")) {
					action_reg_item_content7_et.setHint("鐢锋垨濂�");
				}
				continue;
			}
			if ("extra5".equals(s)) {
				action_reg_item_star8_iv.setVisibility(View.VISIBLE);
				String extype = conf.getString("extra5_type");
				if ("4".equals(extype)) {
					action_reg_item_content8_et.setHint("濡傦細2014-12-16");
				}
				if (conf.getString("extra5").contains("鎬у埆")) {
					action_reg_item_content8_et.setHint("鐢锋垨濂�");
				}
				continue;
			}
			if ("content".equals(s)) {
				action_reg_item_star9_iv.setVisibility(View.VISIBLE);
				continue;
			}
		}
	}

	@OnClick(R.id.registration_submit_bt)
	private void submit(View v) {

		JSONArray array = conf.getJSONArray("need");
		String extype;

		String co = action_reg_item_content1_et.getText().toString();
		if (null == co || "".equals(co)) {
			TUtils.toast(conf.getString("username") + "涓嶈兘涓虹┖");
			return;
		}
		co = action_reg_item_content2_et.getText().toString();
		if (null == co || "".equals(co)) {
			TUtils.toast(conf.getString("tel") + "涓嶈兘涓虹┖");
			return;
		}
		if (!(isMobile(co) || isPhone(co))) {
			LogUtils.i("b--->" + co);
			TUtils.toast("鐢佃瘽鏍煎紡涓嶆纭�");
			return;
		}
		co = action_reg_item_content3_et.getText().toString();
		if (null == co || "".equals(co)) {
			TUtils.toast(conf.getString("title") + "涓嶈兘涓虹┖");
			return;
		}

		for (int i = 0; i < array.size(); i++) {
			String s = array.getString(i);

			if ("username".equals(s)) {
				String content = action_reg_item_content1_et.getText().toString();
				if (null == content || "".equals(content)) {
					TUtils.toast(conf.getString(s) + "涓嶈兘涓虹┖");
					return;
				}
			}
			if ("tel".equals(s)) {
				String content = action_reg_item_content2_et.getText().toString();
				if (null == content || "".equals(content)) {
					TUtils.toast(conf.getString(s) + "涓嶈兘涓虹┖");
					return;
				}
				if (!(isMobile(content) || isPhone(content))) {
					LogUtils.i("--s->" + co);
					TUtils.toast("鐢佃瘽鏍煎紡涓嶆纭�");
					return;
				}
			}
			if ("title".equals(s)) {
				String content = action_reg_item_content3_et.getText().toString();
				if (null == content || "".equals(content)) {
					TUtils.toast(conf.getString(s) + "涓嶈兘涓虹┖");
					return;
				}
			}
			if ("extra1".equals(s)) {
				String content = action_reg_item_content4_et.getText().toString();
				if (null == content || "".equals(content)) {
					TUtils.toast(conf.getString(s) + "涓嶈兘涓虹┖");
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
					TUtils.toast(conf.getString(s) + "涓嶈兘涓虹┖");
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
					TUtils.toast(conf.getString(s) + "涓嶈兘涓虹┖");
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
					TUtils.toast(conf.getString(s) + "涓嶈兘涓虹┖");
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
					TUtils.toast(conf.getString(s) + "涓嶈兘涓虹┖");
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
					TUtils.toast(conf.getString(s) + "涓嶈兘涓虹┖");
					return;
				}
			}
		}

		myasyncUpload = new MyasyncUpload();
		myasyncUpload.execute("");
	}

	private void reset() {
		TUtils.toast("鎶ュ悕鎴愬姛");

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

	class MyasyncUpload extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showUploadDialog();
		}

		@Override
		protected String doInBackground(String... params) {

			StringBuilder pics = new StringBuilder();
			for (int i = 0; i < mSelectPath.size(); i++) {
				File f = new File(mSelectPath.get(i));
				RequestParams para = RequestParamsUtils.getParams();
				para.addBodyParameter("fpic", f);
				para.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
				ResponseStream rs;
				try {
					rs = httpUtils.sendSync(HttpMethod.POST, InterfaceJsonfile.TSBLADDIMG, para);
					JSONObject obj = FjsonUtil.parseObject(rs.readString());
					LogUtils.i(obj.toJSONString());
					if (200 == obj.getIntValue("code")) {
						String id = obj.getString("data");
						LogUtils.i("id-->" + id);
						pics.append(id);
						pics.append(",");
					} else {
						return "涓婁紶鍥剧墖" + i + "澶辫触";
					}
				} catch (HttpException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			String pic = "";
			if (pics.length() > 0) {
				pic = pics.substring(0, pics.length() - 1);
			}
			RequestParams para1 = RequestParamsUtils.getParams();
			para1.addBodyParameter("siteid", InterfaceJsonfile.SITEID);
			para1.addBodyParameter("activityid", activityid);
			para1.addBodyParameter("pic", pic);
			LogUtils.i("pics-->" + pic);

			String content = action_reg_item_content1_et.getText().toString();
			para1.addBodyParameter("username", content);
			LogUtils.i("username-->" + content);

			content = action_reg_item_content2_et.getText().toString();
			para1.addBodyParameter("tel", content);
			LogUtils.i("tel-->" + content);

			content = action_reg_item_content3_et.getText().toString();
			para1.addBodyParameter("title", content);
			LogUtils.i("title-->" + content);

			content = action_reg_item_content4_et.getText().toString();
			para1.addBodyParameter("extra1", content);
			LogUtils.i("extra1-->" + content);

			content = action_reg_item_content5_et.getText().toString();
			para1.addBodyParameter("extra2", content);
			LogUtils.i("extra2-->" + content);

			content = action_reg_item_content6_et.getText().toString();
			para1.addBodyParameter("extra3", content);
			LogUtils.i("extra3-->" + content);

			content = action_reg_item_content7_et.getText().toString();
			para1.addBodyParameter("extra4", content);
			LogUtils.i("extra4-->" + content);

			content = action_reg_item_content8_et.getText().toString();
			para1.addBodyParameter("extra5", content);
			LogUtils.i("extra5-->" + content);

			content = action_reg_item_content9_et.getText().toString();
			para1.addBodyParameter("content", content);
			LogUtils.i("content-->" + content);

			String result = null;
			try {
				ResponseStream rs = httpUtils.sendSync(HttpMethod.POST, InterfaceJsonfile.actionRegSubm, para1);
				result = rs.readString();
			} catch (HttpException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (null != dialog && dialog.isShowing()) {
				dialog.dismiss();
			}
			JSONObject obj = FjsonUtil.parseObject(result);
			if (null != obj) {
				if (200 == obj.getIntValue("code")) {// 鎻愪氦鎴愬姛
					reset();
				}
				TUtils.toast(obj.getString("msg"));
			} else {
				TUtils.toast("" + result);
			}
		}

	}

	private boolean validateS(String type, String con) {
		// 1:閭,2:瀛楃,3:鏁板瓧,4:鏃ユ湡,5:qq,6:瀵嗙爜,7:鍗曢��,8:澶氶��
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
		// 1:閭,2:瀛楃,3:鏁板瓧,4:鏃ユ湡,5:qq,6:瀵嗙爜,7:鍗曢��,8:澶氶��
		LogUtils.i("type-->" + type + "--s-->" + s);
		if ("1".equals(type)) {
			TUtils.toast("閭鏍煎紡涓嶆纭�");
		} else if ("2".equals(type)) {
			TUtils.toast(s + "涓嶈兘涓虹┖");
		} else if ("3".equals(type)) {
			TUtils.toast("璇峰～鍐欐暟瀛�");
		} else if ("4".equals(type)) {
			TUtils.toast("鏃ユ湡鏍煎紡涓嶆纭甛n姝ｇ‘鏍煎紡涓簓yyy-MM-dd");
		} else if ("5".equals(type)) {
			TUtils.toast("qq鏍煎紡涓嶆纭�");
		}
	}

	// 杩涘害瀵硅瘽妗�
	private void showUploadDialog() {
		if (null == dialog) {
			dialog = CustomProgressDialog.createDialog(activity, false);
		}
		dialog.show();
	}

	public void onEventMainThread(Integer id) {
		if (R.id.zy_tsbl_bl_item_iv != id) {
			return;
		}
		Intent intent = new Intent(activity, MultiImageSelectorActivity.class);
		// 鏄惁鏄剧ず鎷嶆憚鍥剧墖
		intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
		// 鏈�澶у彲閫夋嫨鍥剧墖鏁伴噺
		intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, adapter.getMaxSize());
		// 閫夋嫨妯″紡
		intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
		// 榛樿閫夋嫨
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
			p = Pattern.compile("^[1][3,4,5,8][0-9]{9}$"); // 楠岃瘉鎵嬫満鍙�
			m = p.matcher(str);
			flag = m.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	/**
	 * 鐢佃瘽鍙风爜楠岃瘉
	 */
	public static boolean isPhone(String str) {
		Pattern p1 = null;
		Matcher m = null;
		boolean flag = false;
		p1 = Pattern.compile("^(0\\d{2}-\\d{5,10}(-\\d{1,4})?)|(0\\d{3}-\\d{7,8}(-\\d{1,4})?)$"); // 楠岃瘉甯﹀尯鍙风殑
		try {
			m = p1.matcher(str);
			flag = m.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	/**
	 * 楠岃瘉Email鍦板潃鐨勬纭��
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
	 * 鍒ゆ柇鏃ユ湡鏍煎紡鍜岃寖鍥�
	 */
	public static boolean isDate(String date) {
		boolean flag = false;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // yyyy骞�
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
