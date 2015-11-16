package com.hzpd.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.custorm.DonutProgress;
import com.hzpd.custorm.ImageViewPager;
import com.hzpd.custorm.RecyclingPagerAdapter;
import com.hzpd.hflt.R;
import com.hzpd.hflt.wxapi.FacebookSharedUtil;
import com.hzpd.modle.CommentsCountBean;
import com.hzpd.modle.ImageListSubBean;
import com.hzpd.modle.ImgListBean;
import com.hzpd.modle.Jsonbean;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.NewsDetailBean;
import com.hzpd.modle.NewsDetailImgList;
import com.hzpd.modle.NewsItemBeanForCollection;
import com.hzpd.modle.NewsJumpBean;
import com.hzpd.modle.ReplayBean;
import com.hzpd.ui.App;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.Constant;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.EventUtils;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.MyCommonUtil;
import com.hzpd.utils.RequestParamsUtils;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
import com.hzpd.utils.TUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.IPhotoView;
import uk.co.senab.photoview.PhotoView;

/**
 * 图集展示*
 */
public class NewsAlbumActivity extends MBaseActivity implements OnClickListener {
    @Override
    public String getAnalyticPageName() {
        return "图集展示页";
    }

    @ViewInject(R.id.img_detail_test_pager)
    private ImageViewPager mViewPager;
    @ViewInject(R.id.img_detail_title_id)
    private TextView mTextViewTitle;
    @ViewInject(R.id.img_detail_content_id)
    private TextView mTextViewContent;
    @ViewInject(R.id.img_detial_number_id)
    private TextView mTextViewNumber;
    @ViewInject(R.id.main_title_personal)
    private View mLayoutBack;

    @ViewInject(R.id.imgdetails_title_pl)
    private LinearLayout imgdetails_title_pl;
    @ViewInject(R.id.imgdetails_title_comment)
    private LinearLayout imgdetails_title_comment;
    @ViewInject(R.id.imgdetails_title_num)
    private TextView imgdetails_title_num;

    @ViewInject(R.id.album_donutProgress)
    private DonutProgress donutProgress;

    private NewsAlbumAdapter simpleAdapter;

    private String from;//news album newsitem
    private int currentPosition = 0;

    @ViewInject(R.id.album_rl_head)
    private RelativeLayout album_rl_head;//头部
    @ViewInject(R.id.album_rl_bottom)//底部
    private RelativeLayout album_rl_bottom;

    @ViewInject(R.id.pop_xiazai_iv1)
    private ImageView pop_xiazai_iv1;

    private boolean animationFlag = false;//是否隐藏

