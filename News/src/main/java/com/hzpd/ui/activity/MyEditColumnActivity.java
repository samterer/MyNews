package com.hzpd.ui.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hzpd.adapter.RecommendDragAdapter;
import com.hzpd.adapter.editcolumn.DragAdapter;
import com.hzpd.adapter.editcolumn.LastEditColumnAdapter;
import com.hzpd.custorm.DragGrid;
import com.hzpd.custorm.OtherGridView;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsChannelBean;
import com.hzpd.modle.event.ChangeChannelEvent;
import com.hzpd.modle.event.ChannelSortedList;
import com.hzpd.ui.App;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.AnalyticUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.Log;
import com.hzpd.utils.SerializeUtil;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.SystemBarTintManager;
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

public class MyEditColumnActivity extends MBaseActivity {

    @Override
    public String getAnalyticPageName() {
        return AnalyticUtils.SCREEN.edit;
    }

    @ViewInject(R.id.editcolumn_dragGridView)
    private DragGrid editcolumn_dragGridView;
    @ViewInject(R.id.editcolumn_gridview)
    private OtherGridView editcolumn_gridview;

    @ViewInject(R.id.stitle_tv_content)
    private TextView stitle_tv_content;

    private SerializeUtil<List<NewsChannelBean>> mSaveTitleData;

    private LastEditColumnAdapter myAllAdapter;//所有title适配器
    private RecommendDragAdapter dragAdapter;    //显示条目适配器

    //适配器内容
    private List<NewsChannelBean> channelData;
    private List<NewsChannelBean> myAllList;
    private HashMap<String, NewsChannelBean> saveTitleMap;

    private String channelJsonPath;
    @ViewInject(R.id.editcolumn_item_tv)
    private TextView editcolumn_item_tv;
    @ViewInject(R.id.text_editcolumn)
    private TextView text_editcolumn;
    @ViewInject(R.id.choose_tab)
    private TextView choose_tab;
    @ViewInject(R.id.editcolum_explain)
    private TextView editcolum_explain;

    private ChannelSortedList csl;


    private boolean isEdit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.editcolumn_my_layout);
            ViewUtils.inject(this);
            stitle_tv_content.setText(R.string.prompt_column_subscribe);
            text_editcolumn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isEdit) {
                        isEdit = true;
                        Log.e("isEdit", "isEdit" + true);
                        editcolum_explain.setVisibility(View.VISIBLE);
                        dragAdapter.isEditItem(isEdit);
                        editcolumn_dragGridView.isEditColumn(isEdit);
                        text_editcolumn.setText(getResources().getString(R.string.editcolumn_ok));
                    } else {
                        isEdit = false;
                        editcolum_explain.setVisibility(View.GONE);
                        dragAdapter.isEditItem(isEdit);
                        text_editcolumn.setText(getResources().getString(R.string.editcolumn_edit));
                        editcolumn_dragGridView.isEditColumn(isEdit);
                        Log.e("isEdit", "isEdit" + false);
                    }
                }
            });
            init();
            getChannelJson();


        } catch (Exception e) {
            e.printStackTrace();
        }
        super.changeStatusBar();
    }


    private void init() {
        channelJsonPath = App.getInstance().getAllDiskCacheDir()
                + File.separator;
        saveTitleMap = new HashMap<String, NewsChannelBean>();
        mSaveTitleData = new SerializeUtil<List<NewsChannelBean>>();
        // 读取缓存的频道信息，频道信息在WelcomeActivity中就已经请求过了
        channelData = mSaveTitleData.readyDataToFile(getChannelInfoCacheSavePath());
        if (channelData == null) {
            return;
        }
        Log.e("channelData", "channelData" + channelData.toString());
        // 不显示在新闻tab栏的频道列表
        myAllList = new ArrayList<>();
        myAllAdapter = new LastEditColumnAdapter(this);
        editcolumn_gridview.setAdapter(myAllAdapter);

        csl = new ChannelSortedList();
    }

    //修改频道缓存配置，清除缓存时不删除频道
    private String getChannelInfoCacheSavePath() {
        return App.getInstance().getAllDiskCacheDir()
                + File.separator
                + App.mTitle;
    }

    //TODO 是否有修改
    boolean modified = false;
    private int selPosition = -1;

    private void reReadVisibleChannel() {

        try {
            channelData = mSaveTitleData.readyDataToFile(getChannelInfoCacheSavePath());
            dragAdapter = new RecommendDragAdapter(this, channelData);
            editcolumn_dragGridView.setAdapter(dragAdapter);
            editcolumn_dragGridView.text_editcolumn = text_editcolumn;
            editcolumn_dragGridView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        final int position, long id) {
                    LogUtils.e("position-->" + position);
                    if (!isEdit) {
                        selPosition = position;
                        finish();
                        return;
                    }
                    if (position == 0) {
                        return;
                    }

                    modified = true;
                    final NewsChannelBean ncb = channelData.get(position + DragAdapter.hiddenNum);
                    channelData.remove(position + DragAdapter.hiddenNum);
                    dragAdapter.setList(channelData);
                    myAllAdapter.addData(ncb);
                }
            });
            editcolumn_dragGridView.setOnDragListener(new DragGrid.OnDragListener() {
                @Override
                public void onDrag() {
                    Log.e("editcolumn_dragGridView", "editcolumn_dragGridView--->onDrag");
                    modified = true;
                }
            });
