
//客户端传参识别，close_lazyload 和 url_prefix可由客户端在<head>内插入<script></script>代码来定义
if(typeof close_lazyload == 'undefined'){
	//图片懒加载开关
	close_lazyload = false;
}
if(typeof url_prefix == 'undefined'){
	//加载图片的客户端协议
	url_prefix = "content:/com.hzpd.provider.ImageProvider/";
}

//
var img_type = "origin", //默认图片类型-原图
	font_size = "m", //默认字号-中号
	night_mode = 0, //默认日夜间模式-白天
	group_id = 0, //文章id
	imgs = [],
	article,
	store = [], //lazyload剩余图片库存
	offset = 100, //图片lazyload默认偏移量
	clicked_toggle_img_btn = false, //标记加载大图事件是否来源于“一键切换大图按钮”
	profile = document.getElementById('profile'), //文章底部头条号容器
	subscribe_btn = document.getElementById('subscribe'), //头条号订阅按钮
	autoplayed = false, //页面第一个视频是否已经自动播放
	videos = []; //正文中所有视频

/**
* 从小图/无图状态点击时加载大图
*/
function largeImg(){
 	window.imagelistner.openImage(this.src);
 	return true;
}

/**
* 图片加载回调 —— 由客户端调用
* 当img的src以getimage/xxx（xxx取值origin/thumb）等形式进行获取时，客户端先从缓存中获取，失败时会触发img的errorimg事件，
* 但是，此时客户端会进一步去下载该图片，下载成功后通过image_load_cb 修改当前<img>元素。
* 所以，在img的errorimg事件中一定不能手动删除加载失败的<img>节点。
*
* @param {number} i 图片索引
* @param {boolean} ok 是否成功
* @param {boolean} is_large 是否为大图
*/
function image_load_client(i, ok, is_large) {
	if (i < 0 || i >= imgs.length){
		return;
	}

	var parent = imgs[i],
		img_a = parent.querySelectorAll('img'),
		_this = null;

	if (img_a.length > 0) {
		_this = img_a[0];
	}

	if (! _this){
		return;
	}

	var large_img = null;
	if (img_a.length > 1) {
		large_img = img_a[1];
	}
	var offline = false, p;

	if (_this.src.indexOf("/getimage/origin/") > 0) {
		p = "image/origin/"
	} else if (_this.src.indexOf("/getimage/thumb/") > 0) {
		p = "image/thumb/";
	} else {
		offline = true;
		if (is_large) {
			p = "image/origin/";
		} else {
			p = "image/thumb/";
		}
	}

	var src = url_prefix + p + src_path;
	if (ok) {
		_this.setAttribute("src", src);
	} else {
		del_elt_class(parent, "loading");
	}
};

/**
* 图片加载成功后处理
*/
function appendimg(){
	var parent = this.parentNode,
		s_w = parent.getAttribute("s_width"),
		s_h = parent.getAttribute("s_height"),
		n_w = this.naturalWidth,
		n_h = this.naturalHeight,
		is_large = false;

	if (n_w == parent.getAttribute("width") && n_h == parent.getAttribute("height")) {
		is_large = true;

		if (this.getAttribute("width") != s_w ) {
			//适用场景：点小图看大图，大图加载成功，把图片容器调整到合适尺寸
			parent.style.width = s_w + "px";
			parent.style.height = s_h + "px";
		}

		del_elt_class(parent, "thumb");
		del_elt_class(parent, "offline");

		unbind_loadOriginImg_trigger(parent);
	} else if (n_w > 0 && n_h > 0) {
		var maxWidth = article.offsetWidth,
			_wh = adjust_origin_scale(n_w, n_h, maxWidth);

		parent.style.width = _wh.w + "px";
		parent.style.height = _wh.h + "px";
	} else {
		//会有可能进入这个分支？
		parent.style.width = "120px";
		parent.style.height = "120px";
	}


	this.style.display = 'block';
	
	var p = parent.firstElementChild;
	if (p && p.tagName == 'IMG' && p != this) {
		parent.removeChild(p);
	}

	del_elt_class(parent, "loading");
};