    private NewsDetailBean ndb;
    private ImgListBean imgListBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_detail_img_main_layout);
        ViewUtils.inject(this);

        pop_xiazai_iv1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageListSubBean bean = simpleAdapter.getCurrentBean(currentPosition);
                final String sdPath = getSDPath();
                if (null == sdPath) {
                    TUtils.toast(getString(R.string.toast_no_sdcard));
                    return;
                }


                String imgUrl = bean.getSubphoto();
                if ((null == imgUrl || "".equals(imgUrl))
                        && !imgUrl.endsWith(".jpg") && !imgUrl.endsWith(".JPG")
                        && !imgUrl.endsWith(".png") && !imgUrl.endsWith(".PNG")
                        && !imgUrl.endsWith(".gif") && !imgUrl.endsWith(".GIF")) {
                    TUtils.toast(getString(R.string.toast_image_format_is_wrong));
                    return;
                }
                File img = App.getFile(sdPath + File.separator + "adownload_joymeng" + File.separator + System.currentTimeMillis() + ".jpeg");
                final String imagePath=sdPath + File.separator + "adownload_joymeng"+ File.separator;
                httpUtils.download(
                        imgUrl
                        , img.getAbsolutePath()
                        , true, true
                        , new RequestCallBack<File>() {
                            @Override
                            public void onStart() {
                                super.onStart();
//						donutProgress.setVisibility(View.VISIBLE);
//						donutProgress.setProgress(0);
                            }

                            @Override
                            public void onLoading(long total, long current,
                                                  boolean isUploading) {
                                super.onLoading(total, current, isUploading);
                                LogUtils.i("current:" + current + " total:" + total + "   " + (current * 100 / total));
//						donutProgress.setProgress((int)(current*100/total));
                            }

                            @Override
                            public void onSuccess(ResponseInfo<File> responseInfo) {
                                TUtils.toast(getString(R.string.toast_downloaded_at, responseInfo.result.getAbsolutePath()));
                                MediaScannerConnection.scanFile(NewsAlbumActivity.this,new String[]{imagePath},null,null);
//						alpha();
                            }

                            @Override
                            public void onFailure(HttpException error, String msg) {
                                TUtils.toast(getString(R.string.toast_download_failed));
//						alpha();
                            }
                        });
            }
        });

        init();

    }

    private void init() {

        String pid = null;
        Intent intent = getIntent();
        String action = intent.getAction();
        if (null != action && Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            if (uri != null) {
                pid = uri.getPath();
                pid = pid.replace(File.separator, "");
                from = "browser";
                getAlbum_browser(pid);
            }
        } else {
            from = intent.getStringExtra("from");
        }

        initViewPage();

        if ("news".equals(from)) {
            currentPosition = intent.getIntExtra("position", 0);
            ndb = (NewsDetailBean) intent.getSerializableExtra("ndb");

            List<ImageListSubBean> mlist = new ArrayList<ImageListSubBean>();
            if (null != ndb.getPic()) {
                for (NewsDetailImgList im : ndb.getPic()) {
                    ImageListSubBean ilsb = new ImageListSubBean(im.getSubdesc(), im.getSubphoto());
                    mlist.add(ilsb);
                }
                simpleAdapter.setList(mlist);
            }
            mTextViewTitle.setText(ndb.getTitle());
            if (simpleAdapter.getCount() > currentPosition) {
                mViewPager.setCurrentItem(currentPosition);
            }
        } else if ("album".equals(from)) {
            currentPosition = 0;
            imgListBean = (ImgListBean) intent.getSerializableExtra("imgbean");
            if (null != imgListBean) {
                simpleAdapter.setList(imgListBean.getSubphoto());
                setVisible();
            }

        } else if ("collection".equals(from)) {
            getCollectionPhotoList(intent.getStringExtra("json_url")
                    , intent.getStringExtra("pid"));
        } else if ("newsitem".equals(from)) {
            //获取图集
            LogUtils.i("newbean");
            NewsBean nb = (NewsBean) intent.getSerializableExtra("newbean");
            getAlbum_ni(nb.getJson_url());

        } else if ("browser".equals(from)) {
            LogUtils.i("browser");
            getAlbum_browser(pid);
        } else {
            return;
        }

        mLayoutBack.setOnClickListener(this);

        imgdetails_title_pl.setOnClickListener(this);
        imgdetails_title_comment.setOnClickListener(this);


    }

    private void setVisible() {
        if (simpleAdapter.getListSize() > 0) {
            imgdetails_title_pl.setVisibility(View.VISIBLE);
//			imgdetails_title_comment.setVisibility(View.VISIBLE);
            getCommentsCounts();//评论数量
        }

    }

    private void initViewPage() {
        simpleAdapter = new NewsAlbumAdapter();

        mViewPager.setPageMargin(10);

        mViewPager.setAdapter(simpleAdapter);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) {

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageSelected(int arg0) {
                mTextViewNumber.setText((arg0 + 1) + "/" + simpleAdapter.getCount());
                mTextViewContent.setText(simpleAdapter.getDes(arg0));
                currentPosition = arg0;
            }
        });
        mViewPager.setCurrentItem(currentPosition);

    }

    class NewsAlbumAdapter extends RecyclingPagerAdapter {
        private List<ImageListSubBean> list;
        private LayoutInflater inflater;

        public NewsAlbumAdapter() {
            list = new ArrayList<ImageListSubBean>();
            inflater = LayoutInflater.from(NewsAlbumActivity.this);

        }

        public ImageListSubBean getCurrentBean(int position) {
            return list.get(position % list.size());
        }

        public void setList(List<ImageListSubBean> mList) {
            if (mList.size() > 0) {
                clearList();
                addList(mList);
            }
        }

        public int getListSize() {
            return list.size();
        }

        public void addList(List<ImageListSubBean> mList) {
            list.addAll(mList);
            mTextViewNumber.setText("1/" + list.size());
            if ("news".equals(from)) {
                mTextViewTitle.setText(ndb.getTitle());
            } else {
                mTextViewTitle.setText(imgListBean.getTitle());
            }

            if (list.size() > 0) {
                mTextViewContent.setText(list.get(0).getSubdesc());
            }
            notifyDataSetChanged();
        }

        public void clearList() {
            list.clear();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        public String getDes(int positon) {
            return list.get(positon).getSubdesc();
        }


        @Override
        public View getView(int position, View convertView, ViewGroup container) {

            final PhotoView photoView = new PhotoView(activity);
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT
                    , LayoutParams.MATCH_PARENT);
            photoView.setLayoutParams(params);

            SPUtil.displayImage(list.get(position).getSubphoto()
                    , photoView, DisplayOptionFactory.getOption(OptionTp.Big)
                    , new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
//							donutProgress.setVisibility(View.VISIBLE);
//							donutProgress.setProgress(0);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view,
                                            FailReason failReason) {
//							alpha();
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//							alpha();
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
//							alpha();
                }
            }
                    , new ImageLoadingProgressListener() {
                @Override
                public void onProgressUpdate(String imageUri, View view, int current,
                                             int total) {
                    donutProgress.setProgress(current / total);
                }
            });

            photoView.setOnDoubleTapListener(new OnDoubleTapListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    LogUtils.i("photoView click");
//					album_rl_head
//					album_rl_bottom
//					news_detail_ll_bottom2
                    if (animationFlag) {
                        ObjectAnimator
                                .ofFloat(album_rl_head, "translationY"
                                        , -album_rl_head.getHeight(), 0f)
                                .setDuration(350)
                                .start();

                        ObjectAnimator
                                .ofFloat(album_rl_bottom, "translationY"
                                        , album_rl_bottom.getHeight(), 0f)
                                .setDuration(350)
                                .start();

                        animationFlag = false;
                    } else {
                        ObjectAnimator
                                .ofFloat(album_rl_head, "translationY"
                                        , 0f, -album_rl_head.getHeight())
                                .setDuration(350)
                                .start();

                        ObjectAnimator
                                .ofFloat(album_rl_bottom, "translationY"
                                        , 0f, album_rl_bottom.getHeight())
                                .setDuration(350)
                                .start();

                        animationFlag = true;
                    }
                    return true;
                }

                @Override
                public boolean onDoubleTapEvent(MotionEvent e) {
                    return false;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    float scale = photoView.getScale();
                    scale = Math.round(scale * 100) / 100f;
                    LogUtils.i("photoView double " + scale);

                    if (scale <= IPhotoView.DEFAULT_MIN_SCALE) {
                        photoView.setScale(IPhotoView.DEFAULT_MID_SCALE, true);
                    } else if (scale <= IPhotoView.DEFAULT_MID_SCALE) {
                        photoView.setScale(IPhotoView.DEFAULT_MAX_SCALE, true);
                    } else if (scale <= IPhotoView.DEFAULT_MAX_SCALE) {
                        photoView.setScale(IPhotoView.DEFAULT_MIN_SCALE, true);
                    } else {
                        return true;
                    }

                    return false;
                }
            });

            return photoView;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_title_personal: {
                finish();
            }
            break;
            case R.id.imgdetails_title_comment: {
                //跳转到评论
                //跳转到评论页
                if (null == imgListBean) {
                    return;
                }
                if (!MyCommonUtil.isNetworkConnected(this)) {
                    TUtils.toast(getString(R.string.toast_check_network));
                    return;
                }

            }
            break;
            case R.id.imgdetails_title_pl: {
                //分享 收藏  写评论
                popUpwindow();
            }
            break;
        }
    }

    private void popUpwindow() {
        final PopupWindow pinlunpop = new PopupWindow(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View popRoot = inflater.inflate(R.layout.newsdetail_popupwindow_layout, null);
        pinlunpop.setWindowLayoutMode(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        pinlunpop.setContentView(popRoot);
        ColorDrawable dw = new ColorDrawable(Color.TRANSPARENT);
        pinlunpop.setBackgroundDrawable(dw);
        pinlunpop.setOutsideTouchable(true);
        ImageView pop_fenxiang_img = (ImageView) popRoot.findViewById(R.id.pop_fenxiang_img);
        ImageView pop_shoucang_img = (ImageView) popRoot.findViewById(R.id.pop_shoucang_img);
        ImageView pop_ziti_img = (ImageView) popRoot.findViewById(R.id.pop_ziti_img);
        ImageView pop_xiazai_iv = (ImageView) popRoot.findViewById(R.id.pop_xiazai_iv);


        //写评论
        pop_ziti_img.setImageResource(R.drawable.bt_comment_unselected);
        if (null != ccBean) {
            if (!"0".equals(ccBean.getComflag())) {
//				pop_ziti_img.setVisibility(View.GONE);
            }
        }
        pop_xiazai_iv.setVisibility(View.VISIBLE);

//		分享
        pop_fenxiang_img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pinlunpop.dismiss();
                String[] array = new String[imgListBean.getSubphoto().size()];
                for (int i = 0; i < imgListBean.getSubphoto().size(); i++) {
                    ImageListSubBean b = imgListBean.getSubphoto().get(i);
                    array[i] = b.getSubphoto();
                }
                String imgPath = null;
                if (imgListBean.getSubphoto().size() > 0) {
                    imgPath = imgListBean.getSubphoto().get(0).getSubphoto();
                }
                //http://58.68.134.165:8081/index/Album/view/id/pid
                FacebookSharedUtil.showImgShares(imgListBean.getTitle(), imgPath, imgListBean.getPid(), NewsAlbumActivity.this);
            }
        });
        pop_shoucang_img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pinlunpop.dismiss();
                addCollection();
            }
        });
        //
        pop_ziti_img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pinlunpop.dismiss();
                if (null == imgListBean) {
                    return;
                }

