package com.hzpd.url;

/**
 * @author color
 *         <p/>
 *         http://cb.zyrb.com.cn:73/99cms/api.php?s=/Index/index2
 */
public class InterfaceJsonfile {

    //本地测试
//http://10.80.3.123/cmsv2
    //默认
//    public static final String host1 = "http://10.80.3.123/99cms";
//    private static final String host2 = "http://10.80.3.123/99cms_jhxt";
    //    public static final String host1 = "http://ec2-52-76-26-225.ap-southeast-1.compute.amazonaws.com/1tcms";
//    private static final String host2 = "http://ec2-52-76-26-225.ap-southeast-1.compute.amazonaws.com/1tcms";
//印尼
    public static final String host1 = "http://ec2-52-76-26-225.ap-southeast-1.compute.amazonaws.com/ltcms";
//    public static final String host1 = "http://10.80.3.123/cmsv2";
    // ========================================
    public static final String PATH_ROOT = host1 + "/api.php?s=";
    public static final String ROOT = host1 + "/";

    public static final String SITE = "hflt";//
    public static final String SITEID = "1";//

    // ========================================
    public static final String PWDTYPE = "type";
    public static final int NICKNAME = 1;
    public static final int GENDER = 2;

    //TODO 新增

    public static final String USER_LOG = PATH_ROOT + "/Log/active";

    //TODO 新的推荐频道数据接口
    public static final String CHANNEL_RECOMMEND_NEW = PATH_ROOT + "/ChooseNews/getChoosedNewsByTag";
    //查看统计
    public static final String Stat = PATH_ROOT + "/Stat/getStat";
    //S_1. 添加浏览量（/Stat/setView）POST
    public static final String XF_BROWSE = PATH_ROOT + "/Stat/setView";
    // H5 图文直播地址
    public static final String HTMLURL = ROOT + "api.php?s=/Livemsg/view/lid/";
    // ========================================
    //推荐频道数据接口
    public static final String CHANNEL_RECOMMEND = PATH_ROOT + "/ChooseNews/getChoosedNews";

    // 频道列表
    public static final String CHANNELLIST = ROOT + "cms_json/" + SITE + "/common/type/";// +{$type}(News,Lohas,Album)";
    // 开屏图片
    public static final String mAdPic = PATH_ROOT + "/Version/getAdNew";
    // 缓存
    public static final String CACHE = PATH_ROOT + "/News/getAppCache";
    // 幻灯
    public static final String FLASH = ROOT + "cms_json/" + SITE + "/common/flash/";

    // 新闻列表
    public static final String NEWSLIST = PATH_ROOT + "/News/getNewsLastList";

    // 图集列表
    public static final String ALBUMLIST = PATH_ROOT + "/Album/getAlbumLastList";
    //
    public static final String VIDEOLIST = PATH_ROOT + "/Video/getVideoLastList";
    //
    // 登陆
    public static final String LOGIN = PATH_ROOT + "/User/ucenterLogin";
    // 注册
    public static final String REGISTER = PATH_ROOT + "/User/ucenterReg";
    // 更改密码
    public static final String CHANGEPWD = PATH_ROOT + "/User/resetPwd";
    // 修改用户昵称
    public static final String CHANGEPINFO = PATH_ROOT + "/User/cmsChangeUser";
    // 修改用户性别
    public static final String CHANGEGENDER = PATH_ROOT + "/User/reviseSex";
    // 绑定手机号
    public static final String BINDERPHONE = PATH_ROOT + "/User/bindMobile";
    // 解绑手机号
    public static final String UNBINDERPHONE = PATH_ROOT + "/User/freeMobile";

    // 查看评论
    public static final String CHECKCOMMENT = PATH_ROOT + "/Comment/showcomment";
    // 发表评论
    public static final String PUBLISHCOMMENT = PATH_ROOT + "/Comment/setComment";
    // 发表评论2
    public static final String PUBLISHCOMMENT2 = PATH_ROOT + "/Comment/setCommentv2";
    // 评论发表回复
    public static final String PUBLISHCOMMENTCOMENT = PATH_ROOT + "/Comment/setReplay";
    // 删除回复
    public static final String DELETEREPLY = PATH_ROOT + "/Comment/delReplay";
    // 赞评论
    public static final String PRISE1 = PATH_ROOT + "/Comment/dopraise";