/**
* 图片加载失败处理
*/
function errorimg(){
	var parent = this.parentNode;

	if (this.src.indexOf(url_prefix + "getimage/none/") == 0) {
		add_elt_class(parent, "offline");

		bind_loadOriginImg_trigger(parent);
	} else if (this.src.indexOf(url_prefix + "image/") == 0) {
		var p = parent.firstElementChild,
			spinner = parent.querySelector('.spinner');

		if (p && p.tagName == 'IMG' && p != this && spinner) {
			spinner.parentNode.removeChild(spinner);
		}
		
		del_elt_class(parent, "offline");
	}

	del_elt_class(parent, "loading");

	//注：此处一定不能在图片加载失败时，立即删除该img。原因见image_load_cb的功能说明
};

/**
* 调整图片显示尺寸（超过正文宽度的一半就用正文宽度代替）
* @param {number} origin_w 原始宽度
* @param {number} origin_h 原始高度
* @param {number} maxWidth 正文宽度
*/
function adjust_origin_scale(origin_w, origin_h, maxWidth){
	var half_maxWidth = 0.5*maxWidth;

	if(!origin_w){
		origin_w = 200;
	}

	var w1 = origin_w,
		h1 = 0;

	if(w1 > half_maxWidth){
		w1 = maxWidth;
	}

	if( !w1 ){
		w1 = 200;
	}

	h1 = parseInt(origin_h * w1 / origin_w);
	if (!h1) {
		h1 = 200;
	}

	return { "w" : w1, "h" : h1 };
}

/**
* 图片容器初始化，调整大图、小图尺寸/编制图片索引/设置ss_href
*/
function recalImgSize() {
	var maxWidth = article.offsetWidth,
		len = imgs.length;

	for(i=0; i<len; i++) {
		var _this 	= imgs[i],
			h 		= _this.getAttribute("height"),
			w 		= _this.getAttribute("width"),
			_wh 	= adjust_origin_scale(w, h, maxWidth),

			thumb_h = _this.getAttribute("thumb_height"),
			thumb_w = _this.getAttribute("thumb_width"),
			_wh2 	= adjust_origin_scale(thumb_w, thumb_h, maxWidth),

			href 	= _this.getAttribute("href") || '';
		
		_this.setAttribute("s_width", _wh.w ); //调整后大图显示宽度
		_this.setAttribute("s_height", _wh.h ); //调整后大图显示高度

		_this.setAttribute("t_width", _wh2.w ); //调整后thumb图显示宽度
		_this.setAttribute("t_height", _wh2.h ); //调整后thumb图显示高度
		
		_this.setAttribute("ss_index", i);

		if (href && href.indexOf("hotnews://large_image") == 0){
			_this.setAttribute("ss_href", href);
		}
	}
}

/**
* 正文中全部图片的加载控制
* @param {string} type 图片类型
*/
function show_images(type){
	var valid_types = ['origin','thumb','none'];
	
	if( valid_types.indexOf(type) == -1 ){
		return;
	}

	var maxWidth = article.offsetWidth,
		imgs_len = imgs.length;

	for (var i = 0; i <  imgs_len; i++) {
		var parent = imgs[i],
			h = parent.getAttribute("s_height"),
			w = parent.getAttribute("s_width"),
			src_path = parent.getAttribute("zip_src_path"),
			src = url_prefix + "getimage/" + type +  "/" + src_path + "/"  + group_id + "/" + i;

		del_elt_class(parent, "offline");
		add_elt_class(parent, "loading");
		
		if (type == 'thumb') {
			add_elt_class(parent, 'thumb');

			h = parent.getAttribute("t_height");
			w = parent.getAttribute("t_width");
			
			bind_loadOriginImg_trigger(parent);
		}

		parent.style.width = w + "px";
		parent.style.height = h + "px";

		if(close_lazyload){
			//关闭了懒加载，直接给img设置src属性
			parent.innerHTML = "<img onload='appendimg.call(this)' style='display:none;' onerror='errorimg.call(this)' src='"+src+"' width='"+w+"' height='"+h+"'/>";
			aler(document.boddy.innerHTML);
		}else{
			//开启了懒加载，在图片容器上添加lazyload信息，在scroll屏幕的时候，由懒加载处理函数动态添加相应<img>元素
			parent.setAttribute('lazy_src', src);
			parent.setAttribute('lazy_w', w);
			parent.setAttribute('lazy_h', h);
		}

		//上面是通过parent.innerHTML方式更新DOM，这里要放到最后设置gif标志
		if(type == 'thumb'){
			toggleGifState(parent, true);
		}
	};
};

