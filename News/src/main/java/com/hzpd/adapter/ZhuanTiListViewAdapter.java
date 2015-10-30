package com.hzpd.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.SPUtil;

public class ZhuanTiListViewAdapter extends ListBaseAdapter<NewsBean> {

	private float fontSize = 0;// 字体大小
	private int number;

	public ZhuanTiListViewAdapter(Activity context) {
		super(context);
		fontSize = SPUtil.getInstance().getTextSize();
	}

	public void setFontSize(float mfontSize) {
		this.fontSize = mfontSize;
		notifyDataSetChanged();
	}

	@Override
	public View getMyView(int position, View convertView, ViewGroup parent) {

		if (null == convertView) {
			convertView = inflater.inflate(R.layout.lehuo_list_item_layout,
					parent, false);
		}

		TextView title = (TextView) convertView
				.findViewById(R.id.lehuo_content_txt);
		TextView sj = (TextView) convertView.findViewById(R.id.lehuo_sj_txt);
		ImageView img = (ImageView) convertView.findViewById(R.id.lehuo_img_id);

		NewsBean nb = list.get(position);

		String s[] = nb.getImgs();
		String simg = "";
		if (null != s && s.length > 0) {
			simg = s[0];
		}
		SPUtil.displayImage(simg, img,
				DisplayOptionFactory.getOption(OptionTp.Small));

		title.setTextSize(fontSize);
		title.setText(nb.getTitle());
		sj.setText(nb.getUpdate_time());

		return convertView;
	}

}