package com.hzpd.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.color.tools.mytools.LogUtils;
import com.hzpd.hflt.R;
import com.hzpd.modle.ImageListSubBean;
import com.hzpd.modle.ImgListBean;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.SPUtil;

import java.util.List;

public class ImgListViewAdapter extends ListBaseAdapter<ImgListBean> {

    private final int TYPE_1 = 0;
    private final int TYPE_2 = 1;
    private final int TYPE_3 = 2;
    private final int TYPE_4 = 3;
    private final int TYPE_5 = 4;

    private float fontSize;

    public ImgListViewAdapter(Activity c) {
        super(c);
        fontSize = SPUtil.getInstance().getTextSize();
    }

    @Override
    public int getItemViewType(int position) {
        ImgListBean ilb = list.get(position);
        int count = 0;
        if (null != ilb.getSubphoto()) {
            count = ilb.getSubphoto().size();
        }
        if (count <= 1) {
            return TYPE_1;
        } else if (count == 2) {
            return TYPE_2;
        } else if (count == 3) {
            return TYPE_3;
        } else if (count == 4) {
            return TYPE_4;
        } else {
            return TYPE_5;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }


    public void setFontSize(float mfontSize) {
        this.fontSize = mfontSize;
        notifyDataSetChanged();
    }


    public void remove(int position) {
        list.remove(position);
        notifyDataSetChanged();
    }

    private static class ViewHolder1 {
        public ImageView img;
        public TextView title;
        public TextView pl;

        public ViewHolder1(View v) {
            img = (ImageView) v.findViewById(R.id.img_img1_id1);
            title = (TextView) v.findViewById(R.id.img_content_id1);
            pl = (TextView) v.findViewById(R.id.img_number_id1);
        }
    }

    // 添加的内容;
    private static class ViewHolder2 {
        public ImageView img1;
        public ImageView img2;
        public TextView title;
        public TextView pl;

        public ViewHolder2(View v) {
            img1 = (ImageView) v.findViewById(R.id.img_img2_id1);
            img2 = (ImageView) v.findViewById(R.id.img_img2_id2);
            title = (TextView) v.findViewById(R.id.img_content_id2);
            pl = (TextView) v.findViewById(R.id.img_number_id2);
        }
    }

    private static class ViewHolder3 {
        public ImageView img1;
        public ImageView img2;
        public ImageView img3;
        public TextView title;
        public TextView pl;

        public ViewHolder3(View v) {
            img1 = (ImageView) v.findViewById(R.id.img_img3_id1);
            img2 = (ImageView) v.findViewById(R.id.img_img3_id2);
            img3 = (ImageView) v.findViewById(R.id.img_img3_id3);
            title = (TextView) v.findViewById(R.id.img_content_id3);
            pl = (TextView) v.findViewById(R.id.img_number_id3);
        }
    }

    private static class ViewHolder4 {
        public ImageView img1;
        public ImageView img2;
        public ImageView img3;
        public ImageView img4;
        public TextView title;
        public TextView pl;

        public ViewHolder4(View v) {
            img1 = (ImageView) v.findViewById(R.id.img_img4_id1);
            img2 = (ImageView) v.findViewById(R.id.img_img4_id2);
            img3 = (ImageView) v.findViewById(R.id.img_img4_id3);
            img4 = (ImageView) v.findViewById(R.id.img_img4_id4);
            title = (TextView) v.findViewById(R.id.img_content_id4);
            pl = (TextView) v.findViewById(R.id.img_number_id4);

        }
    }

    private static class ViewHolder5 {
        public ImageView img1;
        public ImageView img2;
        public ImageView img3;
        public ImageView img4;
        public ImageView img5;
        public TextView title;
        public TextView pl;

        public ViewHolder5(View v) {
            img1 = (ImageView) v.findViewById(R.id.img_img5_id1);
            img2 = (ImageView) v.findViewById(R.id.img_img5_id2);
            img3 = (ImageView) v.findViewById(R.id.img_img5_id3);
            img4 = (ImageView) v.findViewById(R.id.img_img5_id4);
            img5 = (ImageView) v.findViewById(R.id.img_img5_id5);
            title = (TextView) v.findViewById(R.id.img_content_id5);
            pl = (TextView) v.findViewById(R.id.img_number_id5);
        }
    }

    @Override
    public View getMyView(int position, View convertView, ViewGroup parent) {
        ViewHolder1 holder1 = null;
        ViewHolder2 holder2 = null;
        ViewHolder3 holder3 = null;
        ViewHolder4 holder4 = null;
        ViewHolder5 holder5 = null;

        int type = getItemViewType(position);
        ImgListBean ilb = list.get(position);

        LogUtils.i("type--->" + type + " p-->" + position);

        if (convertView == null) {
            switch (type) {
                case TYPE_2: {
                    convertView = inflater.inflate(R.layout.img_pic_two, parent, false);
                    holder2 = new ViewHolder2(convertView);
                    convertView.setTag(holder2);
                }
                break;
                case TYPE_3: {
                    convertView = inflater.inflate(R.layout.img_pic_three, parent, false);
                    holder3 = new ViewHolder3(convertView);
                    convertView.setTag(holder3);
                }
                break;
                case TYPE_4: {
                    convertView = inflater.inflate(R.layout.img_pic_four, parent, false);
                    holder4 = new ViewHolder4(convertView);
                    convertView.setTag(holder4);
                }
                break;
                case TYPE_5: {
                    convertView = inflater.inflate(R.layout.img_pic_five, parent, false);
                    holder5 = new ViewHolder5(convertView);
                    convertView.setTag(holder5);
                }
                break;
                default: {
                    convertView = inflater.inflate(R.layout.img_pic_one, parent, false);
                    holder1 = new ViewHolder1(convertView);
                    convertView.setTag(holder1);
                }
                break;
            }
        } else {
            switch (type) {
                case TYPE_2: {
                    holder2 = (ViewHolder2) convertView.getTag();
                }
                break;
                case TYPE_3: {
                    holder3 = (ViewHolder3) convertView.getTag();
                }
                break;
                case TYPE_4: {
                    holder4 = (ViewHolder4) convertView.getTag();
                }
                break;
                case TYPE_5: {
                    holder5 = (ViewHolder5) convertView.getTag();
                }
                break;
                default: {
                    holder1 = (ViewHolder1) convertView.getTag();
                }
                break;
            }
        }

        List<ImageListSubBean> allImg = ilb.getSubphoto();
        Context context = convertView.getContext();
        switch (type) {
            case TYPE_2: {
                holder2.title.setTextSize(fontSize);
                holder2.pl.setTextSize(fontSize);
                holder2.title.setText(ilb.getTitle() + "");

                if (allImg != null && allImg.size() > 1) {
                    holder2.pl.setText(context.getString(R.string.prompt_images, allImg.size()));
                    SPUtil.displayImage(allImg.get(0).getSubphoto(), holder2.img1,
                            DisplayOptionFactory.getOption(OptionTp.Small));
                    SPUtil.displayImage(allImg.get(1).getSubphoto(), holder2.img2,
                            DisplayOptionFactory.getOption(OptionTp.Small));
                } else {
                    holder2.pl.setText(context.getString(R.string.prompt_images, 0));
                }
            }
            break;
            case TYPE_3: {
                holder3.title.setTextSize(fontSize);
                holder3.pl.setTextSize(fontSize);
                holder3.title.setText(ilb.getTitle() + "");

                if (allImg != null && allImg.size() > 2) {
                    holder3.pl.setText(context.getString(R.string.prompt_images, allImg.size()));
                    SPUtil.displayImage(allImg.get(0).getSubphoto(), holder3.img1,
                            DisplayOptionFactory.getOption(OptionTp.Small));
                    SPUtil.displayImage(allImg.get(1).getSubphoto(), holder3.img2,
                            DisplayOptionFactory.getOption(OptionTp.Small));
                    SPUtil.displayImage(allImg.get(2).getSubphoto(), holder3.img3,
                            DisplayOptionFactory.getOption(OptionTp.Small));
                } else {
                    holder3.pl.setText(context.getString(R.string.prompt_images, 0));
                }
            }
            break;
            case TYPE_4: {
                holder4.title.setTextSize(fontSize);
                holder4.pl.setTextSize(fontSize);
                holder4.title.setText(ilb.getTitle() + "");

                if (allImg != null && allImg.size() > 3) {
                    holder4.pl.setText(context.getString(R.string.prompt_images, allImg.size()));
                    SPUtil.displayImage(allImg.get(0).getSubphoto(), holder4.img1,
                            DisplayOptionFactory.getOption(OptionTp.Small));
                    SPUtil.displayImage(allImg.get(1).getSubphoto(), holder4.img2,
                            DisplayOptionFactory.getOption(OptionTp.Small));
                    SPUtil.displayImage(allImg.get(2).getSubphoto(), holder4.img3,
                            DisplayOptionFactory.getOption(OptionTp.Small));
                    SPUtil.displayImage(allImg.get(3).getSubphoto(), holder4.img4,
                            DisplayOptionFactory.getOption(OptionTp.Small));
                } else {
                    holder4.pl.setText(context.getString(R.string.prompt_images, 0));
                }
            }
            break;
            case TYPE_5: {
                holder5.title.setTextSize(fontSize);
                holder5.pl.setTextSize(fontSize);
                holder5.title.setText(ilb.getTitle() + "");

                if (allImg != null && allImg.size() > 4) {
                    holder5.pl.setText(context.getString(R.string.prompt_images, allImg.size()));
                    SPUtil.displayImage(allImg.get(0).getSubphoto(), holder5.img1,
                            DisplayOptionFactory.getOption(OptionTp.Small));
                    SPUtil.displayImage(allImg.get(1).getSubphoto(), holder5.img2,
                            DisplayOptionFactory.getOption(OptionTp.Small));
                    SPUtil.displayImage(allImg.get(2).getSubphoto(), holder5.img3,
                            DisplayOptionFactory.getOption(OptionTp.Small));
                    SPUtil.displayImage(allImg.get(3).getSubphoto(), holder5.img4,
                            DisplayOptionFactory.getOption(OptionTp.Small));
                    SPUtil.displayImage(allImg.get(4).getSubphoto(), holder5.img5,
                            DisplayOptionFactory.getOption(OptionTp.Small));
                } else {
                    holder5.pl.setText(context.getString(R.string.prompt_images, 0));
                }
            }
            break;
            default: {
                holder1.title.setTextSize(fontSize);
                holder1.pl.setTextSize(fontSize);
                holder1.title.setText(ilb.getTitle() + "");

                if (allImg != null && allImg.size() > 0) {
                    holder1.pl.setText(context.getString(R.string.prompt_images, allImg.size()));
                    SPUtil.displayImage(allImg.get(0).getSubphoto(), holder1.img,
                            DisplayOptionFactory.getOption(OptionTp.Small));
                } else {
                    holder1.pl.setText(context.getString(R.string.prompt_images, 0));
                }
            }
        }

        return convertView;
    }
}
