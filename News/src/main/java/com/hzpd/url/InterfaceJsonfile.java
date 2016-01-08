package com.hzpd.url;

/**
 * @author color
 *         <p/>
 *         http://cb.zyrb.com.cn:73/99cms/api.php?s=/Index/index2
 */
public class InterfaceJsonfile {

    //印尼
    public static final String PATH_ROOT = "http://v2.nutnote.com/api.php?s=";
    public static final String S3 = "http://s3-ap-southeast-1.amazonaws.com/ltcms/";
    //本地测试
//    public static final String PATH_ROOT = "http://10.80.3.123/cmsv2/api.php?s=";

    public static final String SITE = "hflt";//
    public static final String SITEID = "1";//
    // ========================================
    public static final String PWDTYPE = "type";
    public static final int NICKNAME = 1;
    public static final int GENDER = 2;

    // 频道列表
    public static final String CHANNELLIST = S3 + "cms_json/#country#/" + SITE + "/common/type/";// +{$type}(News,Lohas,Album)";
    // 幻灯
    public static final String FLASH = S3 + "cms_json/#country#/" + SITE + "/common/flash/";
    // 配置
    public static final String AD_CONFIG = S3 + "config/android_config.json"; //TODO getAdNew
    //TODO
    public static final String USER_LOG = PATH_ROOT + "/Log/active";
    //TODO 新的推荐频道数据接口
    public static final String CHANNEL_RECOMMEND_NEW = PATH_ROOT + "/ChooseNews/getChoosedNewsByTag";
    // H5 图文直播地址
    public static final String HTMLURL = PATH_ROOT + "api.php?s=/Livemsg/view/lid/";
    // 新闻列表
    public static final String NEWSLIST = PATH_ROOT + "/News/getNewsLastList";
    // 查看评论
    public static final String CHECKCOMMENT = PATH_ROOT + "/Comment/showcomment";
    // 发表评论
    public static final String PUBLISHCOMMENT = PATH_ROOT + "/Comment/setComment";
    // 评论发表回复
    public static final String PUBLISHCOMMENTCOMENT = PATH_ROOT + "/Comment/setReplay";
    // 赞评论
    public static final String PRISE1 = PATH_ROOT + "/Comment/dopraise";
    // 收藏列表
    public static final String COLLECTIONLIST = PATH_ROOT + "/Favorite/getFavoriteListv3";
    // 添加收藏
    public static final String ADDCOLLECTION = PATH_ROOT + "/Favorite/setFavoritev3";
    // 删除收藏
    public static final String DELETECOLLECTION = PATH_ROOT + "/Favorite/setFavoriteDelv3";
    // 是否收藏
    public static final String ISCELLECTION = PATH_ROOT + "/Favorite/isMyFavoritev3";
    // 搜索
    public static final String SEARCH = PATH_ROOT + "/Search/getSearch";
    // 搜索关键词
    public static final String SEARCH_KEY = PATH_ROOT + "/Search/tag";
    // 查看最新评论
    public static final String mLatestComm = PATH_ROOT + "/Comment/latestComment";
    // 查看最热评论
    public static final String mHotComm = PATH_ROOT + "/Comment/hotComment";
    // 第三方登录
    public static final String thirdLogin = PATH_ROOT + "/User/thirdLogin";
    // 查看新闻评论次数
    public static final String commentsConts = PATH_ROOT + "/Stat/getStat";
    // 发表反馈
    public static final String feedback = PATH_ROOT + "/Configs/setFeedback";
    // 视频条目
    public static final String videoItem = PATH_ROOT + "/Video/getVideoItem";
    // 专题栏目列表
    public static final String SUBJECTCOLUMNSLIST = PATH_ROOT + "/Subject/getAllColumns";
    // U_11用户详细信息
    public static final String XF_USERINFO = PATH_ROOT + "/User/userPubInfo";
    //我的评论
    public static final String XF_MYCOMMENTS = PATH_ROOT + "/Comment/getMyCommentsv2";
    //赞和踩
    public static final String News_Price = PATH_ROOT + "/Like/like";
    public static final String discovery_url = PATH_ROOT + "/Tag/discovery";
    public static final String classify_top_url = PATH_ROOT + "/Tag/category";
    public static final String classify_url = PATH_ROOT + "/Tag/tagList";
    public static final String tag_news_url = PATH_ROOT + "/Tag/news";
    public static final String tag_click_url = PATH_ROOT + "/Tag/attention";


}