/**
* 显示正文图片入口
*/
function showImages() {
	recalImgSize();
	
	show_images(img_type);
}

/**
* 图片懒加载初始化，收集所有待加载图片
*/
function init_lazyload(){
	store = [];

	var nodes = document.querySelectorAll('[lazy_src]'),
		len = nodes.length;
	
	for(var i=0; i<len; i++){
		store.push(nodes[i]);
	}
}

/**
* 图片懒加载-显示符合条件的图片
*/
function _pollImages(){
	var len = store.length;

    if (len > 0){
      for (var i = 0; i < len; i++) {
        var self = store[i];

        if (self && _inView(self)) {
        	var lazy_w = self.getAttribute('lazy_w'),
        		lazy_h = self.getAttribute('lazy_h'),
        		lazy_src = self.getAttribute('lazy_src'),
        		t = document.createElement('div');

        	t.innerHTML = "<img onload='appendimg.call(this)' style='display:none;' onerror='errorimg.call(this)' src='"+lazy_src+"' width='"+lazy_w+"' height='"+lazy_h+"'/>";
			var o = t.firstElementChild;
			t.removeChild(o);
			self.appendChild(o);

			self.removeAttribute('lazy_src');

			store.splice(i, 1);
			len = store.length;
			i--;
        }
      }
    }else{
		document.removeEventListener('scroll',_pollImages, false);
	}
}


/**
* 页面变量初始化
*/
function initVars(){
	imgs 	= document.getElementsByClassName("image");
	metas 	= document.getElementsByTagName("meta");

	var imgs_len = imgs.length;

	//从<meta>标签中提取页面变量

	//从location.search中提取页面变量
	if( location.search ){
	}

	//从location.hash中提取页面变量
	if( location.hash ){
	}
	
	//设置夜间模式

	//设置字体
	
	//对于“关闭懒加载”的wifi情形，当页面图片个数超过10幅时，强制打开懒加载
	if(close_lazyload && imgs_len > 10){
		close_lazyload = false;
	}

}


/**
* 设置日夜间模式
* @param {number} flag 1代表日间，0代表夜间
*/
function setDayMode(flag){
	if(["0","1",0,1].indexOf(flag) < 0 ) return;
	flag = parseInt(flag);

	var b = document.body,
		cn = b.className;

	if(flag){
		b.className = cn.replace("night","");
	}else{
		if(cn.indexOf("night") == -1){
			b.className = cn + " night";
		}
	}
}

/****************************************************************
* 基本封装对象/客户端会调用 ClientFun.setFontSize、 ClientFun.setDayMode
*****************************************************************/
var ClientFun = {
	setFontSize : setFontSize,
	setDayMode : setDayMode
};

/****************************************************************
* 页面初始化入口
*****************************************************************/
function initPage(){

	var images = document.getElementsByTagName("img");
	for(var i=0;i<images.length;i++){
		images[i].onclick=largeImg;
	}
	return;
//TODO alert(document.body.innerHTML);
	//页面相关变量初始化
	initVars();

	//显示视频app4.5+ TODO
//	showCustomVideo();

	// postpone calculate image size, 'article' may not finish layout
	// article width is 0 at this point on some device.
	setTimeout(function() {
		showImages();

		initScrollEvents();

		if(!close_lazyload){
			init_lazyload();//懒加载初始化
			_pollImages();//首次懒加载
			document.addEventListener("scroll",_pollImages,false);
		}
	}, 100);
	
//	init_table(); //TODO
	
	//视频点击播放
	for(var i = 0, len = videos.length; i < len; i++){
		var video = videos[i];
		video.addEventListener('click',function(){playVideo(this,0)},false);
	}

	//
	simulate_active_events();

	//通知客户端domReady
	location.href = "hotnews://domReady";
};

//TODO
document.addEventListener("DOMContentLoaded",initPage,false);

/**
* window.load相关动作
*/
function bodyLoad(){

}
window.addEventListener("load",bodyLoad(), false);
