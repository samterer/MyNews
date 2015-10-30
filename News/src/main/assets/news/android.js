
/**
* https://wiki.bytedance.com/pages/viewpage.action?pageId=10913220
*/

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
* @param {object} parent 图片容器
*/
function show_large_image(parent) {
	var is_offline = has_elt_class(parent, "offline");
	
	if (is_offline) {
		del_elt_class(parent, "thumb");
		del_elt_class(parent, "offline");
	}
	add_elt_class(parent, "loading");
	
	var img = null,
		img_a = parent.querySelectorAll('img');

	if (img_a.length > 0) {
		img = img_a[0];
	}

	if ( img ) {
		var n = null;
		if (img_a.length > 1) {
			n = img_a[1];
		}
		if ( n ){
			return;
		}
		// remove thumb img if it's not compelete
		if (img.naturalWidth == 0) {
			parent.removeChild(img);
		}
	}

	var index = parent.getAttribute("ss_index"),
		h = parent.getAttribute("s_height"),
		w = parent.getAttribute("s_width"),
		src_path = parent.getAttribute("zip_src_path"),
		src = url_prefix + "getimage/origin/" + src_path + "/"  + group_id + "/" + index,
		t = document.createElement('div');

	t.innerHTML = "<img onload='appendimg.call(this)' style='display:none' onerror='errorimg.call(this)' src='"+src+"' alt_src='"+src+"' width='1' height='1'/>";
	var o = t.firstElementChild;
	t.removeChild(o);
	parent.appendChild(o);
	
	if (!is_offline) {
		var spinner = document.createElement("i");
		spinner.className = 'spinner';
		parent.appendChild(spinner);
	}

	setTimeout(function(){
		var event = "hotnews://";
		if (is_offline) {
			event += "show_image";
		} else {
			event += "origin_image";
		}
		event += "?index=" + index;
		window.location.href = event;
	}, 500);

	unbind_loadOriginImg_trigger(parent);
}

/**
* 点击“小图/无图”加载“origin原图”事件处理函数
* @param {object} e 点击事件event对像
*/
function loadOriginImg_handler(e){
	var that = this;

	setTimeout(function(){
		show_large_image(that);
	}, 100);
	
	e.preventDefault();
}

/**
* 绑定点击“小图/无图”加载“origin原图”事件
* @param {object} a 图片链接
*/
function bind_loadOriginImg_trigger(a) {
	if (a.getAttribute("ss_href")){
		a.setAttribute("href", "javascript:void(0)");
		a.addEventListener("click", loadOriginImg_handler, false);
	}
}

