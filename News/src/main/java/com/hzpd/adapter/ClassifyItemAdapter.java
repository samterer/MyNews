package com.hzpd.adapter;import android.content.Context;import android.support.v7.widget.RecyclerView;import android.view.LayoutInflater;import android.view.View;import android.view.ViewGroup;import android.widget.ImageView;import android.widget.TextView;import android.widget.Toast;import com.hzpd.hflt.R;import com.hzpd.modle.TagBean;import com.hzpd.modle.event.ClassifItemEvent;import com.hzpd.utils.AvoidOnClickFastUtils;import com.hzpd.utils.DisplayOptionFactory;import com.hzpd.utils.Log;import com.hzpd.utils.SPUtil;import com.lidroid.xutils.ViewUtils;import java.util.ArrayList;import java.util.List;import de.greenrobot.event.EventBus;public class ClassifyItemAdapter extends RecyclerView.Adapter {    private LayoutInflater mInflater;    List<TagBean> list = null;    private View.OnClickListener onClickListener;    public ClassifyItemAdapter(Context context) {        this.mInflater = LayoutInflater.from(context);        list = new ArrayList<>();    }    public ClassifyItemAdapter(Context context, View.OnClickListener onClickListener) {        this.mInflater = LayoutInflater.from(context);        this.onClickListener = onClickListener;        list = new ArrayList<>();    }    public ClassifyItemAdapter(Context context, List<TagBean> list) {        this.mInflater = LayoutInflater.from(context);        this.list = list;    }    public class ItemViewHolder extends RecyclerView.ViewHolder {        public ItemViewHolder(View v) {            super(v);            ViewUtils.inject(this, v);            v.setOnClickListener(onClickListener);        }        View classify_item;        ImageView mImg;        TextView mTxt;    }    public void appendData(List<TagBean> data) {        if (data == null) {            return;        }        list.addAll(data);        notifyDataSetChanged();    }    @Override    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {        View view = mInflater.inflate(R.layout.classify_item_layout,                parent, false);        ItemViewHolder viewHolder = new ItemViewHolder(view);        viewHolder.classify_item = view.findViewById(R.id.classify_item);        viewHolder.mImg = (ImageView) view                .findViewById(R.id.id_index_gallery_item_image);        viewHolder.mTxt = (TextView) view.findViewById(R.id.id_index_gallery_item_text);        return viewHolder;    }    @Override    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {        final ItemViewHolder viewHolder = (ItemViewHolder) holder;        final TagBean tagBean = list.get(position);        if (tagBean.getName() != null) {            viewHolder.mTxt.setText(tagBean.getName());        }        if (tagBean.getIcon() != null) {            SPUtil.displayImage(tagBean.getIcon(), viewHolder.mImg                    , DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Personal_center_News));        }        viewHolder.classify_item.setOnClickListener(new View.OnClickListener() {            @Override            public void onClick(View v) {                if (AvoidOnClickFastUtils.isFastDoubleClick()) {                    return;                }                EventBus.getDefault().post(new ClassifItemEvent("" + tagBean.getId()));                Toast.makeText(viewHolder.classify_item.getContext(), "点击了", Toast.LENGTH_SHORT).show();            }        });    }    @Override    public int getItemCount() {        if (list.size() > 0)            Log.i("list.size()", "list.size()" + list.size());        else            Log.i("list.size()", "list.size()=0");        return list.size();    }}