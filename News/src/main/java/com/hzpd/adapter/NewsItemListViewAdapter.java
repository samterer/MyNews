package com.hzpd.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
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
import com.hzpd.modle.db.NewsBeanDBDao;
import com.hzpd.ui.App;
import com.hzpd.ui.activity.XF_NewsCommentsActivity;
import com.hzpd.ui.interfaces.I_Result;
import com.hzpd.ui.widget.CircleIndicator;
import com.hzpd.utils.AAnim;
import com.hzpd.utils.CalendarUtil;
import com.hzpd.utils.DBHelper;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.DisplayOptionFactory.OptionTp;
import com.hzpd.utils.SPUtil;
import com.hzpd.utils.db.NewsListDbTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class NewsItemListViewAdapter extends RecyclerView.Adapter {

    public static final String AD_KEY = "1902056863352757_1942167249341718";
    public static final int STEP = 12;
    public static final int MAX_POSITION = 100;

    public interface CallBack {
        void loadMore();
    }

    final Random random = new Random();
    public CallBack callBack;
    Context context;
    LayoutInflater inflater;
    List<NewsBean> list = null;
    List<NewsPageListBean> viewPagelist = new ArrayList<>();
    DBHelper dbHelper;
    List<NewsBean> appendoldlist = null;

    final static int TYPE_FLASH = 9;
    final static int TYPE_THREEPIC = 0;
    final static int TYPE_LEFTPIC = 1;
    final static int TYPE_BIGPIC = 2;
    final static int TYPE_LARGE = 3;
    final static int TYPE_TEXT = 4; // 纯文本
    final static int TYPE_JOKE = 5; // JOKE 段子
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
    int nextAdPosition = 6;

    public void setAds(HashMap<String, NativeAd> ads) {
        this.ads = ads;
    }

    public void setNativeAd() {
        if (ads == null) {
            return;
        }
//        Log.e("test", "nextAdPosition " + nextAdPosition);
        final NativeAd nativeAd = new NativeAd(context.getApplicationContext(), AD_KEY);
        ads.put("" + nextAdPosition, nativeAd);
        final int adPos = nextAdPosition;
        nativeAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
//                Log.e("test", "onError->" + nextAdPosition + ": " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
//                Log.e("test", "onAdLoaded->" + nextAdPosition + ": " + nativeAd);
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
        newsListDbTask = new NewsListDbTask(context);
        readedNewsSet = new HashSet<String>();
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
        if (data == null && isClearOld) {
            list.clear();
            return;
        }
        int index = getItemCount();
        if (showLoading) {
            index = index - 1;
        }
        if (isClearOld) {
            list.clear();
            SPUtil.clearAds(ads);
        }
        list.addAll(data);
        getReaded(data);
        if (isDb) {
            appendoldlist = data;
        }
        if (isClearOld) {
            notifyDataSetChanged();
            nextAdPosition = 6;
        } else {
            notifyItemRangeInserted(index, data.size());
        }
        while (list.size() > nextAdPosition) {
            if (ads == null) {
                return;
            }
            setNativeAd();
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
            NewsBeanDB nbdb = dbHelper.getNewsList().queryBuilder().where(NewsBeanDBDao.Properties.Nid.eq(nid)).build().unique();
            nbdb.setNid(nid);
            nbdb.setIsreaded("1");
            dbHelper.getNewsList().update(nbdb);
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
            case TYPE_FLASH://幻灯
                convertView = inflater.inflate(
                        R.layout.news_item_flash_layout, parent, false);
                viewHolder = new FlashHolder(convertView);
                break;
            case TYPE_TEXT://纯文本
                convertView = inflater.inflate(
                        R.layout.news_list_text_layout, parent, false);
                viewHolder = new TextViewHolder(convertView);
                break;
            case TYPE_JOKE://段子
                convertView = inflater.inflate(
                        R.layout.news_list_joke_layout, parent, false);
                viewHolder = new JokeHolder(convertView);
                break;
            case TYPE_THREEPIC://三张连图
                convertView = inflater.inflate(
                        R.layout.news_3_item_layout, parent, false);
                viewHolder = new VHThree(convertView);
                break;
            case TYPE_LEFTPIC://普通
                convertView = inflater.inflate(
                        R.layout.news_list_item_layout, parent, false);
                viewHolder = new VHLeftPic(convertView);
                break;
            case TYPE_BIGPIC:
                convertView = inflater.inflate(
                        R.layout.news_big_item_layout, parent, false);
                viewHolder = new VHBigPic(convertView);
                break;
            case TYPE_LARGE://大图
                convertView = inflater.inflate(
                        R.layout.news_large_item_layout, parent, false);
                viewHolder = new VHLargePic(convertView);
                break;
            case TYPE_AD://广告
                convertView = inflater.inflate(
                        R.layout.news_list_ad_layout, parent, false);
                viewHolder = new AdHolder(convertView);
                break;
            case TYPE_LOADING://加载
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
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.item_title, typedValue, true);
            int color = typedValue.data;

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
                case TYPE_TEXT: {
                    bean = list.get(position);
                    holder.itemView.setTag(bean);
                    TextViewHolder textViewHolder = (TextViewHolder) holder;
                    textViewHolder.newsitem_title.setTextSize(fontSize);
                    textViewHolder.newsitem_title.setText(bean.getTitle());
                    textViewHolder.newsitem_title.setTextColor(color);
                    if (readedNewsSet.contains(bean.getNid())) {
                        textViewHolder.newsitem_title.setTextColor(App.getInstance()
                                .getResources().getColor(R.color.grey_font));
                    } else {
                        textViewHolder.newsitem_title.setTextColor(color);
                    }
                    SPUtil.setAtt(textViewHolder.item_type_iv, bean.getAttname());

                    String fav = bean.getFav();
                    if (!TextUtils.isEmpty(fav)) {

                        int fav_counts = Integer.parseInt(fav);
                        if (fav_counts > 0) {
                            textViewHolder.newsitem_collectcount.setVisibility(View.VISIBLE);
                            textViewHolder.newsitem_collectcount.setText(fav_counts + "");
                        } else {
                            textViewHolder.newsitem_collectcount.setVisibility(View.GONE);
                        }
                    } else {
                        textViewHolder.newsitem_collectcount.setVisibility(View.GONE);
                    }

                    String from = bean.getCopyfrom();
                    if (!TextUtils.isEmpty(from)) {
                        textViewHolder.newsitem_source.setVisibility(View.VISIBLE);
                        textViewHolder.newsitem_source.setText(from);
                    } else {
                        textViewHolder.newsitem_source.setVisibility(View.GONE);
                    }

                    String comcount = bean.getComcount();
                    if (!TextUtils.isEmpty(comcount)) {
                        int counts = Integer.parseInt(comcount);
                        if (counts > 0) {
                            textViewHolder.newsitem_commentcount.setVisibility(View.VISIBLE);
                            bean.setComcount(counts + "");
                            textViewHolder.newsitem_commentcount.setText(counts + "");
                        } else {
                            textViewHolder.newsitem_commentcount.setVisibility(View.GONE);
                        }
                    } else {
                        textViewHolder.newsitem_commentcount.setVisibility(View.GONE);
                    }


                    if (CalendarUtil.friendlyTime(bean.getUpdate_time(), context) == null) {
                        textViewHolder.newsitem_time.setText("");
                    } else {
                        textViewHolder.newsitem_time.setText(CalendarUtil.friendlyTime(bean.getUpdate_time(), context));
                    }
                    textViewHolder.nli_foot.setVisibility(View.GONE);

                    textViewHolder.newsitem_unlike.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(context, "不喜欢", Toast.LENGTH_SHORT).show();
                        }
                    });

                    if (bean.getSid() != null && !"0".equals(bean.getSid())) {
                        textViewHolder.nli_foot.setImageResource(R.drawable.zq_subscript_issue);
                        textViewHolder.nli_foot.setVisibility(View.VISIBLE);
                    }
                    SPUtil.setRtype(bean.getRtype(), textViewHolder.nli_foot);

                }
                break;
                case TYPE_JOKE: {
                    bean = list.get(position);
                    holder.itemView.setTag(bean);
                    JokeHolder jokeHolder = (JokeHolder) holder;
                    jokeHolder.newsitem_title.setTextSize(fontSize);
                    jokeHolder.newsitem_title.setText(bean.getTitle());
                    jokeHolder.newsitem_title.setTextColor(App.getInstance()
                            .getResources().getColor(R.color.item_title));
                    if (readedNewsSet.contains(bean.getNid())) {
                        jokeHolder.newsitem_title.setTextColor(App.getInstance()
                                .getResources().getColor(R.color.grey_font));
                    } else {
                        jokeHolder.newsitem_title.setTextColor(App.getInstance()
                                .getResources().getColor(R.color.item_title));
                    }

                    String jokeLike=bean.getLike();
                    if (!TextUtils.isEmpty(jokeLike)){
                        jokeHolder.joke_good_tv.setText(""+jokeLike);
                    }

                    String jokeUnlike=bean.getUnlike();
                    if (!TextUtils.isEmpty(jokeUnlike)){
                        jokeHolder.joke_bad_tv.setText(""+jokeUnlike);
                    }

                    String jokeComCounts=bean.getComcount();
                    if (!TextUtils.isEmpty(jokeComCounts)){
                        jokeHolder.joke_comment_counts_tv.setText(jokeComCounts);
                    }
                    final String nid=bean.getNid();
                    jokeHolder.joke_comment_counts_tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent commentsIntent = new Intent(context, XF_NewsCommentsActivity.class);
                            commentsIntent.putExtra("News_nid", nid);
                            context.startActivity(commentsIntent);
                        }
                    });



                }
                break;
                case TYPE_THREEPIC:
                    bean = list.get(position);
                    VHThree vhThree = (VHThree) holder;
                    holder.itemView.setTag(bean);
                    vhThree.newsitem_title.setTextSize(fontSize);
                    vhThree.newsitem_title.setText(bean.getTitle());

                    vhThree.newsitem_title.setTextColor(color);
                    if (readedNewsSet.contains(bean.getNid())) {
                        vhThree.newsitem_title.setTextColor(App.getInstance()
                                .getResources().getColor(R.color.grey_font));
                    } else {
                        vhThree.newsitem_title.setTextColor(App.getInstance()
                                .getResources().getColor(R.color.item_title));
                    }

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


                    SPUtil.setAtt(vhThree.item_type_iv, bean.getAttname());
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
                    SPUtil.setRtype(bean.getRtype(), vhThree.newsitem_foot);
                    break;
                case TYPE_LEFTPIC: {
                    bean = list.get(position);
                    holder.itemView.setTag(bean);
                    VHLeftPic vhLeftPic = (VHLeftPic) holder;
                    vhLeftPic.newsitem_title.setTextSize(fontSize);
                    vhLeftPic.newsitem_title.setText(bean.getTitle());

                    vhLeftPic.newsitem_title.setTextColor(color);

                    if (readedNewsSet.contains(bean.getNid())) {
                        vhLeftPic.newsitem_title.setTextColor(App.getInstance()
                                .getResources().getColor(R.color.grey_font));
                    } else {
                        vhLeftPic.newsitem_title.setTextColor(color);
                    }
                    SPUtil.setAtt(vhLeftPic.item_type_iv, bean.getAttname());

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

                    if (bean.getSid() != null && !"0".equals(bean.getSid())) {
                        vhLeftPic.nli_foot.setImageResource(R.drawable.zq_subscript_issue);
                        vhLeftPic.nli_foot.setVisibility(View.VISIBLE);
                    }
                    SPUtil.setRtype(bean.getRtype(), vhLeftPic.nli_foot);

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
                }
                break;
                case TYPE_AD:
                    try {
                        bean = list.get(position);
                        holder.itemView.setTag(bean);
                        AdHolder adHolder = (AdHolder) holder;
                        NativeAd nativeAd = ads.get("" + position);
                        nativeAd.unregisterView();
                        View view = SPUtil.getRandomAdView(context, nativeAd);
                        adHolder.viewGroup.removeAllViews();
                        adHolder.viewGroup.addView(view);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //ads.get("" + position).registerViewForInteraction(holder.itemView);
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
                    vhLargePic.newsitem_title.setTextColor(color);
                    if (readedNewsSet.contains(bean.getNid())) {
                        vhLargePic.newsitem_title.setTextColor(App.getInstance()
                                .getResources().getColor(R.color.grey_font));
                    } else {
                        vhLargePic.newsitem_title.setTextColor(color);
                    }

                    SPUtil.setAtt(vhLargePic.item_type_iv, bean.getAttname());

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
                    SPUtil.setRtype(bean.getRtype(), vhLargePic.nli_foot);

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
        } else if ("11".equals(bean.getType())) {
            return TYPE_JOKE;
        } else if (bean.getImgs() == null || bean.getImgs().length == 0) {
            return TYPE_TEXT;
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
        ViewGroup viewGroup;

        public AdHolder(View itemView) {
            super(itemView);
            viewGroup = (ViewGroup) itemView.findViewById(R.id.ad_layout);
        }

    }

    public class LoadingHolder extends RecyclerView.ViewHolder {
        public LoadingHolder(View itemView) {
            super(itemView);
        }

    }

    private class FlashHolder extends RecyclerView.ViewHolder {
        private TopPicViewPager topViewpager;//顶部轮播图
        private CircleIndicator indicator_default;
        private TextView mTextView;
        private FrameLayout news_viewpage_myroot;//顶部轮播图根布局

        public FlashHolder(View v) {
            super(v);
            topViewpager = (TopPicViewPager) v.findViewById(R.id.test_pager);
            indicator_default = (CircleIndicator) v.findViewById(R.id.indicator_default);
            mTextView = (TextView) v.findViewById(R.id.viewpage_txt_id);
            news_viewpage_myroot = (FrameLayout) v.findViewById(R.id.news_viewpage_myroot);
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

    //JOKE 段子，评论，时间，脚标
    private class JokeHolder extends RecyclerView.ViewHolder {
        private TextView newsitem_title;
        private TextView joke_good_tv;
        private TextView joke_bad_tv;
        private TextView joke_share_tv;
        private TextView joke_comment_counts_tv;

        public JokeHolder(View v) {
            super(v);
            newsitem_title = (TextView) v.findViewById(R.id.newsitem_title);
            joke_good_tv= (TextView) v.findViewById(R.id.joke_good_tv);
            joke_bad_tv= (TextView) v.findViewById(R.id.joke_bad_tv);
            joke_share_tv= (TextView) v.findViewById(R.id.joke_share_tv);
            joke_comment_counts_tv= (TextView) v.findViewById(R.id.joke_comment_counts_tv);
            v.setOnClickListener(onClickListener);
        }
    }

    //三联图
    private class VHThree extends RecyclerView.ViewHolder {
        private TextView newsitem_title;
        private TextView tv3;
        private ImageView img0;
        private ImageView img1;
        private ImageView img2;
        private ImageView newsitem_foot;
        private TextView newsitem_comments;
        private TextView newsitem_source;
        private TextView newsitem_collectcount;
        private ImageView item_type_iv;

        public VHThree(View v) {
            super(v);
            newsitem_title = (TextView) v.findViewById(R.id.newsitem_title);
            tv3 = (TextView) v.findViewById(R.id.news_3_tv_time);
            newsitem_comments = (TextView) v.findViewById(R.id.newsitem_comments);
            newsitem_source = (TextView) v.findViewById(R.id.newsitem_source);
            newsitem_collectcount = (TextView) v.findViewById(R.id.newsitem_collectcount);
            newsitem_foot = (ImageView) v.findViewById(R.id.newsitem_foot);
            img0 = (ImageView) v.findViewById(R.id.news_3_item1);
            img1 = (ImageView) v.findViewById(R.id.news_3_item2);
            img2 = (ImageView) v.findViewById(R.id.news_3_item3);
            item_type_iv = (ImageView) v.findViewById(R.id.item_type_iv);
            v.setOnClickListener(onClickListener);
        }
    }

    //左边图片，右title，评论，时间，脚标
    private class VHLeftPic extends RecyclerView.ViewHolder {
        private TextView newsitem_title;
        private ImageView nli_foot;
        private TextView newsitem_source;//来源
        private TextView newsitem_collectcount;//收藏数
        private TextView newsitem_commentcount;//评论数
        private TextView newsitem_time;
        private ImageView newsitem_img;
        private ImageView newsitem_unlike;
        private ImageView item_type_iv;
        private LinearLayout ll_tag;

        public VHLeftPic(View v) {
            super(v);
            newsitem_title = (TextView) v.findViewById(R.id.newsitem_title);
            nli_foot = (ImageView) v.findViewById(R.id.nli_foot);
            newsitem_source = (TextView) v.findViewById(R.id.newsitem_source);
            newsitem_collectcount = (TextView) v.findViewById(R.id.newsitem_collectcount);
            newsitem_commentcount = (TextView) v.findViewById(R.id.newsitem_commentcount);
            newsitem_time = (TextView) v.findViewById(R.id.newsitem_time);
            newsitem_img = (ImageView) v.findViewById(R.id.newsitem_img);
            newsitem_unlike = (ImageView) v.findViewById(R.id.newsitem_unlike);
            item_type_iv = (ImageView) v.findViewById(R.id.item_type_iv);
            ll_tag = (LinearLayout) v.findViewById(R.id.ll_tag);
            v.setOnClickListener(onClickListener);
        }
    }

    //纯文本，评论，时间，脚标
    private class TextViewHolder extends RecyclerView.ViewHolder {
        private TextView newsitem_title;
        private ImageView nli_foot;
        private TextView newsitem_source;//来源
        private TextView newsitem_collectcount;//收藏数
        private TextView newsitem_commentcount;//评论数
        private TextView newsitem_time;
        private ImageView newsitem_unlike;
        private ImageView item_type_iv;
        private LinearLayout ll_tag;

        public TextViewHolder(View v) {
            super(v);
            newsitem_title = (TextView) v.findViewById(R.id.newsitem_title);
            nli_foot = (ImageView) v.findViewById(R.id.nli_foot);
            newsitem_source = (TextView) v.findViewById(R.id.newsitem_source);
            newsitem_collectcount = (TextView) v.findViewById(R.id.newsitem_collectcount);
            newsitem_commentcount = (TextView) v.findViewById(R.id.newsitem_commentcount);
            newsitem_time = (TextView) v.findViewById(R.id.newsitem_time);
            newsitem_unlike = (ImageView) v.findViewById(R.id.newsitem_unlike);
            item_type_iv = (ImageView) v.findViewById(R.id.item_type_iv);
            ll_tag = (LinearLayout) v.findViewById(R.id.ll_tag);
            v.setOnClickListener(onClickListener);
        }
    }

    //大图
    private class VHLargePic extends RecyclerView.ViewHolder {
        private TextView newsitem_title;
        private ImageView nli_foot;
        private TextView newsitem_source; //来源
        private TextView newsitem_collectcount;//收藏数
        private TextView newsitem_commentcount;//评论数
        private TextView newsitem_time;
        private ImageView newsitem_img;
        private ImageView newsitem_unlike;
        private ImageView item_type_iv;

        public VHLargePic(View v) {
            super(v);
            newsitem_title = (TextView) v.findViewById(R.id.newsitem_title);
            nli_foot = (ImageView) v.findViewById(R.id.nli_foot);
            newsitem_source = (TextView) v.findViewById(R.id.newsitem_source);
            newsitem_collectcount = (TextView) v.findViewById(R.id.newsitem_collectcount);
            newsitem_commentcount = (TextView) v.findViewById(R.id.newsitem_commentcount);
            newsitem_time = (TextView) v.findViewById(R.id.newsitem_time);
            newsitem_img = (ImageView) v.findViewById(R.id.newsitem_img);
            newsitem_unlike = (ImageView) v.findViewById(R.id.newsitem_unlike);
            item_type_iv = (ImageView) v.findViewById(R.id.item_type_iv);
            v.setOnClickListener(onClickListener);
        }
    }

    private class VHBigPic extends RecyclerView.ViewHolder {

        private ImageView news_big_item1;


        public VHBigPic(View v) {
            super(v);
            news_big_item1 = (ImageView) v.findViewById(R.id.news_big_item1);
            v.setOnClickListener(onClickListener);
        }
    }


}
