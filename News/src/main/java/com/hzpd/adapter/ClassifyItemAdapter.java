package com.hzpd.adapter;import android.content.Context;import android.support.v7.widget.RecyclerView;import android.view.LayoutInflater;import android.view.View;import android.view.ViewGroup;import android.widget.ImageView;import android.widget.TextView;import com.hzpd.hflt.R;import com.hzpd.modle.TagBean;import com.hzpd.utils.Log;import java.util.ArrayList;import java.util.List;public class ClassifyItemAdapter extends RecyclerView.Adapter{    private LayoutInflater mInflater;    private List<Integer> mDatas;    List<TagBean> list = null;    public ClassifyItemAdapter(Context context,List<TagBean> list) {        this.mInflater = LayoutInflater.from(context);        this.list=list;    }    public static class ItemViewHolder extends RecyclerView.ViewHolder {        public ItemViewHolder(View arg0) {            super(arg0);        }        ImageView mImg;        TextView mTxt;    }    public void appendData(List<TagBean> data) {        if (data == null) {            return;        }        Log.i("appendData", "appendData   data" + data);        list.addAll(data);        Log.i("appendData", "appendData" + list.toString());        notifyDataSetChanged();    }    @Override    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {        View view = mInflater.inflate(R.layout.classify_item_layout,                parent, false);        ItemViewHolder viewHolder = new ItemViewHolder(view);        viewHolder.mImg = (ImageView) view                .findViewById(R.id.id_index_gallery_item_image);        viewHolder.mTxt= (TextView) view.findViewById(R.id.id_index_gallery_item_text);        return viewHolder;    }    @Override    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {        Log.i("","list.size()"+list.size());        Log.i("onBindViewHolder","onBindViewHolder"+list.get(position).toString());        ItemViewHolder viewHolder= (ItemViewHolder) holder;        TagBean tagBean=list.get(position);        if (tagBean.getName()!=null){            viewHolder.mTxt.setText(tagBean.getName());        }    }    @Override    public int getItemCount() {        if (list.size()>0)            Log.i("list.size()","list.size()"+list.size());        else            Log.i("list.size()","list.size()=0");        return list.size();    }}