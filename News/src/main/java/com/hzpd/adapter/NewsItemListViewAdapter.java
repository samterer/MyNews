package com.hzpd.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.modle.CommentsCountBean;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.ui.App;
import com.hzpd.ui.interfaces.I_Result;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.CalendarUtil;
import com.hzpd.utils.Constant;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.db.NewsListDbTask;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class NewsItemListViewAdapter extends ListBaseAdapter<NewsBean> {

	public static enum Itemtype {
		THREEPIC(0), LEFTPIC(1);
		private int type;

		private Itemtype(int type) {
			this.type = type;
		}

		public int getType() {
			return type;
		}
	}

	private SPUtil spu;
	private float fontSize = 0;//字体大小

	private HashMap<String, Integer> commentsMap;
	private HashSet<String> readedNewsSet;
	private NewsListDbTask newsListDbTask;


	public NewsItemListViewAdapter(Activity c) {
		super(c);
		spu = SPUtil.getInstance();
		commentsMap = new HashMap<String, Integer>();
		readedNewsSet = new HashSet<String>();
		newsListDbTask = new NewsListDbTask(context);

		fontSize = spu.getTextSize();
	}

	@Override
	public void appendData(List<NewsBean> data, boolean isClearOld) {
		super.appendData(data, isClearOld);
		LogUtils.i("list size-->" + data.size());
		getReaded(data);
		getCommentsCounts(data);
	}

	@Override
	public void appendDataTop(List<NewsBean> data, boolean isClearOld) {
		super.appendDataTop(data, isClearOld);

		getReaded(list);
		getCommentsCounts(list);
	}


	@Override
	public void clear() {
		super.clear();
		commentsMap.clear();
	}

	public void setReadedId(String nid) {
		readedNewsSet.add(nid);

		try {
			NewsBeanDB nbdb = new NewsBeanDB();
			nbdb.setNid(Integer.parseInt(nid));
			nbdb.setIsreaded(1);

			dbHelper.getNewsListDbUtils().update(nbdb
					, WhereBuilder.b("nid", "=", nid)
					, "isreaded");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getReaded(List<NewsBean> list) {
		if (null == list) {
			return;
		}
		for (final NewsBean nb : list) {

			newsListDbTask.isRead(nb.getNid(), new I_Result() {
				@Override
				public void setResult(Boolean flag) {
					if (flag) {
						readedNewsSet.add(nb.getNid());
					}
				}
			});
		}
	}

	@Override
	public int getViewTypeCount() {
		return Itemtype.values().length;
	}

	@Override
	public int getItemViewType(int position) {
		NewsBean nb = list.get(position);
		if ("4".equals(nb.getType())) {
			return Itemtype.THREEPIC.getType();
		} else {
			return Itemtype.LEFTPIC.getType();
		}
	}

	public void setFontSize(float mfontSize) {
		this.fontSize = mfontSize;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		VHThree vhThree = null;
		VHLeftPic vhLeftPic = null;

		int type = getItemViewType(position);

		//数据设置 
		NewsBean nb = list.get(position);

		if (convertView == null) {
			switch (type) {
				case 0: {
					convertView = inflater.inflate(
							R.layout.news_3_item_layout, parent, false);
					vhThree = new VHThree(convertView);
					convertView.setTag(vhThree);
				}
				break;
				case 1: {
					convertView = inflater.inflate(
							R.layout.news_list_item_layout, parent, false);
					vhLeftPic = new VHLeftPic(convertView);
					convertView.setTag(vhLeftPic);
				}
				break;
			}
		} else {
			switch (type) {
				case 0: {
					vhThree = (VHThree) convertView.getTag();
				}
				break;
				case 1: {
					vhLeftPic = (VHLeftPic) convertView.getTag();
				}
				break;
			}
		}


		switch (type) {
			case 0: {
				vhThree.tv3.setTextSize(fontSize);
				vhThree.newsitem_title.setTextSize(fontSize);
				vhThree.newsitem_title.setText(nb.getTitle());

				vhThree.tv3.setText(CalendarUtil.friendlyTime(nb.getUpdate_time()));
				if (readedNewsSet.contains(nb.getNid())) {
					vhThree.tv3.setTextColor(App.getInstance()
							.getResources().getColor(R.color.grey_font));
				} else {
					vhThree.tv3.setTextColor(App.getInstance()
							.getResources().getColor(R.color.black));
				}

				Integer counts = commentsMap.get(nb.getNid());
				if (null == counts) {
					counts = 0;
				}
				if (counts > 0) {
					vhThree.newsitem_comments.setVisibility(View.VISIBLE);
					nb.setComcount(counts + "");
					vhThree.newsitem_comments.setText(counts + "");
				} else {
					vhThree.newsitem_comments.setVisibility(View.GONE);
				}

				String s[] = nb.getImgs();
				if (s.length == 1) {
					mImageLoader.displayImage(s[0], vhThree.img0, DisplayOptionFactory.getOption(OptionTp.Small));
					mImageLoader.displayImage("", vhThree.img1, DisplayOptionFactory.getOption(OptionTp.Small));
					mImageLoader.displayImage("", vhThree.img2, DisplayOptionFactory.getOption(OptionTp.Small));
				} else if (s.length == 2) {
					mImageLoader.displayImage(s[0], vhThree.img0, DisplayOptionFactory.getOption(OptionTp.Small));
					mImageLoader.displayImage(s[1], vhThree.img1, DisplayOptionFactory.getOption(OptionTp.Small));
					mImageLoader.displayImage("", vhThree.img2, DisplayOptionFactory.getOption(OptionTp.Small));
				} else if (s.length > 2) {
					mImageLoader.displayImage(s[0], vhThree.img0, DisplayOptionFactory.getOption(OptionTp.Small));
					mImageLoader.displayImage(s[1], vhThree.img1, DisplayOptionFactory.getOption(OptionTp.Small));
					mImageLoader.displayImage(s[2], vhThree.img2, DisplayOptionFactory.getOption(OptionTp.Small));
				}

				vhThree.newsitem_foot.setVisibility(View.GONE);
				//1新闻  2图集  3直播 4专题  5关联新闻 6视频
				if ("2".equals(nb.getRtype())) {
					vhThree.newsitem_foot.setImageResource(R.drawable.zq_subscript_album);
					vhThree.newsitem_foot.setVisibility(View.VISIBLE);
				} else if ("3".equals(nb.getRtype())) {
					vhThree.newsitem_foot.setImageResource(R.drawable.zq_subscript_live);
//				vhThree.newsitem_foot.setVisibility(View.VISIBLE);
				} else if ("4".equals(nb.getRtype())) {
					vhThree.newsitem_foot.setImageResource(R.drawable.zq_subscript_special);
					vhThree.newsitem_foot.setVisibility(View.VISIBLE);
				}
//			else if("5".equals(nb.getRtype())){
//				vhThree.newsitem_foot.setText("关联新闻");
//				vhThree.newsitem_foot.setVisibility(View.VISIBLE);
//			}
				else if ("6".equals(nb.getRtype())) {
					vhThree.newsitem_foot.setImageResource(R.drawable.zq_subscript_video);
					vhThree.newsitem_foot.setVisibility(View.VISIBLE);
				} else if ("7".equals(nb.getRtype())) {
					vhThree.newsitem_foot.setImageResource(R.drawable.zq_subscript_html);
					vhThree.newsitem_foot.setVisibility(View.VISIBLE);
				}

			}
			break;
			case 1: {
				vhLeftPic.newsitem_title.setTextSize(fontSize);
				vhLeftPic.newsitem_title.setText(nb.getTitle());
				if (readedNewsSet.contains(nb.getNid())) {
					vhLeftPic.newsitem_title.setTextColor(App.getInstance()
							.getResources().getColor(R.color.grey_font));
				} else {
					vhLeftPic.newsitem_title.setTextColor(App.getInstance()
							.getResources().getColor(R.color.black));
				}
				Integer counts = commentsMap.get(nb.getNid());
				if (null == counts) {
					counts = 0;
				}

				if (counts > 0) {
					vhLeftPic.newsitem_commentcount.setVisibility(View.VISIBLE);
					nb.setComcount(counts + "");
					vhLeftPic.newsitem_commentcount.setText(counts + "");
				} else {
					vhLeftPic.newsitem_commentcount.setVisibility(View.GONE);
				}

				vhLeftPic.newsitem_time.setText(CalendarUtil.friendlyTime(nb.getUpdate_time()));

				vhLeftPic.newsitem_img.setVisibility(View.VISIBLE);
				vhLeftPic.nli_foot.setVisibility(View.GONE);

				//1为文字新闻；2为图文新闻；3为视频新闻；7为专题；8为多图；9为直播
//			if ("3".equals(nb.getType())){
//				vhLeftPic.nli_foot.setText("视频");
//				vhLeftPic.nli_foot.setVisibility(View.VISIBLE);
//			}else if("2".equals(nb.getType())){
//				vhLeftPic.newsitem_img.setVisibility(View.VISIBLE);
				if ("1".equals(nb.getType())) {
					vhLeftPic.newsitem_img.setVisibility(View.GONE);
				}
//			}else if("9".equals(nb.getType())){
//				vhLeftPic.nli_foot.setText("直播");
//				vhLeftPic.nli_foot.setVisibility(View.VISIBLE);
//			}

				if (!"0".equals(nb.getSid())) {
					vhLeftPic.nli_foot.setImageResource(R.drawable.zq_subscript_special);
					vhLeftPic.nli_foot.setVisibility(View.VISIBLE);
				}

				//1新闻  2图集  3直播 4专题  5关联新闻 6视频 7引用
				if ("2".equals(nb.getRtype())) {
					vhLeftPic.nli_foot.setImageResource(R.drawable.zq_subscript_album);
					vhLeftPic.nli_foot.setVisibility(View.VISIBLE);
				} else if ("3".equals(nb.getRtype())) {
					vhLeftPic.nli_foot.setImageResource(R.drawable.zq_subscript_live);
//				vhLeftPic.nli_foot.setVisibility(View.VISIBLE);
				} else if ("4".equals(nb.getRtype())) {
					vhLeftPic.nli_foot.setImageResource(R.drawable.zq_subscript_special);
					vhLeftPic.nli_foot.setVisibility(View.VISIBLE);
				} else if ("7".equals(nb.getRtype())) {
					vhLeftPic.nli_foot.setImageResource(R.drawable.zq_subscript_html);
					vhLeftPic.nli_foot.setVisibility(View.VISIBLE);
				}
//			else if("5".equals(nb.getRtype())){
//				vhLeftPic.nli_foot.setText("关联新闻");
//				vhLeftPic.nli_foot.setVisibility(View.VISIBLE);
//			}
				else if ("6".equals(nb.getRtype())) {
					vhLeftPic.nli_foot.setImageResource(R.drawable.zq_subscript_video);
					vhLeftPic.nli_foot.setVisibility(View.VISIBLE);
				}

				if (vhLeftPic.newsitem_img.getVisibility() == View.VISIBLE
						&& null != nb.getImgs()
						&& nb.getImgs().length > 0) {
					mImageLoader.displayImage(nb.getImgs()[0], vhLeftPic.newsitem_img,
							DisplayOptionFactory.getOption(OptionTp.Small));
				} else {
					mImageLoader.displayImage("", vhLeftPic.newsitem_img,
							DisplayOptionFactory.getOption(OptionTp.Small));
				}

			}
			break;
		}

		return convertView;
	}

	//三联图
	private static class VHThree {
		@ViewInject(R.id.newsitem_title)
		private TextView newsitem_title;
		@ViewInject(R.id.news_3_tv_time)
		private TextView tv3;
		@ViewInject(R.id.news_3_item1)
		private ImageView img0;
		@ViewInject(R.id.news_3_item2)
		private ImageView img1;
		@ViewInject(R.id.news_3_item3)
		private ImageView img2;
		@ViewInject(R.id.newsitem_foot)
		private ImageView newsitem_foot;
		@ViewInject(R.id.newsitem_comments)
		private TextView newsitem_comments;

		public VHThree(View v) {
			ViewUtils.inject(this, v);

		}
	}

	//左边图片，右title，评论，时间，脚标
	private static class VHLeftPic {
		@ViewInject(R.id.newsitem_title)
		private TextView newsitem_title;
		@ViewInject(R.id.nli_foot)
		private ImageView nli_foot;
		@ViewInject(R.id.newsitem_commentcount)
		private TextView newsitem_commentcount;
		@ViewInject(R.id.newsitem_time)
		private TextView newsitem_time;
		@ViewInject(R.id.newsitem_img)
		private ImageView newsitem_img;

		public VHLeftPic(View v) {
			ViewUtils.inject(this, v);
		}
	}

	private void getCommentsCounts(List<NewsBean> list) {
		if (list.size() < 1) {
			return;
		}

		StringBuilder sb = new StringBuilder();
		for (NewsBean nb : list) {
			sb.append(nb.getNid());
			sb.append(",");
		}

		RequestParams params = RequestParamsUtils.getParams();
		params.addBodyParameter("type", Constant.TYPE.NewsA.toString());
		params.addBodyParameter("nids", sb.substring(0, sb.length() - 1));
		HttpUtils httpUtils = new HttpUtils();
		httpUtils.send(HttpMethod.POST
				, InterfaceJsonfile.commentsConts
				, params
				, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				LogUtils.i("getCommentsCounts-->" + responseInfo.result);

				JSONObject obj = FjsonUtil
						.parseObject(responseInfo.result);
				if (null == obj) {
					return;
				}

				if (200 == obj.getIntValue("code")) {

					List<CommentsCountBean> li = JSONObject.parseArray(obj.getString("data")
							, CommentsCountBean.class);
					if (null == li) {
						return;
					}
					for (CommentsCountBean cc : li) {
						Integer i = Integer.valueOf(0);
						try {
							i = Integer.parseInt(cc.getC_num());
						} catch (Exception e) {

						}
						commentsMap.put(cc.getNid(), i);
					}
					notifyDataSetChanged();
				}

			}

			@Override
			public void onFailure(HttpException error, String msg) {
				LogUtils.i("loginSubmit error-->" + msg);

			}
		});
	}

	@Override
	public View getMyView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

}