//            editcolumn_dragGridView.setEditListener(new DragGrid.OnEditListener() {
//                @Override
//                public void onEdit(boolean isEdit) {
//
//                }
//            });
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                    LogUtils.i("channelData-->" + channelData.size() + "  " + (position + DragAdapter.hiddenNum));
                    myAllAdapter.setAnim(false);
                    channelData.remove(position + DragAdapter.hiddenNum);
                    dragAdapter.setList(channelData);
                    myAllAdapter.notifyDataSetChanged();
                } else {
                    LogUtils.i("myAllList-->" + myAllList.size() + " " + position);
                    dragAdapter.setAnim(false);
                    myAllList.remove(position);
                    myAllAdapter.setList(myAllList);
                    dragAdapter.notifyDataSetChanged();
                }
            }
        });

        objAnim.start();

    }

    private void getChannelJson() {
        String remoteChannelJson = channelJsonPath + File.separator + "News";
        File target = App.getFile(remoteChannelJson);
        if (target.exists()) {
            parseChannelJson(target);
        } else {
            httpUtils.download(
                    InterfaceJsonfile.CHANNELLIST + "News"
                    , target.getAbsolutePath()
                    , new RequestCallBack<File>() {
                        @Override
                        public void onSuccess(ResponseInfo<File> responseInfo) {
                            Log.i(getLogTag(), "write channel to file success");
                            parseChannelJson(responseInfo.result);
                        }

                        @Override
                        public void onFailure(HttpException error, String msg) {
                            Log.i(getLogTag(), "write channel to file Failed");
                        }
                    });
        }
    }

    private void parseChannelJson(File target) {
        Log.d(getLogTag(), target.getAbsolutePath());

        // 获取json
        String data = App.getFileContext(target);
        JSONObject obj = FjsonUtil.parseObject(data);

        if (null == obj) {
            target.delete();
            return;
        }

        // 读取json解析出所有的频道
        JSONArray array = obj.getJSONArray("data");
        myAllList = JSONArray.parseArray(array.toJSONString(), NewsChannelBean.class);
        Collections.sort(myAllList);
        addLocalChannels(myAllList);
        Log.i(getLogTag(), "Channel count : " + myAllList.size() + "::::" + myAllList.toString());

        // 将新获取的频道临时保存到map中
        for (NewsChannelBean stb : myAllList) {
            saveTitleMap.put(stb.getTid(), stb);
        }

        // 遍历用户可见的频道列表，如果用户可见的频道在新获取的频道列表中不存在，就删除该频道
        if (channelData != null) {
            Iterator<NewsChannelBean> iterator = channelData.iterator();
            while (iterator.hasNext()) {
                NewsChannelBean stbq = iterator.next();
                NewsChannelBean nb = saveTitleMap.get(stbq.getTid());
                if (null == nb) {
                    Log.d(getLogTag(), "removed " + stbq.getCnname());
                    iterator.remove();
                }
            }
        }

        // 更新用户可见的频道列表缓存
        mSaveTitleData.writeDataToFile(channelData, getChannelInfoCacheSavePath());


        reReadVisibleChannel();

        // 筛选出用户不可见的频道
        Log.d(getLogTag(), "尝试筛选出用户不可见的频道");
        Iterator<NewsChannelBean> itera = myAllList.iterator();
        while (itera.hasNext()) {
            NewsChannelBean ncb = itera.next();
            for (NewsChannelBean stb : channelData) {
                if (stb.getTid().equals(ncb.getTid())) {
                    Log.d(getLogTag(), "移除可见频道：" + ncb.getCnname());
                    itera.remove();
                    break;
                }
            }
        }

        if (myAllList != null) {
            myAllAdapter.setList(myAllList);
            Log.i(getLogTag(), "Hidden channel count : " + myAllList.size());

            editcolumn_gridview.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent,
                                        final View view, final int position, long id) {
                    final NewsChannelBean ncb = (NewsChannelBean) myAllAdapter.getItem(position);
                    modified = true;
                    channelData.add(ncb);
                    dragAdapter.setList(channelData);
                    myAllList.remove(position);
                    myAllAdapter.setList(myAllList);
                }
            });
        }
    }

    private void writeData() {
        List<NewsChannelBean> saveTitleList = new ArrayList<NewsChannelBean>();
        for (int i = 0; i < channelData.size(); i++) {
            NewsChannelBean sb = new NewsChannelBean();
            sb.setCnname(channelData.get(i).getCnname());
            sb.setStyle(channelData.get(i).getStyle());
            sb.setTid(channelData.get(i).getTid());
            sb.setType(channelData.get(i).getType());
            saveTitleList.add(sb);
        }
        mSaveTitleData.writeDataToFile(saveTitleList, getChannelInfoCacheSavePath());
        csl.setSaveTitleList(saveTitleList);
    }

    @OnClick(R.id.stitle_ll_back)
    private void goback(View v) {
        finish();
    }

    @Override
    protected void onDestroy() {
        if (!modified) {
            csl = null;
        }
        if (isEdit) {
            selPosition = -1;
        }
        if (modified) {
            writeData();
            modified = false;
        }
        EventBus.getDefault().post(new ChangeChannelEvent(csl, selPosition));
        super.onDestroy();
    }

    private void addLocalChannels(List<NewsChannelBean> list) {

//        //	专题
//        NewsChannelBean channelSubject = new NewsChannelBean();
//        channelSubject.setTid("" + NewsChannelBean.TYPE_SUBJECT);
//        channelSubject.setType(NewsChannelBean.TYPE_SUBJECT);
//        channelSubject.setCnname(getString(R.string.menu_subject));
//        if (!list.contains(channelSubject)) {
//            list.add(0, channelSubject);
//            Log.d(getLogTag(), "add channelSubject");
//        }
//        // 视频
//        NewsChannelBean channelVideo = new NewsChannelBean();
//        channelVideo.setTid("" + NewsChannelBean.TYPE_VIDEO);
//        channelVideo.setType(NewsChannelBean.TYPE_VIDEO);
//        channelVideo.setCnname(getString(R.string.menu_video));
//        if (!list.contains(channelVideo)) {
//            list.add(0, channelVideo);
//            Log.d(getLogTag(), "add channelVideo");
//        }
//        //	图集
//        NewsChannelBean channelImageAlbum = new NewsChannelBean();
//        channelImageAlbum.setTid("" + NewsChannelBean.TYPE_IMAGE_ALBUM);
//        channelImageAlbum.setType(NewsChannelBean.TYPE_IMAGE_ALBUM);
//        channelImageAlbum.setCnname(getString(R.string.menu_album));
//        if (!list.contains(channelImageAlbum)) {
//            list.add(0, channelImageAlbum);
//            Log.d(getLogTag(), "add channelImageAlbum");
//        }


        // 添加推荐频道
        NewsChannelBean channelRecommend = new NewsChannelBean();
        channelRecommend.setTid("" + NewsChannelBean.TYPE_RECOMMEND);
        channelRecommend.setType(NewsChannelBean.TYPE_RECOMMEND);
        channelRecommend.setCnname(getString(R.string.recommend));
        if (!list.contains(channelRecommend)) {
            list.add(0, channelRecommend);
        }

    }
}
