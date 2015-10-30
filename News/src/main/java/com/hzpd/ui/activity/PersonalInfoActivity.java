package com.hzpd.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.hzpd.hflt.R;
import com.hzpd.ui.fragments.ZQ_FindbackpwdFragment;
import com.hzpd.ui.fragments.ZQ_ModifyPersonalInfoFragment;
import com.hzpd.ui.fragments.ZQ_PersonalInfoFragment;
import com.hzpd.url.InterfaceJsonfile;
import com.hzpd.url.InterfaceJsonfile_TW;
import com.hzpd.url.InterfaceJsonfile_YN;
import com.hzpd.utils.CODE;
import com.hzpd.utils.SharePreferecesUtils;
import com.hzpd.utils.StationConfig;
import com.lidroid.xutils.util.LogUtils;

public class PersonalInfoActivity extends MBaseActivity {

	private ZQ_ModifyPersonalInfoFragment modifyPersonalFm;
	private ZQ_PersonalInfoFragment personalInfoFm;
	private ZQ_FindbackpwdFragment findbackpwdFm;

	private Fragment currentFm;
	private FragmentManager fm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zq_personalinfo_layout);

		fm = getSupportFragmentManager();

		personalInfoFm = new ZQ_PersonalInfoFragment();

//		FragmentTransaction ft = fm.beginTransaction();
//		ft.replace(R.id.zq_pinfo_fm, personalInfoFm);
//		ft.commit();


		fm.beginTransaction()
				.replace(R.id.zq_pinfo_fm, personalInfoFm)
				.commit();

		currentFm = personalInfoFm;
	}

	public void toModifyPinfoFm(int type) {
		String station= SharePreferecesUtils.getParam(PersonalInfoActivity.this, StationConfig.STATION, "def").toString();
		String PWDTYPE=null;
		if (station.equals(StationConfig.DEF)){
			PWDTYPE=InterfaceJsonfile.PWDTYPE;
		}else if (station.equals(StationConfig.YN)){
			PWDTYPE= InterfaceJsonfile_YN.PWDTYPE;
		}else if (station.equals(StationConfig.TW)){
			PWDTYPE= InterfaceJsonfile_TW.PWDTYPE;
		}
		FragmentTransaction ft = fm.beginTransaction();
		ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
				, android.R.anim.slide_in_left, android.R.anim.slide_out_right);

		modifyPersonalFm = new ZQ_ModifyPersonalInfoFragment();
		Bundle args = new Bundle();
		args.putInt(PWDTYPE, type);
		modifyPersonalFm.setArguments(args);
		ft.add(R.id.zq_pinfo_fm, modifyPersonalFm);
		ft.addToBackStack(null);
		ft.commit();
		currentFm = modifyPersonalFm;

	}

	public void toFindbackpwdFm() {
		String station= SharePreferecesUtils.getParam(PersonalInfoActivity.this, StationConfig.STATION, "def").toString();
		String PWDTYPE=null;
		if (station.equals(StationConfig.DEF)){
			PWDTYPE=InterfaceJsonfile.PWDTYPE;
		}else if (station.equals(StationConfig.YN)){
			PWDTYPE= InterfaceJsonfile_YN.PWDTYPE;
		}else if (station.equals(StationConfig.TW)){
			PWDTYPE= InterfaceJsonfile_TW.PWDTYPE;
		}
		FragmentTransaction ft = fm.beginTransaction();
		ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
				, android.R.anim.slide_in_left, android.R.anim.slide_out_right);

		findbackpwdFm = new ZQ_FindbackpwdFragment();
		Bundle args = new Bundle();
		args.putInt(PWDTYPE, 2);
		findbackpwdFm.setArguments(args);
		ft.add(R.id.zq_pinfo_fm, findbackpwdFm);
		ft.addToBackStack(null);
		ft.commit();
		currentFm = findbackpwdFm;
	}

	public void goBackPinfoFm() {
		fm.popBackStack();
		currentFm = personalInfoFm;
		FragmentTransaction ft = fm.beginTransaction();
		ft.show(currentFm);
		ft.commit();
	}

	@Override
	public void onBackPressed() {
		if (currentFm instanceof ZQ_PersonalInfoFragment) {
			finish();
		} else if (currentFm instanceof ZQ_ModifyPersonalInfoFragment) {
			goBackPinfoFm();
			personalInfoFm.modify();
		} else if (currentFm instanceof ZQ_FindbackpwdFragment) {
			goBackPinfoFm();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		LogUtils.i("requestCode-->" + requestCode + "  resultCode-->" + resultCode);
		switch (requestCode) {
			case CODE.IMAGE_REQUEST_CODE: {
				if (null != data) {
					personalInfoFm.startPhotoZoom(data.getData());
				}
			}
			break;
			case CODE.CAMERA_REQUEST_CODE: {
				personalInfoFm.startPhotoZoom();
			}
			break;
			case CODE.RESULT_REQUEST_CODE: {
				if (data != null) {
					Bundle extras = data.getExtras();
					if (extras != null) {
						Bitmap photo = extras.getParcelable("data");
						personalInfoFm.uploadPhoto(photo);
						LogUtils.i("setphoto");
					} else {
						LogUtils.i("extras null");
					}
				}
			}
			break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

}
