package com.hzpd.adapter;import android.content.Context;import android.support.v7.widget.RecyclerView;import android.view.LayoutInflater;import android.view.View;import android.view.ViewGroup;import android.widget.ImageView;import android.widget.TextView;import android.widget.Toast;import com.hzpd.hflt.R;import com.hzpd.modle.NewsChannelBean;import com.hzpd.modle.TagBean;import com.hzpd.modle.event.ClassifItemEvent;import com.hzpd.ui.App;import com.hzpd.utils.AvoidOnClickFastUtils;import com.hzpd.utils.DisplayOptionFactory;import com.hzpd.utils.Log;import com.hzpd.utils.SPUtil;import com.hzpd.utils.SerializeUtil;import java.io.File;import java.util.ArrayList;import java.util.HashMap;import java.util.List;import de.greenrobot.event.EventBus;public class ClassifyItemListAdapter extends RecyclerView.Adapter {    private LayoutInflater mInflater;    List<TagBean> list = null;    public ClassifyItemListAdapter(Context context) {        this.mInflater = LayoutInflater.from(context);        list = new ArrayList<>();    }    public ClassifyItemListAdapter(Context context, List<TagBean> list) {        this.mInflater = LayoutInflater.from(context);        this.list = list;    }    public class ItemViewHolder extends RecyclerView.ViewHolder {        public ItemViewHolder(View v) {            super(v);        }        View classify_item;        ImageView mImg;        TextView mTxt;        TextView mTxt_num;        TextView tv_subscribe;    }    public void appendData(List<TagBean> data, boolean isClearOld) {        if (data == null) {            return;        }        if (isClearOld) {            list.clear();        }        list.addAll(data);        notifyDataSetChanged();    }    @Override    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {        View view = mInflater.inflate(R.layout.classify_item_list_layout,                parent, false);        ItemViewHolder viewHolder = new ItemViewHolder(view);        viewHolder.classify_item = view.findViewById(R.id.classify_item);        viewHolder.mImg = (ImageView) view                .findViewById(R.id.id_index_gallery_item_image);        viewHolder.mTxt = (TextView) view.findViewById(R.id.id_index_gallery_item_text);        viewHolder.mTxt_num = (TextView) view.findViewById(R.id.id_index_gallery_item_num_text);        viewHolder.tv_subscribe = (TextView) view.findViewById(R.id.tv_subscribe);        return viewHolder;    }    @Override    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {        final ItemViewHolder viewHolder = (ItemViewHolder) holder;        final TagBean tagBean = list.get(position);        if (tagBean.getName() != null) {            viewHolder.mTxt.setText(tagBean.getName());        }        if (tagBean.getIcon() != null) {            SPUtil.displayImage(tagBean.getIcon(), viewHolder.mImg                    , DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Personal_center_News));        }        if (tagBean.getNum() != null) {            viewHolder.mTxt_num.setText("" + tagBean.getNum());        }        viewHolder.tv_subscribe.setOnClickListener(new View.OnClickListener() {            @Override            public void onClick(View v) {                Toast.makeText(viewHolder.tv_subscribe.getContext(), "订阅。。。", Toast.LENGTH_SHORT).show();                Log.i("viewHolder.tv_subscribe","viewHolder.tv_subscribe");            }        });        viewHolder.classify_item.setOnClickListener(new View.OnClickListener() {            @Override            public void onClick(View v) {                if (AvoidOnClickFastUtils.isFastDoubleClick()) {                    return;                }                EventBus.getDefault().post(new ClassifItemEvent("" + tagBean.getId()));                Toast.makeText(viewHolder.classify_item.getContext(), "点击了" + tagBean.getId(), Toast.LENGTH_SHORT).show();            }        });    }    @Override    public int getItemCount() {        if (list.size() > 0) {            Log.i("list.size()", "list.size()" + list.size());            return list.size();        } else {            Log.i("list.size()", "list.size()=0");            return 0;        }    }}