package com.hzpd.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hzpd.adapter.editcolumn.DragAdapter;
import com.hzpd.adapter.editcolumn.DragGridView;
import com.hzpd.adapter.editcolumn.LastEditColumnAdapter;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsChannelBean;
import com.hzpd.modle.event.ChannelSortedList;
import com.hzpd.ui.App;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.SerializeUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.greenrobot.event.EventBus;

public class MyEditColumnActivity extends SBaseActivity {

	@ViewInject(R.id.editcolumn_dragGridView)
	private DragGridView editcolumn_dragGridView;
	@ViewInject(R.id.editcolumn_gridview)
	private GridView editcolumn_gridview;

	@ViewInject(R.id.stitle_tv_content)
	private TextView stitle_tv_content;

	private SerializeUtil<List<NewsChannelBean>> mSaveTitleData;

	private LastEditColumnAdapter myAllAdapter;//所有title适配器
	private DragAdapter adapter;    //显示条目适配器

	//适配器内容
	private List<NewsChannelBean> titleData;
	private List<NewsChannelBean> myAllList;
	private HashMap<String, NewsChannelBean> saveTitleMap;

	private String channelJsonPath;
	@ViewInject(R.id.editcolumn_item_tv)
	private TextView editcolumn_item_tv;

	private ChannelSortedList csl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editcolumn_my_layout);
		ViewUtils.inject(this);
		stitle_tv_content.setText("栏目订阅");
		init();
		getChannelJson();
	}

	private void init() {
		channelJsonPath = App.getInstance().getJsonFileCacheRootDir();
		saveTitleMap = new HashMap<String, NewsChannelBean>();
		mSaveTitleData = new SerializeUtil<List<NewsChannelBean>>();
		titleData = mSaveTitleData.readyDataToFile(App.getInstance()
				.getJsonFileCacheRootDir()
				+ File.separator
				+ App.mTitle);

		for (int i = 0; i < titleData.size(); i++) {
			NewsChannelBean nb = titleData.get(i);
			if ("84".equals(nb.getTid())) {
				if (0 != i) {
					titleData.remove(i);
					titleData.add(0, nb);
				}
				break;
			}
		}


		myAllList = new ArrayList<NewsChannelBean>();
		myAllAdapter = new LastEditColumnAdapter(this);
		editcolumn_gridview.setAdapter(myAllAdapter);
		csl = new ChannelSortedList();
	}

	private void reReadTitle() {
		titleData = mSaveTitleData.readyDataToFile(App.getInstance()
				.getJsonFileCacheRootDir()
				+ File.separator
				+ App.mTitle);
		adapter = new DragAdapter(this, titleData);
		editcolumn_dragGridView.setAdapter(adapter);

		editcolumn_dragGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
			                        final int position, long id) {
				LogUtils.i("position-->" + position);
				final NewsChannelBean ncb = titleData.get(position + DragAdapter.hiddenNum);
				titleData.remove(position + DragAdapter.hiddenNum);
				adapter.setList(titleData);
				myAllAdapter.addData(ncb);

//				LogUtils.i("startx:"+(view.getX()+editcolumn_dragGridView.getX())
//						+" starty:"+(view.getY()+editcolumn_dragGridView.getY()));
//				editcolumn_gridview.postDelayed(new Runnable() {
//					@Override
//					public void run() {
//						View endView=editcolumn_dragGridView.getChildAt(editcolumn_gridview.getChildCount()-1);
//						LogUtils.i("endx:"+(endView.getX()+editcolumn_gridview.getX())
//								+" endy:"+(endView.getY()+editcolumn_dragGridView.getY()));
//					
//						addAnim(ncb
//								,true
//								,(view.getX()+editcolumn_dragGridView.getX())
//								,(view.getY()+editcolumn_dragGridView.getY())
//								,(endView.getX()+editcolumn_gridview.getX())
//								,(endView.getY()+editcolumn_gridview.getY())
//								,position);
//					}
//				}, 150);
//				
			}
		});
	}

	/**
	 * @param ncb
	 * @param from     true取消订阅
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 * @param position
	 */
	public void addAnim(NewsChannelBean ncb, final boolean from,
	                    float startX, float startY
			, float endX, float endY, final int position) {
		PropertyValuesHolder pvhX = null;
		PropertyValuesHolder pvhY = null;

		pvhX = PropertyValuesHolder.ofFloat("x", startX, endX);
		pvhY = PropertyValuesHolder.ofFloat("y", startY, endY);

		editcolumn_item_tv.setText(ncb.getCnname());

		ObjectAnimator objAnim = ObjectAnimator
				.ofPropertyValuesHolder(editcolumn_item_tv, pvhX, pvhY);

		objAnim.setDuration(500);
		objAnim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				super.onAnimationStart(animation);
				LogUtils.i("animStart");
				editcolumn_item_tv.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				LogUtils.i("animend");
				editcolumn_item_tv.setVisibility(View.GONE);
				if (from) {
					LogUtils.i("titleData-->" + titleData.size() + "  " + (position + DragAdapter.hiddenNum));
					myAllAdapter.setAnim(false);
					titleData.remove(position + DragAdapter.hiddenNum);
					adapter.setList(titleData);
					myAllAdapter.notifyDataSetChanged();
				} else {
					LogUtils.i("myAllList-->" + myAllList.size() + " " + position);
					adapter.setAnim(false);
					myAllList.remove(position);
					myAllAdapter.setList(myAllList);
					adapter.notifyDataSetChanged();
				}
			}
		});

		objAnim.start();

	}

	private void getChannelJson() {
		File target = App.getFile(channelJsonPath + File.separator + "News");

		if (target.exists()) {
			String data = App.getFileContext(target);
			JSONObject obj = FjsonUtil.parseObject(data);

			if (null == obj) {
				target.delete();
				return;
			}

			JSONArray array = obj.getJSONArray("data");
			myAllList = JSONArray.parseArray(array.toJSONString(), NewsChannelBean.class);
			Collections.sort(myAllList);
			LogUtils.i("mAllbean-->" + titleData.size());

			for (NewsChannelBean stb : myAllList) {
				saveTitleMap.put(stb.getTid(), stb);
			}

			LogUtils.i("titleData-->" + titleData.size());

			Iterator<NewsChannelBean> iterator = titleData.iterator();
			while (iterator.hasNext()) {
				NewsChannelBean stbq = iterator.next();
				NewsChannelBean nb = saveTitleMap.get(stbq.getTid());
				if (null == nb) {
					iterator.remove();
				}
			}

			mSaveTitleData.writeDataToFile(titleData,
					App.getInstance().getJsonFileCacheRootDir() + File.separator
							+ App.mTitle);

			reReadTitle();
			LogUtils.i("mAllbean-->" + myAllList.size());

			Iterator<NewsChannelBean> itera = myAllList.iterator();
			while (itera.hasNext()) {
				NewsChannelBean ncb = itera.next();
				for (int i = 0; i < titleData.size(); i++) {
					NewsChannelBean stb = titleData.get(i);
					if (stb.getTid().equals(ncb.getTid())) {
						itera.remove();
						break;
					}
				}
			}

			if (myAllList != null) {
				myAllAdapter.setList(myAllList);
				LogUtils.i("myAllList-->" + myAllList.size());
				LogUtils.i("myAllAdapter-->" + myAllAdapter.getCount());

				editcolumn_gridview.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent,
					                        final View view, final int position, long id) {

						final NewsChannelBean ncb = (NewsChannelBean) myAllAdapter.getItem(position);

//						adapter.setAnim(true);
						titleData.add(ncb);
						adapter.setList(titleData);
						myAllList.remove(position);
						myAllAdapter.setList(myAllList);

//						editcolumn_dragGridView.postDelayed(new Runnable() {
//							@Override
//							public void run() {
//								LogUtils.i("startx:"+(view.getX()+editcolumn_gridview.getX())
//										+" starty:"+(view.getY()+editcolumn_gridview.getY()));
//								View endView=editcolumn_dragGridView.getChildAt(editcolumn_dragGridView.getChildCount()-1);
//								LogUtils.i("endx:"+(endView.getX()+editcolumn_dragGridView.getX())
//										+" endy:"+(endView.getY()+editcolumn_dragGridView.getY()));
//							
//								addAnim(ncb
//										,false
//										,(view.getX()+editcolumn_gridview.getX())
//										,(view.getY()+editcolumn_gridview.getY())
//										,(endView.getX()+editcolumn_dragGridView.getX())
//										,(endView.getY()+editcolumn_dragGridView.getY())
//										,position);
//							}
//						}, 150);
					}
				});
			}
		} else {
			httpUtils.download(
					InterfaceJsonfile.CHANNELLIST + "News"
					, target.getAbsolutePath()
					, new RequestCallBack<File>() {
						@Override
						public void onSuccess(ResponseInfo<File> responseInfo) {
							LogUtils.i("write  channel to file success");

							String data = App.getFileContext(responseInfo.result);
							JSONObject obj = FjsonUtil.parseObject(data);

							if (null == obj) {
								responseInfo.result.delete();
								return;
							}


							JSONArray array = obj.getJSONArray("data");
							myAllList = JSONArray.parseArray(array.toJSONString(), NewsChannelBean.class);
							Collections.sort(myAllList);
							LogUtils.i("mAllbean-->" + myAllList.size());
							for (NewsChannelBean stb : myAllList) {
								saveTitleMap.put(stb.getTid(), stb);
							}
							Iterator<NewsChannelBean> iterator = titleData.iterator();
							while (iterator.hasNext()) {
								NewsChannelBean stbq = iterator.next();
								NewsChannelBean nb = saveTitleMap.get(stbq.getTid());
								if (null == nb) {
									iterator.remove();
								}
							}

							mSaveTitleData.writeDataToFile(titleData,
									App.getInstance().getJsonFileCacheRootDir() + File.separator
											+ App.mTitle);

							reReadTitle();

							Iterator<NewsChannelBean> itera = myAllList.iterator();
							while (itera.hasNext()) {
								NewsChannelBean ncb = itera.next();
								for (NewsChannelBean stb : titleData) {
									if (!stb.getTid().equals(ncb.getTid())) {
										itera.remove();
										break;
									}
								}
							}

							if (myAllList != null) {
								myAllAdapter.setList(myAllList);
								LogUtils.i("myAllList-->" + myAllList.size());
								LogUtils.i("myAllAdapter-->" + myAllAdapter.getCount());

								editcolumn_gridview.setOnItemClickListener(new OnItemClickListener() {
									@Override
									public void onItemClick(AdapterView<?> parent,
									                        final View view, final int position, long id) {


										final NewsChannelBean ncb = (NewsChannelBean) myAllAdapter.getItem(position);

//									adapter.setAnim(true);
										titleData.add(ncb);
										adapter.setList(titleData);
										myAllList.remove(position);
										myAllAdapter.setList(myAllList);


//									editcolumn_dragGridView.postDelayed(new Runnable() {
//										@Override
//										public void run() {
//											LogUtils.i("startx:"+(view.getX()+editcolumn_gridview.getX())
//													+" starty:"+(view.getY()+editcolumn_gridview.getY()));
//											View endView=editcolumn_dragGridView.getChildAt(editcolumn_dragGridView.getChildCount()-1);
//											LogUtils.i("endx:"+(endView.getX()+editcolumn_dragGridView.getX())
//													+" endy:"+(endView.getY()+editcolumn_dragGridView.getY()));
//										
//											addAnim(ncb
//													,false
//													,(view.getX()+editcolumn_gridview.getX())
//													,(view.getY()+editcolumn_gridview.getY())
//													,(endView.getX()+editcolumn_dragGridView.getX())
//													,(endView.getY()+editcolumn_dragGridView.getY())
//													,position);
//										}
//									}, 150);
									}
								});
							}

						}

						@Override
						public void onFailure(HttpException error, String msg) {
							LogUtils.i("write  channel to file Failed");

						}
					});
		}
	}

	private void writeData() {
		List<NewsChannelBean> saveTitleList = new ArrayList<NewsChannelBean>();
		for (int i = 0; i < titleData.size(); i++) {
			NewsChannelBean sb = new NewsChannelBean();
			sb.setCnname(titleData.get(i).getCnname());
			sb.setStyle(titleData.get(i).getStyle());
			sb.setTid(titleData.get(i).getTid());
			saveTitleList.add(sb);
		}
		mSaveTitleData.writeDataToFile(saveTitleList,
				App.getInstance().getJsonFileCacheRootDir() + File.separator
						+ App.mTitle);
		csl.setSaveTitleList(saveTitleList);
	}

	@OnClick(R.id.stitle_ll_back)
	private void goback(View v) {
		finish();
	}

	@Override
	protected void onDestroy() {
		writeData();
		EventBus.getDefault().post(csl);
		super.onDestroy();
	}

}
