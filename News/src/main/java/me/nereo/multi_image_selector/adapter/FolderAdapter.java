package me.nereo.multi_image_selector.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzpd.adapter.ListBaseAdapter;
import com.hzpd.hflt.R;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.ViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;

import me.nereo.multi_image_selector.bean.Folder;

public class FolderAdapter extends ListBaseAdapter<Folder> {

	private int lastSelected = 0;

	public FolderAdapter(Activity context) {
		super(context);
	}

	@Override
	public int getCount() {
		return list.size() + 1;
	}

	@Override
	public Folder getItem(int i) {
		if (i == 0)
			return null;
		return list.get(i - 1);
	}

	private int getTotalImageSize() {
		int result = 0;
		if (list != null && list.size() > 0) {
			for (Folder f : list) {
				result += f.images.size();
			}
		}
		return result;
	}

	public void setSelectIndex(int i) {
		if (lastSelected == i)
			return;

		lastSelected = i;
		notifyDataSetChanged();
	}

	public int getSelectIndex() {
		return lastSelected;
	}

	@Override
	public View getMyView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.img_list_item_folder, parent, false);
		}

		ImageView cover = ViewHolder.get(convertView, R.id.photo_selector_folder_cover);
		TextView name = ViewHolder.get(convertView, R.id.photo_selector_folder_name);
		TextView size = ViewHolder.get(convertView, R.id.photo_selector_folder_size);
		ImageView indicator = ViewHolder.get(convertView, R.id.photo_selector_folder_indicator);

		if (lastSelected == position) {
			indicator.setVisibility(View.VISIBLE);
		} else {
			indicator.setVisibility(View.INVISIBLE);
		}

		if (position == 0) {
			name.setText("所有图片");
			size.setText(getTotalImageSize() + "张");
			cover.setImageResource(R.drawable.img_default_error);

			ImageLoader.getInstance().displayImage("drawable://" + R.drawable.img_default_error, cover,
					DisplayOptionFactory.getOption(OptionTp.Small));
		} else {

			Folder data = getItem(position);
			name.setText(data.name);
			size.setText(data.images.size() + "张");

			ImageLoader.getInstance().displayImage("file://" + data.cover.path, cover,
					DisplayOptionFactory.getOption(OptionTp.Small));
		}

		return convertView;
	}

}