    public static final String PRISE = PATH_ROOT + "/Comment/dopraisev2";
    // 收藏列表
    public static final String COLLECTIONLIST = PATH_ROOT + "/Favorite/getFavoriteListv3";
    // 添加收藏
    public static final String ADDCOLLECTION = PATH_ROOT + "/Favorite/setFavoritev3";
    // 删除收藏
    public static final String DELETECOLLECTION = PATH_ROOT + "/Favorite/setFavoriteDelv3";
    // 是否收藏
    public static final String ISCELLECTION = PATH_ROOT + "/Favorite/isMyFavoritev3";

    // 图集index
    public static final String ALBUMINDEX = ROOT + "cms_json/" + SITE + "/Album/List/index";
    // 图集latest
    public static final String ALBUMLATEST = ROOT + "cms_json/" + SITE + "/Album/List/latest";
    // 图片集页page
    public static final String ALBUMPAGE = ROOT + "cms_json/" + SITE + "/Album/List/";

    // 专题详情列表页
    public static final String SUBJECTDETAIL = ROOT + "cms_json/" + SITE + "/Subject/Content/";

    // 视频列表

    // 搜索
    public static final String SEARCH = PATH_ROOT + "/Search/getSearch";

    // 搜索关键词
    public static final String SEARCH_KEY = PATH_ROOT + "/Search/tag";

    //
    // 查看最新评论
    public static final String mLatestComm = PATH_ROOT + "/Comment/latestComment";
    // 查看最热评论
    public static final String mHotComm = PATH_ROOT + "/Comment/hotComment";
    // 我的评论
    public static final String myComm = PATH_ROOT + "/Comment/mycomment";
    // 修改头像
    public static final String changePhoto = PATH_ROOT + "/User/changeUserAvatar";
    // 短信验证码
    public static final String smsCode = PATH_ROOT + "/User/getShortMes";
    // 第三方登录
    public static final String thirdLogin = PATH_ROOT + "/User/thirdLogin";

    // 自浏览器打开获取新闻详情
    public static final String bnewsItem = PATH_ROOT + "/News/getNewsItem";
    // 自浏览器打开获取图集详情
    public static final String bAlbum = ROOT + "api.php?s=/Photo/getPhotoInfoByPID";
    // 查看新闻评论次数
    public static final String commentsConts = PATH_ROOT + "/Stat/getStat";

    // 发表反馈
    public static final String feedback = PATH_ROOT + "/Configs/setFeedback";

    // HTML5 扩展链接
    public static final String html5 = PATH_ROOT + "/H5extend/getH5List";

    // 专题数目
    public static final String subjectNum = ROOT + "api.php?s=/Subject/getSubjectNewsNumv2";
    // 视频条目
    public static final String videoItem = ROOT + "api.php?s=/Video/getVideoItem";

    // 报刊信息
    public static final String NEWSPAGER = ROOT + "cms_json/" + SITE + "/Newspaper/";
    // 新闻列表详情
    public static final String NEWSPAGERLIST = ROOT + "cms_json/" + SITE + "/Content/";
    public static final String NEWSPAGERDATE = ROOT + "api.php?s=/Newspaper/getDate";

    // 专题列表页
    public static final String SUBJECTLIST = NEWSLIST;
    // 专题栏目列表
    public static final String SUBJECTCOLUMNSLIST = ROOT + "api.php?s=/Subject/getAllColumns";
    // 投诉爆料列表
    public static final String TSBLLIST = PATH_ROOT + "/Tipoffs/getTipoffsList";
    // 投诉爆料详情
    public static final String TSBLDETAIL = PATH_ROOT + "/Tipoffs/getTipoffsInfo";
    // 发投诉爆料
    public static final String TSBLPUBLISH = PATH_ROOT + "/Tipoffs/setTipoffs";
    // 发投诉爆料_添加图片
    public static final String TSBLUPLOADPIC = PATH_ROOT + "/Tipoffs/addImg";
    // 评论投诉爆料
    public static final String TSBLCOMMENT = PATH_ROOT + "/Tipoffs/setComment";
    //

