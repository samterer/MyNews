package com.hzpd.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.modle.VideoItemBean;
import com.hzpd.utils.CalendarUtil;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.SPUtil;

public class VideoAdapter extends ListBaseAdapter<VideoItemBean> {

	private float fontSize;

	private int[] icon_img;

	public VideoAdapter(Activity c) {
		super(c);
		icon_img = new int[]{R.drawable.zq_video_green, R.drawable.zq_video_orange
				, R.drawable.zq_video_pink, R.drawable.zq_video_yellow};

		fontSize = SPUtil.getInstance().getTextSize();
	}


	public void setFontSize(float mfontSize) {
		this.fontSize = mfontSize;
		notifyDataSetChanged();
	}

	private static class ViewHolderb {
		TextView vib_tv_title;
		TextView zq_video_tv_updatetime;
		TextView zq_video_tv_comments;
		ImageView vib_iv_img;
		ImageView vib_iv_icon;

		public ViewHolderb(View view) {
			vib_tv_title= (TextView) view.findViewById(R.id.vib_tv_title);
			zq_video_tv_updatetime= (TextView) view.findViewById(R.id.zq_video_tv_updatetime);
			zq_video_tv_comments= (TextView) view.findViewById(R.id.zq_video_tv_comments);
			vib_iv_img= (ImageView) view.findViewById(R.id.vib_iv_img);
			vib_iv_icon= (ImageView) view.findViewById(R.id.vib_iv_icon);
		}
	}

	@Override
	public View getMyView(int position, View convertView, ViewGroup parent) {
		ViewHolderb holderBig;

		final VideoItemBean bean1 = list.get(position);

		if (null == convertView) {
			convertView = inflater.inflate(R.layout.video_item_big,
					parent, false);
			holderBig = new ViewHolderb(convertView);
			convertView.setTag(holderBig);
		} else {
			holderBig = (ViewHolderb) convertView.getTag();
		}

		holderBig.vib_tv_title.setTextSize(fontSize);
		holderBig.vib_tv_title.setText(bean1.getTitle());


		holderBig.zq_video_tv_updatetime.setText(CalendarUtil.friendlyTime(bean1.getTime(), context));

//		holderBig.zq_video_tv_comments.setVisibility(View.VISIBLE);

		holderBig.vib_iv_icon.setImageResource(icon_img[position % icon_img.length]);

		SPUtil.displayImage(bean1.getMainpic(), holderBig.vib_iv_img,
				DisplayOptionFactory.getOption(OptionTp.Small));

		return convertView;
	}


}