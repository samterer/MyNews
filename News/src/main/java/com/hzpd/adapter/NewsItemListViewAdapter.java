package com.hzpd.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.hzpd.custorm.TopPicViewPager;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.NewsPageListBean;
import com.hzpd.modle.db.NewsBeanDB;
import com.hzpd.ui.App;
import com.hzpd.ui.interfaces.I_Result;
import com.hzpd.ui.widget.CircleIndicator;
import com.hzpd.utils.CalendarUtil;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.Log;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.db.NewsListDbTask;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class NewsItemListViewAdapter extends RecyclerView.Adapter {

    public static final String AD_KEY = "1902056863352757_1942167249341718"; // "1902056863352757_1922349784656798";
    public static final int STEP = 10;
    public static final int MAX_POSITION = 80;

    public interface CallBack {
        void loadMore();
    }

    final Random random = new Random();
    public CallBack callBack;
    Context context;
    LayoutInflater inflater;
    List<NewsBean> list = null;
    List<NewsPageListBean> viewPagelist = new ArrayList<>();
    ImageLoader mImageLoader;
    DBHelper dbHelper;
    List<NewsBean> appendoldlist = null;

    final static int TYPE_FLASH = 9;
    final static int TYPE_THREEPIC = 0;
    final static int TYPE_LEFTPIC = 1;
    final static int TYPE_BIGPIC = 2;
    final static int TYPE_LARGE = 3;
    final static int TYPE_AD = 0xad;
    final static int TYPE_LOADING = 0xDD;
    public boolean showLoading = false;
    private SPUtil spu;
    private int fontSize = 0;//字体大小

    private HashSet<String> readedNewsSet;
    private NewsListDbTask newsListDbTask;

    private View.OnClickListener onClickListener;
    TopviewpagerAdapter topviewAdapter;
    private HashMap<String, NativeAd> ads;
    int nextAdPosition = STEP;

    public void setAds(HashMap<String, NativeAd> ads) {
        this.ads = ads;
    }

    public void setNativeAd(final NativeAd nativeAd) {
        Log.e("test", "nextAdPosition->" + nextAdPosition + ": " + nativeAd);
        ads.put("" + nextAdPosition, nativeAd);
        final int adPos = nextAdPosition;
        nativeAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.e("test", "onError->" + nextAdPosition + ": " + adError);
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.e("test", "onAdLoaded->" + nextAdPosition + ": " + nativeAd);
                try {
                    if (!isAdd) {
                        return;
                    }
                    NewsBean newsBeanAD = new NewsBean();
                    newsBeanAD.setType("ad");
                    String titleForAd = nativeAd.getAdTitle();
                    NativeAd.Image coverImage = nativeAd.getAdCoverImage();
                    NativeAd.Image iconForAd = nativeAd.getAdIcon();
                    String socialContextForAd = nativeAd.getAdSocialContext();
                    String titleForAdButton = nativeAd.getAdCallToAction();
                    String textForAdBody = nativeAd.getAdBody();
                    NativeAd.Rating appRatingForAd = nativeAd.getAdStarRating();

                    newsBeanAD.setTitle(textForAdBody);
                    newsBeanAD.setCopyfrom(titleForAd);
                    String[] images = new String[1];
                    images[0] = coverImage.getUrl();
                    newsBeanAD.setImgs(images);
                    int position = adPos;
                    if (list.size() < adPos) {
                        return;
                    }
                    list.add(adPos, newsBeanAD);
                    if (viewPagelist.size() > 0) {
                        ++position;
                    }
                    notifyItemInserted(position);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
            }
        });
        nativeAd.loadAd();
        nextAdPosition += STEP + random.nextInt(STEP);
        if (MAX_POSITION < nextAdPosition) {
            nextAdPosition = Integer.MAX_VALUE;
        }
    }


    public NewsItemListViewAdapter(Context context, View.OnClickListener onClickListener) {
        this.context = context;
        this.onClickListener = onClickListener;
        this.inflater = LayoutInflater.from(context);
        list = new ArrayList<NewsBean>();
        dbHelper = DBHelper.getInstance(context);
        spu = SPUtil.getInstance();
        readedNewsSet = new HashSet<String>();
        newsListDbTask = new NewsListDbTask(context);
        fontSize = spu.getTextSize();
        topviewAdapter = new TopviewpagerAdapter((Activity) context);
    }

    public void setFlashlist(List<NewsPageListBean> list) {
        if (list != null && list.size() > 0) {
            boolean flag = true;
            if (viewPagelist.size() > 0) {
                flag = false;
            }
            viewPagelist.clear();
            viewPagelist.addAll(list);
            if (flag) {
                notifyItemInserted(0);
            } else {
                notifyItemChanged(0);
            }
        }
    }

    public void appendData(List<NewsBean> data, boolean isClearOld, boolean isDb) {
        if (data == null) {
            return;
        }
        int index = getItemCount();
        if (showLoading) {
            index = index - 1;
        }
        if (isClearOld) {
            list.clear();
        }
        list.addAll(data);
        getReaded(data);
        if (isDb) {
            appendoldlist = data;
        }
        if (isClearOld) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(index, data.size());
        }

        while (list.size() > nextAdPosition) {
            NativeAd nativeAd = new NativeAd(context, AD_KEY);
            setNativeAd(nativeAd);
        }
    }

    public void removeOld() {
        if (null != appendoldlist && appendoldlist.size() > 0) {
            list.removeAll(appendoldlist);
            appendoldlist = null;
        }
    }


    public void clear() {
        list.clear();
        appendoldlist.clear();
    }

    public void setReadedId(String nid) {
        readedNewsSet.add(nid);

        try {
            NewsBeanDB nbdb = new NewsBeanDB();
            nbdb.setNid(Integer.parseInt(nid));
            nbdb.setIsreaded(1);
            dbHelper.getNewsListDbUtils().update(nbdb
                    , WhereBuilder.b("nid", "=", nid)
                    , "isreaded");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        isAdd = false;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        isAdd = true;
    }

    boolean isAdd = true;

    public void getReaded(List<NewsBean> list) {
        if (null == list) {
            return;
        }
        for (final NewsBean bean : list) {

            newsListDbTask.isRead(bean.getNid(), new I_Result() {
                @Override
                public void setResult(Boolean flag) {
                    try {
                        if (isAdd && flag) {
                            readedNewsSet.add(bean.getNid());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        View convertView;
        switch (viewType) {
            case TYPE_FLASH:
                convertView = inflater.inflate(
                        R.layout.news_item_flash_layout, parent, false);
                viewHolder = new FlashHolder(convertView);
                break;
            case TYPE_THREEPIC:
                convertView = inflater.inflate(
                        R.layout.news_3_item_layout, parent, false);
                viewHolder = new VHThree(convertView);
                break;
            case TYPE_LEFTPIC:
                convertView = inflater.inflate(
                        R.layout.news_list_item_layout, parent, false);
                viewHolder = new VHLeftPic(convertView);
                break;
            case TYPE_BIGPIC:
                convertView = inflater.inflate(
                        R.layout.news_big_item_layout, parent, false);
                viewHolder = new VHBigPic(convertView);
                break;
            case TYPE_LARGE:
                convertView = inflater.inflate(
                        R.layout.news_large_item_layout, parent, false);
                viewHolder = new VHLargePic(convertView);
                break;
            case TYPE_AD:
                convertView = inflater.inflate(
                        R.layout.news_list_ad_layout, parent, false);
                viewHolder = new AdHolder(convertView);
                break;
            case TYPE_LOADING:
                convertView = inflater.inflate(
                        R.layout.list_load_more_layout, parent, false);
                viewHolder = new LoadingHolder(convertView);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        try {
            NewsBean bean = null;
            int type = getItemViewType(position);
            if (viewPagelist.size() > 0 && position != 0) {
                --position;
            }
            switch (type) {
                case TYPE_LOADING:
                    if (showLoading && callBack != null) {
                        callBack.loadMore();
                    }
                    break;
                case TYPE_FLASH:
                    FlashHolder flashHolder = (FlashHolder) holder;
                    topviewAdapter.setTid(viewPagelist.get(0).getTid());
                    topviewAdapter.setData(viewPagelist);
                    flashHolder.mTextView.setText(viewPagelist.get(0).getTitle());
                    flashHolder.topViewpager.setCurrentItem(0, true);
                    flashHolder.indicator_default.setViewPager(flashHolder.topViewpager);
                    break;
                case TYPE_THREEPIC:
                    bean = list.get(position);
                    VHThree vhThree = (VHThree) holder;
                    holder.itemView.setTag(bean);
                    vhThree.newsitem_title.setTextSize(fontSize);
                    vhThree.newsitem_title.setText(bean.getTitle());
                    vhThree.img0.setImageResource(R.drawable.default_bg);
                    vhThree.img1.setImageResource(R.drawable.default_bg);
                    vhThree.img2.setImageResource(R.drawable.default_bg);
                    String from = bean.getCopyfrom();
                    if (!TextUtils.isEmpty(from)) {
                        vhThree.newsitem_source.setVisibility(View.VISIBLE);
                        vhThree.newsitem_source.setText(from);
                    } else {
                        vhThree.newsitem_source.setVisibility(View.GONE);
                    }

                    String fav = bean.getFav();
                    if (!TextUtils.isEmpty(fav)) {
                        int fav_counts = Integer.parseInt(fav);
                        if (fav_counts > 0) {
                            vhThree.newsitem_collectcount.setVisibility(View.VISIBLE);
                            vhThree.newsitem_collectcount.setText(fav_counts + "");
                        } else {
                            vhThree.newsitem_collectcount.setVisibility(View.GONE);
                        }
                    } else {
                        vhThree.newsitem_collectcount.setVisibility(View.GONE);
                    }
                    String comcount = bean.getComcount();
                    if (!TextUtils.isEmpty(comcount)) {
                        int counts = Integer.parseInt(comcount);
                        if (counts > 0) {
                            vhThree.newsitem_comments.setVisibility(View.VISIBLE);
                            bean.setComcount(counts + "");
                            vhThree.newsitem_comments.setText(counts + "");
                        } else {
                            vhThree.newsitem_comments.setVisibility(View.GONE);
                        }
                    } else {
                        vhThree.newsitem_comments.setVisibility(View.GONE);
                    }


                    if (!TextUtils.isEmpty(bean.getAttname())) {
                        String attname = bean.getAttname();
                        if (attname.equals("a")) {
                            vhThree.item_type_iv.setVisibility(View.VISIBLE);
                            vhThree.item_type_iv.setImageResource(R.drawable.zq_subscript_hot);
                        } else if (attname.equals("b")) {
                            vhThree.item_type_iv.setVisibility(View.VISIBLE);
                            vhThree.item_type_iv.setImageResource(R.drawable.zq_subscript_rekom);
                        } else if (attname.equals("c")) {
                            vhThree.item_type_iv.setVisibility(View.VISIBLE);
                            vhThree.item_type_iv.setImageResource(R.drawable.zq_subscript_kolom);
                        } else if (attname.equals("f")) {
                            vhThree.item_type_iv.setVisibility(View.VISIBLE);
                            vhThree.item_type_iv.setImageResource(R.drawable.zq_subscript_fokus);
                        } else if (attname.equals("h")) {
                            vhThree.item_type_iv.setVisibility(View.VISIBLE);
                            vhThree.item_type_iv.setImageResource(R.drawable.zq_subscript_xtend);
                        } else if (attname.equals("j")) {
                            vhThree.item_type_iv.setVisibility(View.VISIBLE);
                            vhThree.item_type_iv.setImageResource(R.drawable.zq_subscript_issue);
                        } else if (attname.equals("p")) {
                            vhThree.item_type_iv.setVisibility(View.VISIBLE);
                            vhThree.item_type_iv.setImageResource(R.drawable.zq_subscript_album);
                        } else if (attname.equals("s")) {
                            vhThree.item_type_iv.setVisibility(View.VISIBLE);
                            vhThree.item_type_iv.setImageResource(R.drawable.zq_subscript_video);
                        }
                    }
                    vhThree.tv3.setText(CalendarUtil.friendlyTime(bean.getUpdate_time(), context));

                    String s[] = bean.getImgs();
                    if (s.length == 1) {
                        SPUtil.displayImage(s[0], vhThree.img0, DisplayOptionFactory.getOption(OptionTp.Small));
                        SPUtil.displayImage("", vhThree.img1, DisplayOptionFactory.getOption(OptionTp.Small));
                        SPUtil.displayImage("", vhThree.img2, DisplayOptionFactory.getOption(OptionTp.Small));
                    } else if (s.length == 2) {
                        SPUtil.displayImage(s[0], vhThree.img0, DisplayOptionFactory.getOption(OptionTp.Small));
                        SPUtil.displayImage(s[1], vhThree.img1, DisplayOptionFactory.getOption(OptionTp.Small));
                        SPUtil.displayImage("", vhThree.img2, DisplayOptionFactory.getOption(OptionTp.Small));
                    } else if (s.length > 2) {
                        SPUtil.displayImage(s[0], vhThree.img0, DisplayOptionFactory.getOption(OptionTp.Small));
                        SPUtil.displayImage(s[1], vhThree.img1, DisplayOptionFactory.getOption(OptionTp.Small));
                        SPUtil.displayImage(s[2], vhThree.img2, DisplayOptionFactory.getOption(OptionTp.Small));
                    }

                    vhThree.newsitem_foot.setVisibility(View.GONE);
                    //1新闻  2图集  3直播 4专题  5关联新闻 6视频
                    if ("2".equals(bean.getRtype())) {
                        vhThree.newsitem_foot.setImageResource(R.drawable.zq_subscript_album);
                        vhThree.newsitem_foot.setVisibility(View.VISIBLE);
                    } else if ("3".equals(bean.getRtype())) {
                        vhThree.newsitem_foot.setImageResource(R.drawable.zq_subscript_live);
                    } else if ("4".equals(bean.getRtype())) {
                        vhThree.newsitem_foot.setImageResource(R.drawable.zq_subscript_issue);
                        vhThree.newsitem_foot.setVisibility(View.VISIBLE);
                    } else if ("6".equals(bean.getRtype())) {
                        vhThree.newsitem_foot.setImageResource(R.drawable.zq_subscript_video);
                        vhThree.newsitem_foot.setVisibility(View.VISIBLE);
                    } else if ("7".equals(bean.getRtype())) {
                        vhThree.newsitem_foot.setImageResource(R.drawable.zq_subscript_html);
                        vhThree.newsitem_foot.setVisibility(View.VISIBLE);
                    }

                    break;
                case TYPE_LEFTPIC:
                    bean = list.get(position);
                    holder.itemView.setTag(bean);
                    VHLeftPic vhLeftPic = (VHLeftPic) holder;
                    vhLeftPic.newsitem_title.setTextSize(fontSize);
                    vhLeftPic.newsitem_title.setText(bean.getTitle());
                    if (readedNewsSet.contains(bean.getNid())) {
                        vhLeftPic.newsitem_title.setTextColor(App.getInstance()
                                .getResources().getColor(R.color.grey_font));
                    }
                    if (!TextUtils.isEmpty(bean.getAttname())) {
                        String attname = bean.getAttname();
                        if (attname.equals("a")) {
                            vhLeftPic.item_type_iv.setVisibility(View.VISIBLE);
                            vhLeftPic.item_type_iv.setImageResource(R.drawable.zq_subscript_hot);
                        } else if (attname.equals("b")) {
                            vhLeftPic.item_type_iv.setVisibility(View.VISIBLE);
                            vhLeftPic.item_type_iv.setImageResource(R.drawable.zq_subscript_rekom);
                        } else if (attname.equals("c")) {
                            vhLeftPic.item_type_iv.setVisibility(View.VISIBLE);
                            vhLeftPic.item_type_iv.setImageResource(R.drawable.zq_subscript_kolom);
                        } else if (attname.equals("f")) {
                            vhLeftPic.item_type_iv.setVisibility(View.VISIBLE);
                            vhLeftPic.item_type_iv.setImageResource(R.drawable.zq_subscript_fokus);
                        } else if (attname.equals("h")) {
                            vhLeftPic.item_type_iv.setVisibility(View.VISIBLE);
                            vhLeftPic.item_type_iv.setImageResource(R.drawable.zq_subscript_xtend);
                        } else if (attname.equals("j")) {
                            vhLeftPic.item_type_iv.setVisibility(View.VISIBLE);
                            vhLeftPic.item_type_iv.setImageResource(R.drawable.zq_subscript_issue);
                        } else if (attname.equals("p")) {
                            vhLeftPic.item_type_iv.setVisibility(View.VISIBLE);
                            vhLeftPic.item_type_iv.setImageResource(R.drawable.zq_subscript_album);
                        } else if (attname.equals("s")) {
                            vhLeftPic.item_type_iv.setVisibility(View.VISIBLE);
                            vhLeftPic.item_type_iv.setImageResource(R.drawable.zq_subscript_video);
                        }
                    }

                    fav = bean.getFav();
                    if (!TextUtils.isEmpty(fav)) {

                        int fav_counts = Integer.parseInt(fav);
                        if (fav_counts > 0) {
                            vhLeftPic.newsitem_collectcount.setVisibility(View.VISIBLE);
                            vhLeftPic.newsitem_collectcount.setText(fav_counts + "");
                        } else {
                            vhLeftPic.newsitem_collectcount.setVisibility(View.GONE);
                        }
                    } else {
                        vhLeftPic.newsitem_collectcount.setVisibility(View.GONE);
                    }

                    from = bean.getCopyfrom();
                    if (!TextUtils.isEmpty(from)) {
                        vhLeftPic.newsitem_source.setVisibility(View.VISIBLE);
                        vhLeftPic.newsitem_source.setText(from);
                    } else {
                        vhLeftPic.newsitem_source.setVisibility(View.GONE);
                    }

                    comcount = bean.getComcount();
                    if (!TextUtils.isEmpty(comcount)) {
                        int counts = Integer.parseInt(comcount);
                        if (counts > 0) {
                            vhLeftPic.newsitem_commentcount.setVisibility(View.VISIBLE);
                            bean.setComcount(counts + "");
                            vhLeftPic.newsitem_commentcount.setText(counts + "");
                        } else {
                            vhLeftPic.newsitem_commentcount.setVisibility(View.GONE);
                        }
                    } else {
                        vhLeftPic.newsitem_commentcount.setVisibility(View.GONE);
                    }


                    if (CalendarUtil.friendlyTime(bean.getUpdate_time(), context) == null) {
                        vhLeftPic.newsitem_time.setText("");
                    } else {
                        vhLeftPic.newsitem_time.setText(CalendarUtil.friendlyTime(bean.getUpdate_time(), context));
                    }


                    vhLeftPic.newsitem_img.setVisibility(View.VISIBLE);
                    vhLeftPic.nli_foot.setVisibility(View.GONE);

                    vhLeftPic.newsitem_unlike.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(context, "不喜欢", Toast.LENGTH_SHORT).show();
                        }
                    });

                    if ("1".equals(bean.getType())) {
                        vhLeftPic.newsitem_img.setVisibility(View.GONE);
                    }

                    if (!"0".equals(bean.getSid())) {
                        vhLeftPic.nli_foot.setImageResource(R.drawable.zq_subscript_issue);
                        vhLeftPic.nli_foot.setVisibility(View.VISIBLE);
                    }
                    //1新闻  2图集  3直播 4专题  5关联新闻 6视频 7引用
                    if ("2".equals(bean.getRtype())) {
                        vhLeftPic.nli_foot.setImageResource(R.drawable.zq_subscript_album);
                        vhLeftPic.nli_foot.setVisibility(View.VISIBLE);
                    } else if ("3".equals(bean.getRtype())) {
                        vhLeftPic.nli_foot.setImageResource(R.drawable.zq_subscript_live);
                        //				vhLeftPic.nli_foot.setVisibility(View.VISIBLE);
                    } else if ("4".equals(bean.getRtype())) {
                        vhLeftPic.nli_foot.setImageResource(R.drawable.zq_subscript_issue);
                        vhLeftPic.nli_foot.setVisibility(View.VISIBLE);
                    } else if ("7".equals(bean.getRtype())) {
                        vhLeftPic.nli_foot.setImageResource(R.drawable.zq_subscript_html);
                        vhLeftPic.nli_foot.setVisibility(View.VISIBLE);
                    } else if ("6".equals(bean.getRtype())) {
                        vhLeftPic.nli_foot.setImageResource(R.drawable.zq_subscript_video);
                        vhLeftPic.nli_foot.setVisibility(View.VISIBLE);
                    }
                    vhLeftPic.newsitem_img.setImageResource(R.drawable.default_bg);

                    if (vhLeftPic.newsitem_img.getVisibility() == View.VISIBLE
                            && null != bean.getImgs()
                            && bean.getImgs().length > 0) {
                        vhLeftPic.newsitem_title.setPadding(App.px_15dp, 0, 0, 0);
                        vhLeftPic.ll_tag.setPadding(App.px_15dp, 0, 0, 0);
                        SPUtil.displayImage(bean.getImgs()[0], vhLeftPic.newsitem_img,
                                DisplayOptionFactory.getOption(OptionTp.Small));
                    } else {
                        vhLeftPic.newsitem_img.setVisibility(View.GONE);
                        vhLeftPic.newsitem_title.setPadding(0, 0, 0, App.px_15dp);
                        vhLeftPic.ll_tag.setPadding(0, 0, 0, 0);
                    }
                    break;
                case TYPE_AD:
                    bean = list.get(position);
                    holder.itemView.setTag(bean);
                    AdHolder adHolder = (AdHolder) holder;
                    adHolder.newsitem_title.setText(bean.getTitle());
                    adHolder.newsitem_content.setText(bean.getCopyfrom());
                    SPUtil.displayImage(bean.getImgs()[0], adHolder.newsitem_img,
                            DisplayOptionFactory.getOption(OptionTp.Small));
                    ads.get("" + position).registerViewForInteraction(holder.itemView);
                    break;
                case TYPE_BIGPIC:
                    bean = list.get(position);
                    holder.itemView.setTag(bean);
                    break;
                case TYPE_LARGE:
                    bean = list.get(position);
                    holder.itemView.setTag(bean);
                    VHLargePic vhLargePic = (VHLargePic) holder;
                    vhLargePic.newsitem_title.setTextSize(fontSize);
                    vhLargePic.newsitem_title.setText(bean.getTitle());
                    if (readedNewsSet.contains(bean.getNid())) {
                        vhLargePic.newsitem_title.setTextColor(App.getInstance()
                                .getResources().getColor(R.color.grey_font));
                    } else {
                        vhLargePic.newsitem_title.setTextColor(App.getInstance()
                                .getResources().getColor(R.color.black));
                    }

                    if (bean.getAttname() != null) {
                        String attname = bean.getAttname();
                        if (attname.equals("a")) {
                            vhLargePic.item_type_iv.setVisibility(View.VISIBLE);
                            vhLargePic.item_type_iv.setImageResource(R.drawable.zq_subscript_hot);
                        } else if (attname.equals("b")) {
                            vhLargePic.item_type_iv.setVisibility(View.VISIBLE);
                            vhLargePic.item_type_iv.setImageResource(R.drawable.zq_subscript_rekom);
                        } else if (attname.equals("c")) {
                            vhLargePic.item_type_iv.setVisibility(View.VISIBLE);
                            vhLargePic.item_type_iv.setImageResource(R.drawable.zq_subscript_kolom);
                        } else if (attname.equals("f")) {
                            vhLargePic.item_type_iv.setVisibility(View.VISIBLE);
                            vhLargePic.item_type_iv.setImageResource(R.drawable.zq_subscript_fokus);
                        } else if (attname.equals("h")) {
                            vhLargePic.item_type_iv.setVisibility(View.VISIBLE);
                            vhLargePic.item_type_iv.setImageResource(R.drawable.zq_subscript_xtend);
                        } else if (attname.equals("j")) {
                            vhLargePic.item_type_iv.setVisibility(View.VISIBLE);
                            vhLargePic.item_type_iv.setImageResource(R.drawable.zq_subscript_issue);
                        } else if (attname.equals("p")) {
                            vhLargePic.item_type_iv.setVisibility(View.VISIBLE);
                            vhLargePic.item_type_iv.setImageResource(R.drawable.zq_subscript_album);
                        } else if (attname.equals("s")) {
                            vhLargePic.item_type_iv.setVisibility(View.VISIBLE);
                            vhLargePic.item_type_iv.setImageResource(R.drawable.zq_subscript_video);
                        }
                    }

                    fav = bean.getFav();
                    if (!TextUtils.isEmpty(fav)) {
                        int fav_counts = Integer.parseInt(fav);
                        if (fav_counts > 0) {
                            vhLargePic.newsitem_collectcount.setVisibility(View.VISIBLE);
                            vhLargePic.newsitem_collectcount.setText(fav_counts + "");
                        } else {
                            vhLargePic.newsitem_collectcount.setVisibility(View.GONE);
                        }
                    } else {
                        vhLargePic.newsitem_collectcount.setVisibility(View.GONE);
                    }
                    from = bean.getCopyfrom();
                    if (!TextUtils.isEmpty(from)) {
                        vhLargePic.newsitem_source.setVisibility(View.GONE);
                    } else {
                        vhLargePic.newsitem_source.setText(from);
                        vhLargePic.newsitem_source.setVisibility(View.VISIBLE);
                    }
                    comcount = bean.getComcount();
                    if (!TextUtils.isEmpty(comcount)) {
                        int counts = Integer.parseInt(comcount);
                        if (counts > 0) {
                            vhLargePic.newsitem_commentcount.setVisibility(View.VISIBLE);
                            bean.setComcount(counts + "");
                            vhLargePic.newsitem_commentcount.setText(counts + "");
                        } else {
                            vhLargePic.newsitem_commentcount.setVisibility(View.GONE);
                        }
                    } else {
                        vhLargePic.newsitem_commentcount.setVisibility(View.GONE);
                    }


                    if (CalendarUtil.friendlyTime(bean.getUpdate_time(), context) == null) {
                        vhLargePic.newsitem_time.setText("");
                    } else {
                        vhLargePic.newsitem_time.setText(CalendarUtil.friendlyTime(bean.getUpdate_time(), context));
                    }

                    vhLargePic.newsitem_img.setVisibility(View.VISIBLE);
                    vhLargePic.nli_foot.setVisibility(View.GONE);

                    vhLargePic.newsitem_unlike.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(context, "不喜欢", Toast.LENGTH_SHORT).show();
                        }
                    });

                    if (!"0".equals(bean.getSid())) {
                        vhLargePic.nli_foot.setImageResource(R.drawable.zq_subscript_issue);
                        vhLargePic.nli_foot.setVisibility(View.VISIBLE);
                    }
                    //1新闻  2图集  3直播 4专题  5关联新闻 6视频 7引用
                    if ("2".equals(bean.getRtype())) {
                        vhLargePic.nli_foot.setImageResource(R.drawable.zq_subscript_album);
                        vhLargePic.nli_foot.setVisibility(View.VISIBLE);
                    } else if ("3".equals(bean.getRtype())) {
                        vhLargePic.nli_foot.setImageResource(R.drawable.zq_subscript_live);
                        //				vhLargePic.nli_foot.setVisibility(View.VISIBLE);
                    } else if ("4".equals(bean.getRtype())) {
                        vhLargePic.nli_foot.setImageResource(R.drawable.zq_subscript_issue);
                        vhLargePic.nli_foot.setVisibility(View.VISIBLE);
                    } else if ("7".equals(bean.getRtype())) {
                        vhLargePic.nli_foot.setImageResource(R.drawable.zq_subscript_html);
                        vhLargePic.nli_foot.setVisibility(View.VISIBLE);
                    } else if ("6".equals(bean.getRtype())) {
                        vhLargePic.nli_foot.setImageResource(R.drawable.zq_subscript_video);
                        vhLargePic.nli_foot.setVisibility(View.VISIBLE);
                    }

                    if (vhLargePic.newsitem_img.getVisibility() == View.VISIBLE
                            && null != bean.getImgs()
                            && bean.getImgs().length > 0) {
                        SPUtil.displayImage(bean.getImgs()[0], vhLargePic.newsitem_img,
                                DisplayOptionFactory.getOption(OptionTp.Small));
                    } else {
                        SPUtil.displayImage("", vhLargePic.newsitem_img,
                                DisplayOptionFactory.getOption(OptionTp.Small));
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (showLoading && position == getItemCount() - 1) {
            return TYPE_LOADING;
        }
        if (viewPagelist.size() > 0) {
            if (position == 0) {
                return TYPE_FLASH;
            }
            --position;
        }
        NewsBean bean = list.get(position);
        if ("ad".equals(bean.getType())) {
            return TYPE_AD;
        } else if ("4".equals(bean.getType())) {
            return TYPE_THREEPIC;
        } else if ("10".equals(bean.getType())) {
            return TYPE_BIGPIC;
        } else if ("99".equals(bean.getType())) {
            return TYPE_LARGE;
        } else {
            return TYPE_LEFTPIC;
        }
    }

    @Override
    public int getItemCount() {
        int count = list.size() + (viewPagelist.size() > 0 ? 1 : 0);
        if (showLoading) {
            ++count;
        }
        return count;
    }

    public void setFontSize(int mfontSize) {
        this.fontSize = mfontSize;
        notifyDataSetChanged();
    }

    private class AdHolder extends RecyclerView.ViewHolder {
        @ViewInject(R.id.newsitem_title)
        private TextView newsitem_title;
        @ViewInject(R.id.newsitem_source)
        private TextView newsitem_content;
        @ViewInject(R.id.newsitem_img)
        private ImageView newsitem_img;

        public AdHolder(View itemView) {
            super(itemView);
            ViewUtils.inject(this, itemView);
        }

    }

    public class LoadingHolder extends RecyclerView.ViewHolder {
        public LoadingHolder(View itemView) {
            super(itemView);
            ViewUtils.inject(this, itemView);
        }

    }

    private class FlashHolder extends RecyclerView.ViewHolder {
        @ViewInject(R.id.test_pager)
        private TopPicViewPager topViewpager;//顶部轮播图
        @ViewInject(R.id.indicator_default)
        private CircleIndicator indicator_default;
        @ViewInject(R.id.viewpage_txt_id)
        private TextView mTextView;
        @ViewInject(R.id.news_viewpage_myroot)
        private FrameLayout news_viewpage_myroot;//顶部轮播图根布局

        public FlashHolder(View v) {
            super(v);
            ViewUtils.inject(this, v);
            try {
                DisplayMetrics dm = context.getResources().getDisplayMetrics();
                int height = (int) (dm.widthPixels * 0.55);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, height);
                news_viewpage_myroot.setLayoutParams(params);
                mTextView.setText(viewPagelist.get(0).getTitle());
                topViewpager.setCurrentItem(0, true);
                topViewpager.setAdapter(topviewAdapter);
                topViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageSelected(int arg0) {
                        mTextView.setText(topviewAdapter.getBean(arg0).getTitle());//设置小标题
                    }

                    @Override
                    public void onPageScrolled(int arg0, float arg1, int arg2) {

                    }

                    @Override
                    public void onPageScrollStateChanged(int arg0) {

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //三联图
    private class VHThree extends RecyclerView.ViewHolder {
        @ViewInject(R.id.newsitem_title)
        private TextView newsitem_title;
        @ViewInject(R.id.news_3_tv_time)
        private TextView tv3;
        @ViewInject(R.id.news_3_item1)
        private ImageView img0;
        @ViewInject(R.id.news_3_item2)
        private ImageView img1;
        @ViewInject(R.id.news_3_item3)
        private ImageView img2;
        @ViewInject(R.id.newsitem_foot)
        private ImageView newsitem_foot;
        @ViewInject(R.id.newsitem_comments)
        private TextView newsitem_comments;
        @ViewInject(R.id.newsitem_source)
        private TextView newsitem_source;
        @ViewInject(R.id.newsitem_collectcount)
        private TextView newsitem_collectcount;
        @ViewInject(R.id.item_type_iv)
        private ImageView item_type_iv;

        public VHThree(View v) {
            super(v);
            ViewUtils.inject(this, v);
            v.setOnClickListener(onClickListener);
        }
    }

    //左边图片，右title，评论，时间，脚标
    private class VHLeftPic extends RecyclerView.ViewHolder {
        @ViewInject(R.id.newsitem_title)
        private TextView newsitem_title;
        @ViewInject(R.id.nli_foot)
        private ImageView nli_foot;
        //		来源
        @ViewInject(R.id.newsitem_source)
        private TextView newsitem_source;
        //		收藏数
        @ViewInject(R.id.newsitem_collectcount)
        private TextView newsitem_collectcount;
        //		评论数
        @ViewInject(R.id.newsitem_commentcount)
        private TextView newsitem_commentcount;
        @ViewInject(R.id.newsitem_time)
        private TextView newsitem_time;
        @ViewInject(R.id.newsitem_img)
        private ImageView newsitem_img;
        @ViewInject(R.id.newsitem_unlike)
        private ImageView newsitem_unlike;
        @ViewInject(R.id.item_type_iv)
        private ImageView item_type_iv;
        @ViewInject(R.id.ll_tag)
        private LinearLayout ll_tag;

        public VHLeftPic(View v) {
            super(v);
            ViewUtils.inject(this, v);
            v.setOnClickListener(onClickListener);
        }
    }

    //大图
    private class VHLargePic extends RecyclerView.ViewHolder {
        @ViewInject(R.id.newsitem_title)
        private TextView newsitem_title;
        @ViewInject(R.id.nli_foot)
        private ImageView nli_foot;
        //		来源
        @ViewInject(R.id.newsitem_source)
        private TextView newsitem_source;
        //		收藏数
        @ViewInject(R.id.newsitem_collectcount)
        private TextView newsitem_collectcount;
        //		评论数
        @ViewInject(R.id.newsitem_commentcount)
        private TextView newsitem_commentcount;
        @ViewInject(R.id.newsitem_time)
        private TextView newsitem_time;
        @ViewInject(R.id.newsitem_img)
        private ImageView newsitem_img;
        @ViewInject(R.id.newsitem_unlike)
        private ImageView newsitem_unlike;
        @ViewInject(R.id.item_type_iv)
        private ImageView item_type_iv;

        public VHLargePic(View v) {
            super(v);
            ViewUtils.inject(this, v);
            v.setOnClickListener(onClickListener);
        }
    }

    private class VHBigPic extends RecyclerView.ViewHolder {

        @ViewInject(R.id.news_big_item1)
        private ImageView news_big_item1;


        public VHBigPic(View v) {
            super(v);
            ViewUtils.inject(this, v);
            v.setOnClickListener(onClickListener);
        }
    }


}