    // ==========================================
    public static final String bm_gslk = "http://m.hbgajg.com/";
    public static final String bm_lcsk = "http://touch.qunar.com/h5/train/?from=touchindex&startStation=%E7%9F%B3%E5%AE%B6%E5%BA%84";
    public static final String bm_gjcx = "http://zuoche.com/touch/searincity.jspx?cityname=%E7%9F%B3%E5%AE%B6%E5%BA%84";
    public static final String bm_ctky = "http://zuoche.com/touch/transfromcity.jspx";
    public static final String bm_hbdt = "http://touch.qunar.com/h5/flight/?startCity=%E7%9F%B3%E5%AE%B6%E5%BA%84";

    public static final String bm_wzcx = "http://m.hbgajg.com/";
    public static final String bm_hcpcx = "http://zuoche.com/touch/transfromcity.jspx";
    public static final String bm_kdcx = "http://m.kuaidi100.com/index_all.html#input";
    // ==========================================
    // 新闻index
    public static final String PAGEINDEX = "http://101.200.174.98:81/cms_json/cscec/News/list/";
    // 新闻列表页
    public static final String PAGE = "http://101.200.174.98:81/cms_json/cscec/News/list/";
    // ==========================================
    // 积分
    public static final String XF_UPLOADEVENT = PATH_ROOT + "/Credit/setCredit";


    public static final String ABOUTUS = "http://www.joymeng.com/index.php?m=content&c=index&a=show&catid=6&id=59";
    // 赞评论
    public static final String XF_PRAISECOM = PATH_ROOT + "/Comment/dopraisev2";
    // U_11用户详细信息
    public static final String XF_USERINFO = PATH_ROOT + "/User/userPubInfo";
    // C_7_v2.我的评论
    public static final String XF_MYCOMMENTS = PATH_ROOT + "/Comment/getMyCommentsv2";
    // 短信验证
    public static final String SMS_VERIFY = PATH_ROOT + "/User/verify";
    //
    public final static String API_DETAIL = PATH_ROOT + "?s=/Magazine/api/api/article_view/id/";
    //
    // 发投诉爆料_添加图片
    public static final String TSBLADDIMG = PATH_ROOT + "/Tipoffs/addImg";
    // ===========================================

    // 活动列表
    public static final String actionList = PATH_ROOT + "/Activity/getactivitys";
    // 活动详情
    public static final String actionDetail = PATH_ROOT + "/Activity/getTheActivity";
    // 活动投票
    public static final String actionVote = PATH_ROOT + "";
    // 活动报名
    public static final String actionReg = PATH_ROOT + "";
    // 活动报名配置
    public static final String actionConf = PATH_ROOT + "/Activity/getOptionConfig";
    // 活动报名提交
    public static final String actionRegSubm = PATH_ROOT + "/Activity/setOption";
    //
    //
    // 获取投票活动列表
    public static final String mVotesubs = PATH_ROOT + "/Vote3/getvotesubs";
    // 获取投票基本信息
    public static final String mVoteinfo = PATH_ROOT + "/Vote3/getvoteinfo";
    // 获取投票选项的所有可用类型
    public static final String mVotetys = PATH_ROOT + "/Vote3/getvotetypes";
    // 获取投票选项列表信息
    public static final String mVoteopts = PATH_ROOT + "/Vote3/getvoteopts";
    // 提交投票
    public static final String mSetvote = PATH_ROOT + "/Vote3/setvote";
    // 查看投票选项详情
    public static final String mOptbyoptid = PATH_ROOT + "/Vote3/getoptbyoptidv2";

    // 获取活动的奖项设置
    public static final String priceItem = PATH_ROOT + "/Prize/priceItems";
    // 用户抽奖
    public static final String drawPrice = PATH_ROOT + "/Prizev1/drawPrize";
    // 用户填写具体兑奖信息
    public static final String submitPriceInfo = PATH_ROOT + "/Prizev1/submituserinfo";

    //    FILE_ROOT
    public static final String News_Price = PATH_ROOT + "/Like/like";


    // ===========================================
    //
    //
    //
    //
    //

    // 更新
    public static final String GET_VERSION = PATH_ROOT + "/Version/getVersion";

}