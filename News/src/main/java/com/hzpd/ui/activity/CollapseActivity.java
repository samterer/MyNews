package com.hzpd.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hzpd.custorm.CircleImageView;
import com.hzpd.hflt.R;
import com.hzpd.modle.XF_UserInfoBean;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.utils.DisplayOptionFactory;
import com.hzpd.utils.FjsonUtil;
import com.hzpd.utils.SPUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.util.ArrayList;
import java.util.List;

public class CollapseActivity extends ActionBarActivity implements AbsListView.OnScrollListener{

    private PullToRefreshListView pushmsg_lv;
    private ListView listView;
    private Toolbar toolbar;
    private TextView floatTitle;
    private ImageView headerBg;


    private float headerHeight;//顶部高度
    private float minHeaderHeight;//顶部最低高度，即Bar的高度
    private float floatTitleLeftMargin;//header标题文字左偏移量
    private float floatTitleSize;//header标题文字大小
    private float floatTitleSizeLarge;//header标题文字大小（大号）

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collapse);
        initMeasure();
        initView();
        initListViewHeader();
        initListView();
        initEvent();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(CollapseActivity.this, "位置" + position, Toast.LENGTH_SHORT).show();
            }
        });
        toolbar.setTitle("");
        getUserInfoFromServer();
    }

    private void initMeasure(){
        headerHeight = getResources().getDimension(R.dimen.header_height);
        minHeaderHeight = getResources().getDimension(R.dimen.abc_action_bar_default_height_material);
        floatTitleLeftMargin = getResources().getDimension(R.dimen.float_title_left_margin);
        floatTitleSize = getResources().getDimension(R.dimen.float_title_size);
        floatTitleSizeLarge = getResources().getDimension(R.dimen.float_title_size_large);
    }
    private void initView() {
        pushmsg_lv = (PullToRefreshListView) findViewById(R.id.lv_main);
        floatTitle = (TextView) findViewById(R.id.tv_main_title);
        toolbar = (Toolbar) findViewById(R.id.tb_main);
//        toolbar.
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }



    private TextView test;
    private CircleImageView my_avatar;
    private void initListViewHeader() {
        View headerContainer = LayoutInflater.from(this).inflate(R.layout.header_collapse, listView, false);
        headerBg = (ImageView) headerContainer.findViewById(R.id.img_header_bg);
        my_avatar= (CircleImageView) headerContainer.findViewById(R.id.my_avatar);
        test= (TextView) headerContainer.findViewById(R.id.test);
//        test.setText("姓名");
        listView=pushmsg_lv.getRefreshableView();
        listView.addHeaderView(headerContainer);
    }

    private void initListView() {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            data.add(String.valueOf(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.activity_list_item, android.R.id.text1, data);
        listView.setAdapter(adapter);
    }


    private void initEvent() {
        listView.setOnScrollListener(this);
    }
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        //y轴偏移量
        float scrollY = getScrollY(view);

        //变化率
        float headerBarOffsetY = headerHeight - minHeaderHeight;
        float offset =  1 - Math.max((headerBarOffsetY - scrollY) / headerBarOffsetY, 0f);

        //Toolbar背景色透明度
        toolbar.setBackgroundColor(Color.argb((int) (offset * 255), 63, 81, 181));
//        toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar_bg));

        //header背景图Y轴偏移  让背景图出现变动效果，偏移量为1/2 保证图片不会跟listview 分离
        headerBg.setTranslationY(scrollY / 2);
        //去掉标题文字的显示
        toolbar.setTitle("");
    }

    public  float getScrollY(AbsListView view){
        //计算滚动了多长位置
        View c = view.getChildAt(0);  //获取第一位置的item
        if(c == null){
            return 0;
        }

        int firstVisiblePosition = view.getFirstVisiblePosition(); //可视界面里第一个item 的position
        int top  = c.getTop();  //可视界面里第一个item 高度，滚动出界面的是负数
        float headerHeight = 0 ;
        if(firstVisiblePosition >=1){  //hearview 占据第0位置
            headerHeight = this.headerHeight;
        }
        return -top+firstVisiblePosition*c.getHeight()+headerHeight;
    }
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }
    protected HttpUtils httpUtils;
    private XF_UserInfoBean userInfoBean;
    private void getUserInfoFromServer() {

        httpUtils = new HttpUtils();
        RequestParams params = new RequestParams();
        params.addBodyParameter("uid", "" + "52");
        params.addBodyParameter("siteid", InterfaceJsonfile.SITEID);

        httpUtils.send(HttpRequest.HttpMethod.POST
                , InterfaceJsonfile.XF_USERINFO
                , params
                , new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String json = responseInfo.result;
                android.util.Log.i("getUserInfoFromServer", json.toString());

                JSONObject obj = FjsonUtil
                        .parseObject(responseInfo.result);
                if (null != obj) {
                    if (200 == obj.getIntValue("code")) {
                        userInfoBean = FjsonUtil.parseObject(obj.getString("data")
                                , XF_UserInfoBean.class);
                        setUserInfo();
                    } else {
                    }
                } else {
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {

            }
        });
    }

    private void setUserInfo() {
        if (null == userInfoBean) {
            return;
        }
        int progress = 0;
        int maxProgress = 0;
        String sprogress = userInfoBean.getExp();
        String slastProgress = userInfoBean.getLastexp();
        if (!TextUtils.isEmpty(sprogress)
                && TextUtils.isDigitsOnly(sprogress)
                && TextUtils.isDigitsOnly(slastProgress)) {
            try {
                progress = Integer.parseInt(sprogress);
                int lastProgress = Integer.parseInt(slastProgress);
                if (-1 == lastProgress) {
                    maxProgress = progress;
                } else {
                    maxProgress = lastProgress + progress;
                }

            } catch (Exception e) {
            }
        }


        android.util.Log.e("test", userInfoBean.getAvatar_path());
        SPUtil.displayImage(userInfoBean.getAvatar_path()
                , my_avatar
                , DisplayOptionFactory.getOption(DisplayOptionFactory.OptionTp.Avatar));
        test.setText(userInfoBean.getNickname());

    }

}
