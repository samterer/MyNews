package com.hzpd.ui.fragments.welcome;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzpd.hflt.R;
import com.hzpd.ui.App;
import com.hzpd.ui.activity.WelcomeActivity;
import com.hzpd.ui.fragments.BaseFragment;
import com.hzpd.utils.Log;
import com.hzpd.utils.SPUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.nineoldandroids.animation.AnimatorSet;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;

import java.io.File;

public class AdFlashFragment extends BaseFragment {

    @ViewInject(R.id.adflash_img_ad)
    private ImageView adflash_img_ad;
    @ViewInject(R.id.adflash_img_logo)
    private ImageView adflash_img_logo;
    @ViewInject(R.id.adflash_tv_slogan)
    private TextView adflash_tv_slogan;

    private AnimatorSet animSetFadeout;
    private AnimatorSet animSetFadein;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.adflash_frag_layout, container,
                false);
        ViewUtils.inject(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isAdd = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("test", "  onResume ");
        adflash_img_ad.postDelayed(runnable, 2000);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isAdd = false;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        isAdd = true;
    }

    boolean isAdd = false;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.e("test", "  AdFlashFragment " +  isAdd);
            Bitmap bm = null;
            if (!isAdd) {
                return;
            }
            if (null != App.getInstance().welcomeAdbean) {
                final String url = App.getInstance().welcomeAdbean.getImgurl();
                if (!TextUtils.isEmpty(url)) {

                    try {
                        File fBitmap = DiskCacheUtils.findInCache(url.replaceAll("&amp;", "&"), mImageLoader.getDiskCache());
                        if (null != fBitmap) {
                            bm = BitmapFactory.decodeFile(fBitmap
                                    .getAbsolutePath());
                        } else {
                            String uri = url;
                            if (SPUtil.isImageUri(uri)) {
                                mImageLoader.loadImage(uri, null);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!isAdd) {
                return;
            }
            try {
                if (null != bm) {
                    adflash_img_ad.setImageBitmap(bm);
                    adflash_img_ad.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (isAdd) {
                                    ((WelcomeActivity) getActivity()).loadMainUI();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 2000);
                } else {
                    try {
                        if (isAdd) {
                            adflash_img_ad.setImageResource(R.drawable.welcome_1);
                            Log.e("test", "  loadMainUI ");
                            ((WelcomeActivity) getActivity()).loadMainUI();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    @OnClick(R.id.adflash_img_ad)
    private void adJump(View view) {
        String url = null;
        if (null != App.getInstance().welcomeAdbean) {
            url = App.getInstance().welcomeAdbean.getLink();
        }
        if (!TextUtils.isEmpty(url)) {
            ((WelcomeActivity) getActivity()).jump(url);
        }
    }

    @Override
    public void onDestroy() {
        if (null != animSetFadeout) {
            animSetFadeout.removeAllListeners();
        }
        if (null != animSetFadein) {
            animSetFadein.removeAllListeners();
        }
        super.onDestroy();
    }

}
