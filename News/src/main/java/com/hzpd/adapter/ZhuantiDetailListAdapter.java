package com.hzpd.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.SubjectItemColumnsBean;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.ViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class ZhuantiDetailListAdapter extends BaseAdapter {

	private Activity context;
	private LayoutInflater inflater;
	private ImageLoader mImageLoader;

	private float fontSize = 0;// 字体大小

	private LinkedHashMap<SubjectItemColumnsBean, List<NewsBean>> columnList;

	public ZhuantiDetailListAdapter(Activity context) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		mImageLoader = ImageLoader.getInstance();
		columnList = new LinkedHashMap<SubjectItemColumnsBean, List<NewsBean>>();

		fontSize = SPUtil.getInstance().getTextSize();
	}

	public void appendData(SubjectItemColumnsBean column, List<NewsBean> list,
	                       Boolean isClearOldList) {

		List<NewsBean> oldList = columnList.get(column);
		if (null == oldList) {
			oldList = new ArrayList<NewsBean>();
		} else {
			if (isClearOldList) {
				oldList.clear();
			}
		}
		oldList.addAll(list);
		columnList.put(column, oldList);

	}

	public void clearData() {
		columnList.clear();
	}

	@Override
	public int getCount() {
		if (0 == columnList.size()) {
			return 0;
		} else {
			int columnCounts = 0;
			Set<SubjectItemColumnsBean> sets = columnList.keySet();
			for (SubjectItemColumnsBean sicb : sets) {
				columnCounts += 1;
				List<NewsBean> nbList = columnList.get(sicb);
				if (null != nbList) {
					columnCounts += nbList.size();
				}
			}
			return columnCounts;
		}

	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		int columnCounts = 0;
		Set<SubjectItemColumnsBean> sets = columnList.keySet();
		for (SubjectItemColumnsBean sicb : sets) {
			if (position == columnCounts) {
				return 0;
			} else {
				columnCounts += 1;
				List<NewsBean> nbList = columnList.get(sicb);
				if (null != nbList && nbList.size() > 0) {
					columnCounts += nbList.size();
					if (position < columnCounts) {
						return 1;
					}
				}
			}
		}

		return 0;
	}

	@Override
	public Object getItem(int position) {
		NewsBean nb = null;
		int columnCounts = 0;
		Set<SubjectItemColumnsBean> sets = columnList.keySet();
		for (SubjectItemColumnsBean sicb : sets) {
			if (position == columnCounts) {
				return sicb.getCname();
			} else {
				columnCounts += 1;
				List<NewsBean> nbList = columnList.get(sicb);
				if (null != nbList && nbList.size() > 0) {
					if (position >= columnCounts
							&& position < columnCounts + nbList.size()) {
						nb = nbList.get((position - columnCounts)
								% nbList.size());
						break;
					}
					columnCounts += nbList.size();
				}
			}
		}

		return nb;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		int type = getItemViewType(position);
		if (null == convertView) {
			if (0 == type) {
				convertView = inflater.inflate(R.layout.zhuanti_column_item,
						parent, false);
			} else {
				convertView = inflater.inflate(R.layout.lehuo_list_item_layout,
						parent, false);
			}
		}

		if (0 == type) {
			TextView zhuanti_tv_column = ViewHolder.get(convertView,
					R.id.zhuanti_tv_column);
			String title = (String) getItem(position);
			zhuanti_tv_column.setText("" + title);
		} else {
			TextView title = ViewHolder
					.get(convertView, R.id.lehuo_content_txt);
			TextView sj = ViewHolder.get(convertView, R.id.lehuo_sj_txt);
			ImageView img = ViewHolder.get(convertView, R.id.lehuo_img_id);

			NewsBean nb = (NewsBean) getItem(position);

			String s[] = nb.getImgs();
			String simg = "";
			if (null != s && s.length > 0) {
				simg = s[0];
			}
			mImageLoader.displayImage(simg, img,
					DisplayOptionFactory.getOption(OptionTp.Big));

			title.setTextSize(fontSize);
			title.setText(nb.getTitle());
			sj.setText(nb.getUpdate_time());
		}

		return convertView;
	}

	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
	}

}