//				if(!"0".equals(ccBean.getComflag())){
                String smallimg = imgListBean.getSubphoto().get(0).getSubphoto();
                ReplayBean bean = new ReplayBean(imgListBean.getPid()
                        , imgListBean.getTitle(), "Album", imgListBean.getJson_url()
                        , smallimg,imgListBean.getComcount());

//				TUtils.toast(imgListBean.getPid() + "" + imgListBean.getTitle() + "" + "Album" + "" + imgListBean.getJson_url() + "" + smallimg);
                Intent intent = new Intent(NewsAlbumActivity.this, ZQ_ReplyActivity.class);
                intent.putExtra("replay", bean);
                startActivity(intent);
                AAnim.bottom2top(NewsAlbumActivity.this);
//				}else{
//					TUtils.toast("不可评论");
//				}

            }
        });
        pop_xiazai_iv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pinlunpop.dismiss();
                ImageListSubBean bean = simpleAdapter.getCurrentBean(currentPosition);
                String sdPath = getSDPath();
                if (null == sdPath) {
                    TUtils.toast(getString(R.string.toast_no_sdcard));
                    return;
                }


                String imgUrl = bean.getSubphoto();
                if ((null == imgUrl || "".equals(imgUrl))
                        && !imgUrl.endsWith(".jpg") && !imgUrl.endsWith(".JPG")
                        && !imgUrl.endsWith(".png") && !imgUrl.endsWith(".PNG")
                        && !imgUrl.endsWith(".gif") && !imgUrl.endsWith(".GIF")) {
                    TUtils.toast(getString(R.string.toast_image_format_is_wrong));
                    return;
                }
                File img = App.getFile(sdPath + File.separator + "download_joymeng" + File.separator + System.currentTimeMillis() + ".jpg");

                httpUtils.download(
                        imgUrl
                        , img.getAbsolutePath()
                        , true, true
                        , new RequestCallBack<File>() {
                            @Override
                            public void onStart() {
                                super.onStart();
//						donutProgress.setVisibility(View.VISIBLE);
//						donutProgress.setProgress(0);
                            }

                            @Override
                            public void onLoading(long total, long current,
                                                  boolean isUploading) {
                                super.onLoading(total, current, isUploading);
                                LogUtils.i("current:" + current + " total:" + total + "   " + (current * 100 / total));
//						donutProgress.setProgress((int)(current*100/total));
                            }

                            @Override
                            public void onSuccess(ResponseInfo<File> responseInfo) {
                                TUtils.toast(getString(R.string.toast_downloaded_at, responseInfo.result.getAbsolutePath()));
//						alpha();
                            }

                            @Override
                            public void onFailure(HttpException error, String msg) {
                                TUtils.toast(getString(R.string.toast_download_failed));
//						alpha();
                            }
                        });
            }
        });

        pinlunpop.showAsDropDown(imgdetails_title_pl, -60, -15);

    }

    //添加收藏------------------
    private void addCollection() {
        if (null == spu.getUser()) {
            String data = JSONObject.toJSONString(imgListBean);
            LogUtils.i("data-->" + data);
            Jsonbean tcb = new Jsonbean(imgListBean.getPid(), data);
            NewsItemBeanForCollection nibfc = new NewsItemBeanForCollection(imgListBean);

            try {
                NewsItemBeanForCollection nitb = dbHelper.getCollectionDBUitls().findFirst(Selector
                        .from(NewsItemBeanForCollection.class)
                        .where("nid", "=", imgListBean.getPid()));
                if (null == nitb) {
                    dbHelper.getCollectionDBUitls().save(nibfc);
                    dbHelper.getCollectionDBUitls().save(tcb);

                    TUtils.toast(getString(R.string.toast_collect_success));
                } else {
                    dbHelper.getCollectionDBUitls().delete(NewsItemBeanForCollection.class
                            , WhereBuilder.b("nid", "=", imgListBean.getPid()));
                    dbHelper.getCollectionDBUitls().delete(Jsonbean.class, WhereBuilder.b("fid", "=", imgListBean.getPid()));

                    TUtils.toast(getString(R.string.toast_collect_cancelled));
                }

            } catch (DbException e1) {
                e1.printStackTrace();
                TUtils.toast(getString(R.string.toast_collect_failed));
            }
            return;
        }
        String station = SharePreferecesUtils.getParam(NewsAlbumActivity.this, StationConfig.STATION, "def").toString();
        String siteid = null;
        String ADDCOLLECTION_url = null;
        if (station.equals(StationConfig.DEF)) {
            siteid = InterfaceJsonfile.SITEID;
            ADDCOLLECTION_url = InterfaceJsonfile.ADDCOLLECTION;
        } else if (station.equals(StationConfig.YN)) {
            siteid = InterfaceJsonfile_YN.SITEID;
            ADDCOLLECTION_url = InterfaceJsonfile_YN.ADDCOLLECTION;
        } else if (station.equals(StationConfig.TW)) {
            siteid = InterfaceJsonfile_TW.SITEID;
            ADDCOLLECTION_url = InterfaceJsonfile_TW.ADDCOLLECTION;
        }
        LogUtils.i("Type-->" + "  Fid-->" + imgListBean.getPid());
        RequestParams params = RequestParamsUtils.getParamsWithU();
        params.addBodyParameter("type", "2");
        params.addBodyParameter("typeid", imgListBean.getPid());
        params.addBodyParameter("siteid", siteid);
        params.addBodyParameter("data", imgListBean.getJson_url());

        httpUtils.send(HttpMethod.POST
                , ADDCOLLECTION_url//InterfaceApi.addcollection
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                LogUtils.i("result-->" + responseInfo.result);
                JSONObject obj = FjsonUtil.parseObject(responseInfo.result);
                if (null == obj) {
                    return;
                }
                if (200 == obj.getIntValue("code")) {
                    JSONObject object = obj.getJSONObject("data");
                    //1:收藏操作成功 2:取消收藏操作成功
                    if ("1".equals(object.getString("status"))) {
//								newdetail_collection.setImageResource(R.drawable.zqzx_nd_collection_selected);
                    } else {
//								newdetail_collection.setImageResource(R.drawable.zqzx_nd_collection);
                    }
                }

                TUtils.toast(obj.getString("msg"));
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                TUtils.toast(getString(R.string.toast_cannot_connect_to_server));
            }
        });

    }

    //获取收藏
    private void getCollectionPhotoList(String json_url, final String albumitemid) {

        LogUtils.i("json_url-->" + json_url + "  albumitemid-->" + albumitemid);
        if (TextUtils.isEmpty(json_url)) {
            imgdetails_title_pl.setVisibility(View.GONE);
            imgdetails_title_comment.setVisibility(View.GONE);
        }

        File file = App.getFile(App.getInstance().getJsonFileCacheRootDir()
                + File.separator + "temp" + File.separator + "collection" + File.separator + albumitemid);
//		
        Jsonbean tcl = null;
        try {
            tcl = dbHelper.getCollectionDBUitls().findFirst(
                    Selector
                            .from(Jsonbean.class)
                            .where("fid", "=", albumitemid));
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        if (null != tcl) {
            LogUtils.i("data-->" + tcl.getData());

            imgListBean = FjsonUtil.parseObject(tcl.getData(), ImgListBean.class);
            if (null == imgListBean) {
                LogUtils.i("imgListBeann not null");
                return;
            }
            currentPosition = 0;
            simpleAdapter.setList(imgListBean.getSubphoto());
            mTextViewTitle.setText(imgListBean.getTitle());
            setVisible();
            return;
        }

        httpUtils.download(
                json_url
                , file.getAbsolutePath()
                , new RequestCallBack<File>() {
                    @Override
                    public void onSuccess(ResponseInfo<File> responseInfo) {
                        String data = App.getFileContext(responseInfo.result);

                        LogUtils.i("read data-->" + data);
                        JSONObject obj = FjsonUtil.parseObject(data);

                        if (null == obj) {
                            responseInfo.result.delete();
                            return;
                        }
                        imgListBean = FjsonUtil.parseObject(obj.getString("data"), ImgListBean.class);
                        if (null == imgListBean) {
                            responseInfo.result.delete();
                            return;
                        }
                        currentPosition = 0;
                        simpleAdapter.setList(imgListBean.getSubphoto());
                        mTextViewTitle.setText(imgListBean.getTitle());

                        setVisible();
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        TUtils.toast(getString(R.string.toast_server_no_response));
                    }
                });

    }

    //获取单个图集
    private void getAlbum_ni(final String json_url) {
        LogUtils.i("json_url-->" + json_url);
        NewsJumpBean albumdbbean = null;
        try {
            albumdbbean = dbHelper.getAlbumDBUitls().findFirst(Selector.from(NewsJumpBean.class)
                    .where("url", "=", json_url));
        } catch (Exception e) {
            e.printStackTrace();
            albumdbbean = null;
        }
        if (null != albumdbbean) {
            LogUtils.i("albumdbbeanid-->" + albumdbbean.getId()
                    + "\nurl-->" + albumdbbean.getUrl()
                    + "data-->" + albumdbbean.getContent());
            imgListBean = JSONObject.parseObject(albumdbbean.getContent(), ImgListBean.class);
            currentPosition = 0;
            LogUtils.i("-->" + imgListBean.getSubphoto().size());
            if (null == imgListBean) {
                LogUtils.i("imgListBean null");
            }
            simpleAdapter.setList(imgListBean.getSubphoto());
            setVisible();
        } else {
            httpUtils.download(
                    json_url
                    , App.getInstance().getJsonFileCacheRootDir()
                            + File.separator + "temp" + File.separator + "album"
                    , new RequestCallBack<File>() {
                        @Override
                        public void onSuccess(ResponseInfo<File> responseInfo) {
                            String content = App.getFileContext(responseInfo.result);
                            LogUtils.i("result-->" + content);
                            JSONObject obj = JSONObject.parseObject(content);

                            imgListBean = JSONObject.parseObject(
                                    obj.getJSONObject("data").toJSONString()
                                    , ImgListBean.class);

                            NewsJumpBean albumdbbean = new NewsJumpBean(
                                    json_url
                                    , obj.getJSONObject("data").toJSONString());
                            try {
                                dbHelper.getAlbumDBUitls().save(albumdbbean);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            currentPosition = 0;
                            simpleAdapter.setList(imgListBean.getSubphoto());
                            setVisible();
                        }

                        @Override
                        public void onFailure(HttpException error, String msg) {

                        }
                    });
        }

    }

    //获取单个图集来自浏览器跳转
    private void getAlbum_browser(final String pid) {
        Jsonbean tcb = null;
        try {
            tcb = dbHelper.getAlbumDBUitls().findFirst(Selector
                    .from(Jsonbean.class)
                    .where("fid", "=", pid));
        } catch (DbException e) {
            e.printStackTrace();
        }

        if (null != tcb) {
            imgListBean = JSONObject.parseObject(tcb.getData(), ImgListBean.class);

            simpleAdapter.setList(imgListBean.getSubphoto());
            setVisible();
            return;
        }
        String station = SharePreferecesUtils.getParam(NewsAlbumActivity.this, StationConfig.STATION, "def").toString();
        String siteid = null;
        String bAlbum_url = null;
        if (station.equals(StationConfig.DEF)) {
            siteid = InterfaceJsonfile.SITEID;
            bAlbum_url = InterfaceJsonfile.bAlbum;
        } else if (station.equals(StationConfig.YN)) {
            siteid = InterfaceJsonfile_YN.SITEID;
            bAlbum_url = InterfaceJsonfile_YN.bAlbum;
        } else if (station.equals(StationConfig.TW)) {
            siteid = InterfaceJsonfile_TW.SITEID;
            bAlbum_url = InterfaceJsonfile_TW.bAlbum;
        }
        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("siteid", siteid);
        params.addBodyParameter("id", pid);

        httpUtils.send(
                HttpMethod.POST
                , bAlbum_url
                , params
                , new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        LogUtils.i("result-->" + responseInfo.result);
                        JSONObject obj = null;
                        try {
                            obj = JSONObject.parseObject(responseInfo.result);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }

                        if (200 == obj.getIntValue("code")) {

                            imgListBean = JSONObject.parseObject(
                                    obj.getString("data")
                                    , ImgListBean.class);

                            Jsonbean tcb = new Jsonbean(pid, obj.getString("data"));
                            try {
                                dbHelper.getAlbumDBUitls().save(tcb);
                            } catch (DbException e) {
                                e.printStackTrace();
                            }

                            simpleAdapter.setList(imgListBean.getSubphoto());
                            setVisible();
                        } else {
                            TUtils.toast(obj.getString("msg"));
                        }

                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        TUtils.toast(getString(R.string.toast_server_no_response));
                    }
                });

    }

    @Override
    protected void onPause() {
//		if(!"news".equals(from)&&null!=App.mNewsDetailBean){
//			setAnaly(App.mNewsDetailBean.getNid()
//					, App.mNewsDetailBean.getTitle()
//					, "album");
//		}
        super.onPause();
    }


    @Override
    public void finish() {
        if (!App.isStartApp && "browser".equals(from)) {
            Intent in = new Intent();
            in.setClass(this, WelcomeActivity.class);
            startActivity(in);
        }
        super.finish();
    }

    public String getSDPath() {
        String path = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            File sdDir = Environment.getExternalStorageDirectory();
            path = sdDir.getAbsolutePath();
        }
        return path;
    }

    private void alpha() {
        AlphaAnimation alpha = new AlphaAnimation(1f, 0f);
        alpha.setDuration(150);
        alpha.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                donutProgress.setVisibility(View.GONE);
            }
        });
        donutProgress.startAnimation(alpha);
    }

    private CommentsCountBean ccBean;//评论数量，是否可评论

    private void getCommentsCounts() {
        if (null == imgListBean) {
            return;
        }

        EventUtils.sendReadAtical(activity);
        String station = SharePreferecesUtils.getParam(NewsAlbumActivity.this, StationConfig.STATION, "def").toString();
        String commentsConts_url = null;
        if (station.equals(StationConfig.DEF)) {
            commentsConts_url = InterfaceJsonfile.commentsConts;
        } else if (station.equals(StationConfig.YN)) {
            commentsConts_url = InterfaceJsonfile_YN.commentsConts;
        } else if (station.equals(StationConfig.TW)) {
            commentsConts_url = InterfaceJsonfile_TW.commentsConts;
        }
        RequestParams params = RequestParamsUtils.getParams();
        params.addBodyParameter("type", Constant.TYPE.AlbumA.toString());
        params.addBodyParameter("nids", imgListBean.getPid());

        httpUtils.send(HttpMethod.POST
                , commentsConts_url
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                LogUtils.i("loginSubmit-->" + responseInfo.result);

                JSONObject obj = FjsonUtil
                        .parseObject(responseInfo.result);
                if (null == obj) {
                    return;
                }

                if (200 == obj.getIntValue("code")) {

                    List<CommentsCountBean> li = JSONObject.parseArray(obj.getString("data")
                            , CommentsCountBean.class);
                    if (null == li) {
                        return;
                    }
                    for (CommentsCountBean cc : li) {
                        if (imgListBean.getPid().equals(cc.getNid())) {
                            ccBean = cc;
                            String snum = cc.getC_num();
                            if (TextUtils.isDigitsOnly(snum)) {
                                int count = Integer.parseInt(snum);
//										TUtils.toast(count+"");
                                if (count > 0) {
                                    imgdetails_title_comment.setVisibility(View.VISIBLE);
                                    imgdetails_title_num.setText(count + "");//设置评论数量
                                }
                            }
                        }
                    }

                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {

            }
        });
    }

}