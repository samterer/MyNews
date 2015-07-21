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
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

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
		@ViewInject(R.id.vib_tv_title)
		TextView vib_tv_title;
		@ViewInject(R.id.zq_video_tv_updatetime)
		TextView zq_video_tv_updatetime;
		@ViewInject(R.id.zq_video_tv_comments)
		TextView zq_video_tv_comments;
		@ViewInject(R.id.vib_iv_img)
		ImageView vib_iv_img;
		@ViewInject(R.id.vib_iv_icon)
		ImageView vib_iv_icon;


		public ViewHolderb(View view) {
			ViewUtils.inject(this, view);
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


		holderBig.zq_video_tv_updatetime.setText(CalendarUtil.friendlyTime(bean1.getTime()));

//		holderBig.zq_video_tv_comments.setVisibility(View.VISIBLE);

		holderBig.vib_iv_icon.setImageResource(icon_img[position % icon_img.length]);

		mImageLoader.displayImage(bean1.getMainpic(), holderBig.vib_iv_img,
				DisplayOptionFactory.getOption(OptionTp.Big));

		return convertView;
	}


}
