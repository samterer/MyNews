package com.hzpd.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.color.tools.mytools.LogUtils;
import com.hzpd.hflt.R;
import com.hzpd.modle.NewsBean;
import com.hzpd.modle.ReplayBean;
import com.hzpd.ui.App;
import com.hzpd.ui.fragments.XF_NewsCommentsFragment;
import com.hzpd.ui.fragments.XF_NewsDetailFragment;

import java.io.File;


public class XF_NewsHtmlDetailActivity extends MBaseActivity {
	
	
	private XF_NewsDetailFragment newsdetailFm;
	private XF_NewsCommentsFragment commentsFm;
	
	
	//----------
	private String from;
	private NewsBean nb;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.xf_newshtmldetail_layout);
		
		getMyIntent();
		toNewsDetails();
		
	}
	
	
	private void getMyIntent(){
		Intent intent = getIntent();
		String action = intent.getAction();
		if (null != action && Intent.ACTION_VIEW.equals(action)) {
			Uri uri = intent.getData();
			if (uri != null) {
				nb = new NewsBean();
				String tid = uri.getPath();
				tid = tid.replace(File.separator, "");
				nb.setTid(tid);
				nb.setType("news");
				from = "browser";
			}
		} else {
			nb = (NewsBean) intent.getSerializableExtra("newbean");
			LogUtils.i("nit-->" + nb.getNid() + " type-->" + nb.getType()
					+ "  titleid-->" + nb.getTid() + " jsonurl-->"
					+ nb.getJson_url());
			try {
				from = intent.getStringExtra("from");
			} catch (Exception e) {
				from = null;
			}
		}

	}
	
	@Override
	public void onBackPressed() {
		if(currentFm instanceof XF_NewsCommentsFragment){
			fm.popBackStack();
			fm.beginTransaction().setCustomAnimations(R.anim.push_alpha_out,
					R.anim.push_left_out).show(newsdetailFm).commit();
			currentFm=newsdetailFm;
			return;
		}
		super.onBackPressed();
	}
	
	private void toNewsDetails(){
		
		newsdetailFm=new XF_NewsDetailFragment();
		Bundle args=new Bundle();
		args.putSerializable("nb", nb);
		args.putSerializable("from", from);
		newsdetailFm.setArguments(args);
		FragmentTransaction ft=fm.beginTransaction();
		ft.replace(R.id.xf_newshtmldetail_fm, newsdetailFm);
		ft.commit();
		currentFm=newsdetailFm;
		
	}
	
	public void toNewsComments(ReplayBean bean){
		
		commentsFm=new XF_NewsCommentsFragment();
		Bundle args=new Bundle();
		args.putSerializable("reply", bean);
		commentsFm.setArguments(args);
		FragmentTransaction ft=fm.beginTransaction();
		ft.setCustomAnimations(R.anim.push_left_in,R.anim.push_alpha_in,
				R.anim.push_alpha_out,R.anim.push_left_out);
		ft.add(R.id.xf_newshtmldetail_fm, commentsFm).hide(newsdetailFm);
		ft.addToBackStack(null);
		ft.commit();
		currentFm=commentsFm;
	}
	
	
	@Override
	public void finish() {
		LogUtils.i("App.isStartApp-->"+App.isStartApp+"  push-->"+from);
		if (!App.isStartApp && ("push".equals(from)||"browser".equals(from))) {
			Intent in = new Intent();
			in.setClass(this, WelcomeActivity.class);
			startActivity(in);
		}
		super.finish();
	}
	
}