/**
* 取消绑定点击“小图/无图”加载“origin原图”事件
* @param {object} a 图片链接
*/
function unbind_loadOriginImg_trigger(a) {
	a.removeEventListener("click", loadOriginImg_handler, false);
	if ( a.getAttribute("ss_href") ){
		a.setAttribute("href", a.getAttribute("ss_href"));
	}
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
* 点击“显示大图”按钮，加载大图
*/
function toggle_images(){

	show_images('origin');

	if(!close_lazyload){
		clicked_toggle_img_btn = true;

		//一键显示大图时，需要重构lazyload相关数据结构及第一屏图片懒加载处理
		init_lazyload();
		_pollImages();

		//重新绑定scroll事件，避免在“小图”模式已经removeEventListener掉了
		document.addEventListener("scroll",_pollImages,false);
	}
	
	setTimeout(function(){
		window.location.href = "hotnews://toggle_image?action=show";
	}, 500);
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
* 判断图片是否处于屏幕可视区域，在屏幕以下offset处即触发懒加载，与视频的判断差别
* @param {object} element 图片容器DOM
*/
function _inView(element) {
    var coords = element.getBoundingClientRect();
    if(coords.top < 0){
        return true;
    }else{
        return ((coords.top >= 0 && coords.left >= 0 && coords.top) <= (window.innerHeight || document.body.clientHeight) + offset);
    }
}

/**
* gif图片增加特定标识
* @param {object} holder 图片容器
* @param {boolean} show 是否需要展示gif标识
*/
function toggleGifState(holder, show){
	if(!holder) return;
	if(holder.getAttribute("type") !== 'gif') return;

	var gif_play  = holder.querySelector(".gif_play");
	if (show) {
		if (!gif_play) {
			gif_play = document.createElement("i");
			gif_play.className = 'gif_play';
			holder.appendChild(gif_play);
		}
	} else {
		if(gif_play){
			gif_play.parentNode.removeChild(gif_play);
		}
	}
};

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
* 设置正文字号大小
* @param {string} s 取值s/m/l/xl，代表小/中/大/超大
*/
function setFontSize(s){
	if(["s","m","l","xl"].indexOf(s) > -1) {
		var body = document.body;

		del_elt_class(body, "font_s");
		del_elt_class(body, "font_m");
		del_elt_class(body, "font_l");
		del_elt_class(body, "font_xl");
		add_elt_class(body, "font_"+s);
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
 //document.addEventListener("DOMContentLoaded",initPage,false);

/**
* window.load相关动作
*/
function bodyLoad(){

}
window.addEventListener("load",bodyLoad(), false);

/**
* 功能不知，从老版本中保留下来
*/
function initScrollEvents(){
	var doubleinnerHeight = innerHeight*2,
		scrollOneScreen = document.height <= innerHeight,
		scrollDoubleScreen = document.height <= doubleinnerHeight,
		scrollBottom = false;
	
	if(!scrollBottom && document.height <= innerHeight){
		window.location.href = "hotnews://finish_content";
		scrollBottom = true;
	}else{
		window.onscroll = function(){
			if(!scrollOneScreen && (scrollY > innerHeight)){
				//console.log("滚过一屏");
				window.location.href = "hotnews://read_content";
				scrollOneScreen = true;
			};
			
			if(!scrollDoubleScreen && (scrollY > doubleinnerHeight)){
				//console.log("滚过两屏");
				window.location.href = "hotnews://finish_content";
				scrollDoubleScreen = true;
			};		
			
			//修复部分android不能自动加载的问题
			if(!scrollDoubleScreen && !scrollBottom && scrollY > document.height - innerHeight - 5){
				window.location.href = "hotnews://finish_content";
				scrollBottom = true;
			};
		}
	}
}

/**
* 模拟a:active效果，安卓平台对touchmove/touchend的检测机制不够好
*/
function simulate_active_events(){
	document.addEventListener("touchstart", function(e){
		var elt = e.target;

		if( has_elt_attr(elt, 'tt-press') ){
			add_elt_class(elt, 'active');

			setTimeout(function(){
				del_elt_class(elt, 'active');
			},300);
		}
	}, false);

	/*
	document.addEventListener("touchmove", function(e){
		var elt = e.target;

		if( has_elt_attr(elt, 'tt-press') ){
			del_elt_class(elt, 'active');
		}
	}, false);

	document.addEventListener("touchend", function(e){
		var elt = e.target;
		
		if( has_elt_attr(elt, 'tt-press') ){
			del_elt_class(elt, 'active');
		}
	}, false);*/
}

/**
* 显示页面中的视频，解析<div class="custom-video" data-width="xxx" data-height="yyy" data-poster="zzz"></div>
*/
function showCustomVideo(){
	var len = videos.length;
	for(var i=0;i<len;i++){
		var video = videos[i],
			w = video.getAttribute('data-width') || 0,
			h = video.getAttribute('data-height') || 0,
			poster = video.getAttribute('data-poster') || '',
			max_ratio = 75,//最大展示height:width比
			ratio = max_ratio,//default
			rel_ratio = 0,
			style = '';
		
		if(w && h){
			rel_ratio = (100*h/w).toFixed(2);
			if(rel_ratio <= max_ratio){
				ratio = rel_ratio;
			}
		}
		
		if(rel_ratio > max_ratio){
			style = 'height:100%;width:auto;';
		}

		video.style.paddingBottom = ratio + '%';
		video.innerHTML = '<img src="' + poster + '" onload="appendVideoImg.call(this)" onerror="errorVideoImg.call(this)" style="' + style + '"/><i class="custom-video-trigger"></i>';
	}
}

/**
* 调用客户端播放器进行视频播放
* @param {object} that 视频容器<div class="custom-video"></div>
* @param {number} status 区分是用户点击视频，还是视频自动播放，其中 0 代表点击播放，1 代表自动播放 
*/
function playVideo(that,status){
	var sp = that.getAttribute('data-sp'),
		vid = that.getAttribute('data-vid'),
		v_size = that.getAttribute('data-video-size'), //安卓客户端依赖这个参数，iOS不需要
		coords = that.getBoundingClientRect(),
		frame = [
				coords.left,
				that.offsetTop,
				640,
				435
				];
	if(v_size){
		var obj = null;
		try{
			obj = JSON.parse(v_size);
		}catch(ex){}
		if(obj && obj.normal && obj.normal.h && obj.normal.w){
			frame[2] = obj.normal.w;
			frame[3] = obj.normal.h;
		}
	}
	window.ToutiaoJSBridge.call("playNativeVideo",{sp:sp,vid:vid,frame:frame,status:status},video_cb);
}

/**
* 调用客户端播放器时的回调函数，调整视频播放时位置（插入到页面顶部，正文中视频暂时隐藏）
* @param {object} o 客户端回调传送对象
*/
function video_client(o){
	if(o.code == 1){
		var video = document.querySelector('[data-vid="'+o.vid+'"]');
		if(video){
			video.style.display = 'none';
			document.body.style.marginTop = o.height + 'px';
		}
	}
}

/**
* 视频封面图加载成功，调整父节点背景为纯黑
*/
function appendVideoImg(){
	var pNode = this.parentNode;
	pNode && (pNode.style.background = '#000');
}

/**
* 视频封面图加载失败，删除<img>标签
*/
function errorVideoImg(){
	var pNode = this.parentNode;
	if(pNode){
		pNode.removeChild(this);
	}
}

/**
* 判断视频是否处于屏幕可视区域
* @param {object} element 视频容器DOM
*/
function _videoInView(element) {
	var coords = element.getBoundingClientRect(),
		video_height = coords.height || 100;
	return ((coords.top >= 0 && coords.left >= 0 && coords.top) <= (window.innerHeight || document.documentElement.clientHeight) - video_height);
}

/**
* 关闭视频时，APP调用此函数通知web，恢复视频原始位置 —— 由客户端调用
* @param {number} vid 视频ID
*/
function appCloseVideoNoticeWeb(vid){
	var video = document.querySelector('[data-vid="'+vid+'"]');
	if(video){
		video.style.display = 'block';
		document.body.style.marginTop = '0px';
	}
}

/**
* 查询location.hash中特定值，或根据传入object重设location.hash
* @param {string}/{object} arg 如果是{string}表示查询，{object}表示重设
*/
function hash(arg) {
	var s = location.hash.substr(1),
		hashQuery = {};

	if (s) {
		var arr = s.split("&");
		for (var i = 0; i < arr.length; i++) {
			var t = arr[i].split("=");
			hashQuery[t[0]] = t[1];
		}
	}

	if (typeof arg == "string") {
		return hashQuery[arg];
	}

	if (typeof arg == "object") {
		for (var k in arg) {
			hashQuery[k] = arg[k];
		}
		var s2 = "";
		for (var k in hashQuery) {
			s2 += k + "=" + hashQuery[k] + "&";
		}
		location.href = "#" + s2.substring(0, s2.length - 1);
	}
}

/**
* 获取location.search中的指定参数的值
* @param {string} paras 待查询参数
*/
function request(paras){ 
	var s = location.search.substr(1),
		paraObj = {};

	if(s){
		var arr = s.split("&");
		for(var i = 0; i< arr.length; i++){
			var t = arr[i].split("=");
			paraObj[t[0]] = t[1];
		}
	}
	return paraObj[paras.toLowerCase()];
};

/**
* 调整正文中的可搜索关键字链接
*/
function fixHref(){
	var links = document.querySelectorAll("[pro-href]"),
		len = links.length;

	for(var i = 0; i < len; i++){
		var h = links[i].getAttribute("pro-href");
		links[i].setAttribute("href",h);
	}
};

/**
* 优化正文中的表格交互行为
*/
function init_table(){
	var tables = document.querySelectorAll("table"),
		len = tables.length;

	for(var i=0; i<len; i++){
		var t = tables[i],
			tableimgs = t.querySelectorAll(".image");

		if(!tableimgs.length){
			add_elt_class(t, "border");
			//安卓平台还没时间做wrap,故注释掉
			//t.wrap("<div class='horizontal_scroll'/>")
			//以下代码为4.5版本新增，但暂时不放开，待PM确认
			//问题1：安卓不能局部hotnews://disable_swipe，故右滑手势会让整个页面退出
			//问题2：安卓部分系统不触发touchend，不方便实现touchend时swipe.style.opacity = 1
			/*var coords = t.getBoundingClientRect();
			var maxWidth = article.offsetWidth;
			if(coords.width > maxWidth){
				var newTable = document.createElement('div'),
					swipe = document.createElement('div');
				swipe.className = 'swipe_tip';
				swipe.innerHTML = '左滑查看更多';
				newTable.className = 'horizontal_scroll';
				newTable.appendChild(t.cloneNode(true));
				newTable.appendChild(swipe);
				newTable.addEventListener('touchstart',function(){swipe.style.opacity = 0;},false);
				t.parentNode.replaceChild(newTable,t);
			}*/
		};
	}
};

/**
* 展示特殊信息（服务端information接口下发信息给客户端，实用价值模糊） —— 由客户端调用
* @param {object} obj 客户端接收到服务端information接口下发的context值 
*/
var infoInserted = false;
function insertDiv(obj){
	if(infoInserted) return;

	for(var k in obj){
		var con = document.querySelector("#"+k);
		if(con){
			var str = obj[k];
			if(str){
				con.innerHTML = str;
				con.style.display = "block";
			}
		}
	};
	infoInserted = true;
};


/****************************************************************
* 部分DOM操作基本封装，后续版本择机完成zepto改造后，废弃以下函数
*****************************************************************/

// add css class to element
function add_elt_class(elt, cls) {
	if (!elt || !cls)
		return false;
	try {
		var clazz = elt.getAttribute("class");
		if (clazz == null)
			clazz = "";
		var has = false;
		var clazzes = clazz.split(" ");
		for (var i = 0; i < clazzes.length; i++) {
			if (clazzes[i] == cls) {
				has = true;
				break;
			}
		}
		if (!has) {
			if (clazz.length > 0)
				clazz = clazz.concat(" ", cls);
			else
				clazz = cls;
			elt.setAttribute("class", clazz);
			return true;
		}
	} catch (e) {
	}
	return false;
}

// delete a css class of element
function del_elt_class(elt, cls) {
	if (!elt || !cls)
		return false;
	try {
		var clazz = elt.getAttribute("class");
		if (clazz == null)
			return false;
		var has = false;
		var clazzes = clazz.split(" ");
		clazz = ""
		for (var i = 0; i < clazzes.length; i++) {
			if (clazzes[i] == cls) {
				has = true;
				continue;
			} else {
				clazz = clazz.concat(" ", clazzes[i]);
			}
		}
		if (has) {
			elt.setAttribute("class", clazz);
			return true;
		}
	} catch (e) {
	}
	return false;
}

function has_elt_class(elt, cls) {
	if (!elt || !cls)
		return false;
	try {
		var clazz = elt.getAttribute("class");
		if (clazz == null)
			return false;
		var clazzes = clazz.split(" ");
		for (var i = 0; i < clazzes.length; i++) {
			if (clazzes[i] == cls) {
				return true;
			}
		}
	} catch (e) {
	}
	return false;
}

function has_elt_attr(elt, attr){
	if (!elt || !attr)
		return false;
	try{
		return elt.getAttribute(attr) !== null;
	}catch(ex){}

	return false;
}

/**
* 安卓客户端的一些注意事项：
* 1，据说某些安卓设备（如4.0.x, 4.1.x）在点击后立即进行大量运算，会有crash情形，
* 所以，本文中看似一些多余的超时调用大多基于这个原因而设。
* 2，安卓版本的图片加载逻辑依然晦涩难懂，待优化
*/