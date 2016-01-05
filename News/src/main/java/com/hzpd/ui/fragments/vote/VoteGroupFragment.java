package com.hzpd.ui.fragments.vote;


import android.content.Context;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.hflt.R;
import com.hzpd.modle.vote.VoteItem;
import com.hzpd.modle.vote.VoteTitleBean;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.ui.fragments.action.ActionDetailActivity;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.OkHttpClientManager;
import com.hzpd.utils.Log;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.TUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.squareup.okhttp.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VoteGroupFragment extends BaseFragment {

	private VoteTitleBean vtb;


	private String androidId;    //
	private String newsid;        //
	private String subjectid;
	private String isRadio;//是否单选   0单  1多
	private String isVote;//是否可投票  0可  1不可
	private int num;     //指示器页标

	private List<ItemHolder> list;
	private List<VoteItem> voteList;
	private View view;

	private int height = 0;
	private Handler handler;

	private String optionId = "";
	private List<String> cb_optList;//多选option

	protected ImageLoader mImageLoader;
	protected DisplayImageOptions displayImageOptions;

	private LinearLayout vote_ll;
	private boolean isAdded = false;

	public VoteGroupFragment(VoteTitleBean vtb, String androidId, String newsid
			, String isVote, String isRadio, Context context, Handler handler, int num, String subjectid) {
		Log.i("test","isVote-->" + isVote);
		this.androidId = androidId;
		this.newsid = newsid;
		this.vtb = vtb;
		this.isRadio = isRadio;
		this.isVote = isVote;
		this.handler = handler;
		this.num = num;
		this.subjectid = subjectid;

		list = new ArrayList<ItemHolder>();
		voteList = new ArrayList<VoteItem>();
		cb_optList = new ArrayList<String>();

		initDownImage();
		getPic();
	}

	public String getTitle() {
		return vtb.getName();
	}

	public String getTitleId() {
		return vtb.getId();
	}
	private Object tag;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		inflater = LayoutInflater.from(getActivity());
		view = inflater.inflate(R.layout.vote_pager_layout, container, false);
		vote_ll = (LinearLayout) view.findViewById(R.id.vote_ll);
		tag= OkHttpClientManager.getTag();
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		init();
	}

	private void init() {
		if (isAdded) {
			return;
		}
		ItemHolder itemHolder = null;
		for (int i = 0; i < voteList.size(); i++) {
			VoteItem vi = voteList.get(i);
			if (0 == i % 2) {
				itemHolder = new ItemHolder(getActivity());
				itemHolder.setLeft(vi);
				if (i == voteList.size() - 1) {
					list.add(itemHolder);
				}
			} else {
				itemHolder.setRight(vi);
				list.add(itemHolder);
			}
		}

		for (ItemHolder ih : list) {
			vote_ll.addView(ih.ll);
		}
		Log.i("test","list-size->" + list.size());
		height = list.size();
		if (height > 0) {
			isAdded = true;
		}

	}

	private void getPic() {

		Map<String,String> params = RequestParamsUtils.getMaps();
		params.put("device", androidId);
		params.put("subjectid", vtb.getSubjectid());
		params.put("tyid", vtb.getId());
		params.put("page", "1");
		params.put("pagesize", "300");
		OkHttpClientManager.postAsyn(tag
				, InterfaceJsonfile.mVoteopts

				, new OkHttpClientManager.ResultCallback() {
			@Override
			public void onSuccess(Object response) {
				Log.i("test","获取投票选项列表信息-->" + response.toString());
				JSONObject obj = JSONObject.parseObject(response.toString());

				if (200 == obj.getIntValue("code")) {
					List<VoteItem> vlist = JSONObject.parseArray(obj.getJSONArray("data").toJSONString(), VoteItem.class);
					if (vlist != null) {
						voteList.addAll(vlist);
					}

					if (0 == num) {
						Message msg = handler.obtainMessage();
						msg.what = 111;
						msg.obj = (int) (Math.ceil(voteList.size() / 2.0));
						handler.sendMessage(msg);
						Log.i("test","sendMessage");
					}
				} else {
					TUtils.toast(obj.getString("msg"));
				}
			}

			@Override
			public void onFailure(Request request, Exception e) {
				Log.i("test","获取投票选项列表信息 failed");
			}
		}, params);
	}

	public List<ItemHolder> getItemHolder() {
		return list;
	}

	public List<String> getMultiVoted() {
		return cb_optList;
	}

	private class ItemHolder {
		LinearLayout ll;
		LinearLayout left;
		LinearLayout right;
		LinearLayout vote_ll_check1;
		LinearLayout vote_ll_check2;
		ImageView vote_img_touxiang1;
		ImageView vote_img_radio1;
		TextView vote_tv_name1;
		TextView vote_num_tv1;

		ImageView vote_img_touxiang2;
		ImageView vote_img_radio2;
		TextView vote_tv_name2;
		TextView vote_num_tv2;

		VoteItem viLeft;
		VoteItem viRight;

		private LayoutInflater inf;

		public ItemHolder(Context context) {
			inf = LayoutInflater.from(context);

			ll = (LinearLayout) inf.inflate(R.layout.vote_pager_item, null);
			left = (LinearLayout) ll.findViewById(R.id.vote_left_ll1);
			right = (LinearLayout) ll.findViewById(R.id.vote_left_ll2);
			vote_img_touxiang1 = (ImageView) ll.findViewById(R.id.vote_img_touxiang1);
			vote_img_radio1 = (ImageView) ll.findViewById(R.id.vote_img_radio1);
			vote_tv_name1 = (TextView) ll.findViewById(R.id.vote_tv_name1);
			vote_ll_check1 = (LinearLayout) ll.findViewById(R.id.vote_ll_check1);
			vote_ll_check2 = (LinearLayout) ll.findViewById(R.id.vote_ll_check2);
			vote_num_tv1 = (TextView) ll.findViewById(R.id.vote_num_tv1);

			vote_img_touxiang2 = (ImageView) ll.findViewById(R.id.vote_img_touxiang2);
			vote_img_radio2 = (ImageView) ll.findViewById(R.id.vote_img_radio2);
			vote_tv_name2 = (TextView) ll.findViewById(R.id.vote_tv_name2);
			vote_num_tv2 = (TextView) ll.findViewById(R.id.vote_num_tv2);
		}

		public void setLeft(final VoteItem vi) {
			viLeft = vi;
			String lurl = vi.getOption().getImgurl();
			Log.e("test","lurl-->" + lurl);
			mImageLoader.displayImage(lurl, vote_img_touxiang1, displayImageOptions);

//			vote_tv_name1.setText(vi.getOptionid()+"."+vi.getOption().getName());
			vote_tv_name1.setText(vi.getOption().getName());
			vote_num_tv1.setText(getString(R.string.prompt_votes, vi.getVotenum()));
			if ("0".equals(isRadio)) {
				vote_img_radio1.setBackgroundResource(R.drawable.vote_radio_unselected);

				if (viLeft.getOptionid().equals(optionId)) {
					vote_img_radio1.setBackgroundResource(R.drawable.vote_radio_selected);
				}

				if ("1".equals(viLeft.getOption().getStatus())) {
					vote_img_radio1.setBackgroundResource(R.drawable.vote_radio_selected);
				}
			} else {
				vote_img_radio1.setBackgroundResource(R.drawable.vote_cb_unselected);

				if (cb_optList.contains(viLeft.getOptionid())) {
					vote_img_radio1.setBackgroundResource(R.drawable.vote_cb_selected);
				}

				if ("1".equals(viLeft.getOption().getStatus())) {
					vote_img_radio1.setBackgroundResource(R.drawable.vote_cb_selected);
				}
			}

			left.setVisibility(View.VISIBLE);

			Log.i("test","isVoteleft-->" + isVote);

			if ("0".equals(isVote)) {
				vote_ll_check1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if ("0".equals(isRadio)) {
							optionId = viLeft.getOptionid();
							vote_img_radio1.setBackgroundResource(R.drawable.vote_radio_selected);
							Message msg = handler.obtainMessage();
							msg.what = 112;
							msg.obj = viLeft.getOptionid();
							handler.sendMessage(msg);
						} else {
							String s = (String) vote_img_radio1.getTag();
							if (s == null || "2".equals(s)) {
								vote_img_radio1.setBackgroundResource(R.drawable.vote_cb_selected);
								vote_img_radio1.setTag("1");
								cb_optList.add(viLeft.getOptionid());
							} else {
								vote_img_radio1.setBackgroundResource(R.drawable.vote_cb_unselected);
								vote_img_radio1.setTag("2");
								cb_optList.remove(viLeft.getOptionid());
							}
						}
					}
				});
			}
			vote_img_touxiang1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					gotoDetail(viLeft.getOptionid());
				}
			});

		}

		public void setRight(final VoteItem vi) {
			viRight = vi;
			String rurl = vi.getOption().getImgurl();
			Log.e("test","rurl-->" + rurl);
			mImageLoader.displayImage(rurl, vote_img_touxiang2, displayImageOptions);
//			vote_tv_name2.setText(vi.getOptionid()+"."+vi.getOption().getName());
			vote_tv_name2.setText(vi.getOption().getName());
			vote_num_tv2.setText(getString(R.string.prompt_votes, vi.getVotenum()));
			if ("0".equals(isRadio)) {
				vote_img_radio2.setBackgroundResource(R.drawable.vote_radio_unselected);

				if (viRight.getOptionid().equals(optionId)) {
					vote_img_radio2.setBackgroundResource(R.drawable.vote_radio_selected);
				}

				if ("1".equals(viRight.getOption().getStatus())) {
					vote_img_radio2.setBackgroundResource(R.drawable.vote_radio_selected);
				}

			} else {
				vote_img_radio2.setBackgroundResource(R.drawable.vote_cb_unselected);
				if (cb_optList.contains(viRight.getOptionid())) {
					vote_img_radio2.setBackgroundResource(R.drawable.vote_cb_selected);
				}

				if ("1".equals(viRight.getOption().getStatus())) {
					vote_img_radio2.setBackgroundResource(R.drawable.vote_cb_selected);
				}

			}

			right.setVisibility(View.VISIBLE);
			Log.i("test","isVoteright-->" + isVote);
			if ("0".equals(isVote)) {
				vote_ll_check2.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if ("0".equals(isRadio)) {
							optionId = viLeft.getOptionid();
							Message msg = handler.obtainMessage();
							msg.what = 112;
							msg.obj = viRight.getOptionid();
							handler.sendMessage(msg);
							vote_img_radio2.setBackgroundResource(R.drawable.vote_radio_selected);
						} else {
							String s = (String) vote_img_radio2.getTag();
							if (s == null || "2".equals(s)) {
								vote_img_radio2.setBackgroundResource(R.drawable.vote_cb_selected);
								vote_img_radio2.setTag("1");
								cb_optList.add(viRight.getOptionid());
							} else {
								vote_img_radio2.setBackgroundResource(R.drawable.vote_cb_unselected);
								vote_img_radio2.setTag("2");
								cb_optList.remove(viRight.getOptionid());
							}
						}
					}
				});
			}

			vote_img_touxiang2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					gotoDetail(viRight.getOptionid());
				}
			});
		}

		private void gotoDetail(String optionid) {
			Bundle args = new Bundle();
			args.putString("optionid", optionid);
			args.putString("newsid", newsid);
			args.putString("subjectid", subjectid);
			args.putString("isRadio", isRadio);
			args.putString("actionname", ((VoteDetailFragment) getParentFragment()).voteBaseinfo.getSubject());

			((ActionDetailActivity) activity).toVoteDetail(args);

		}

		public void removeOnclick() {
			vote_ll_check1.setOnClickListener(null);
			vote_ll_check2.setOnClickListener(null);
		}

	}

	public int getHeight() {
		init();
		return height;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void clear(String optionId) {
		setSelectedRadio(optionId);
		for (ItemHolder ih : list) {
			if (optionId.equals(ih.viLeft.getOptionid())) {
				if ("0".equals(isRadio)) {
					ih.vote_img_radio1.setBackgroundResource(R.drawable.vote_radio_selected);
				}
			} else {
				if ("0".equals(isRadio)) {
					ih.vote_img_radio1.setBackgroundResource(R.drawable.vote_radio_unselected);
				}
			}
			if (ih.viRight != null) {
				if (optionId.equals(ih.viRight.getOptionid())) {
					if ("0".equals(isRadio)) {
						ih.vote_img_radio2.setBackgroundResource(R.drawable.vote_radio_selected);
					}
				} else {
					if ("0".equals(isRadio)) {
						ih.vote_img_radio2.setBackgroundResource(R.drawable.vote_radio_unselected);
					}
				}
			}
		}
	}

	public void clearAll() {
		cb_optList.clear();
		optionId = "";
		for (ItemHolder ih : list) {
			if (optionId.equals(ih.viLeft.getOptionid())) {
				if ("0".equals(isRadio)) {
					ih.vote_img_radio1.setBackgroundResource(R.drawable.vote_radio_selected);
				} else {
					ih.vote_img_radio1.setBackgroundResource(R.drawable.vote_cb_selected);
				}
			} else {
				if ("0".equals(isRadio)) {
					ih.vote_img_radio1.setBackgroundResource(R.drawable.vote_radio_unselected);
				} else {
					ih.vote_img_radio1.setBackgroundResource(R.drawable.vote_cb_unselected);
				}
			}
			if (ih.viRight != null) {
				if (optionId.equals(ih.viRight.getOptionid())) {
					if ("0".equals(isRadio)) {
						ih.vote_img_radio2.setBackgroundResource(R.drawable.vote_radio_selected);
					} else {
						ih.vote_img_radio2.setBackgroundResource(R.drawable.vote_cb_selected);
					}
				} else {
					if ("0".equals(isRadio)) {
						ih.vote_img_radio2.setBackgroundResource(R.drawable.vote_radio_unselected);
					} else {
						ih.vote_img_radio2.setBackgroundResource(R.drawable.vote_cb_unselected);
					}
				}
			}
		}
	}

	public void setSelectedRadio(String optionId) {
		this.optionId = optionId;
	}

	public void setVoted() {
		isVote = "1";
		for (ItemHolder ih : list) {
			ih.removeOnclick();
		}
	}

	public void initDownImage() {
		mImageLoader = mImageLoader.getInstance();
		displayImageOptions = new DisplayImageOptions.Builder()
				.imageScaleType(ImageScaleType.EXACTLY)
				.showImageOnFail(R.drawable.default_bg)
				.showImageForEmptyUri(R.drawable.default_bg)
				.showImageOnLoading(R.drawable.default_bg)
				.cacheInMemory(true).cacheOnDisk(true)
				.bitmapConfig(Config.RGB_565)
				.displayer(new FadeInBitmapDisplayer(200))
				.build();

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		OkHttpClientManager.cancel(tag);
	}
}