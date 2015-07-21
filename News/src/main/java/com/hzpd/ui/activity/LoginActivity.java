package com.hzpd.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.hzpd.hflt.R;
import com.hzpd.ui.fragments.ZQ_FindbackpwdFragment;
import com.hzpd.ui.fragments.ZQ_LoginFragment;
import com.hzpd.ui.fragments.ZQ_LoginWelcomeFragment;
import com.hzpd.ui.fragments.ZQ_RegisterFragment;
import com.hzpd.url.InterfaceJsonfile;

public class LoginActivity extends MBaseActivity {

	private ZQ_LoginWelcomeFragment loginwelfm;
	private ZQ_LoginFragment loginfm;
	private ZQ_RegisterFragment registerfm;
	private ZQ_FindbackpwdFragment findbackpwdfm;

	private FragmentManager fm;
	private Fragment currentFm;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zq_loginwel_layout);
		fm = getSupportFragmentManager();

		loginwelfm = new ZQ_LoginWelcomeFragment();

		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.zq_loginwel_fm, loginwelfm);
		ft.commit();
		currentFm = loginwelfm;
	}


	public void toLoginFm() {

		FragmentTransaction ft = fm.beginTransaction();
		ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
				, android.R.anim.slide_in_left, android.R.anim.slide_out_right);

		loginfm = new ZQ_LoginFragment();
		ft.add(R.id.zq_loginwel_fm, loginfm);
//		.hide(currentFm);
		ft.addToBackStack(null);
		ft.commit();
		currentFm = loginfm;
	}

	public void toRegisterFm() {
		FragmentTransaction ft = fm.beginTransaction();
		ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
				, android.R.anim.slide_in_left, android.R.anim.slide_out_right);

		registerfm = new ZQ_RegisterFragment();
		ft.add(R.id.zq_loginwel_fm, registerfm);
//		.hide(currentFm);
		ft.addToBackStack(null);
		ft.commit();
		currentFm = registerfm;
	}

	public void toFindbackpwdFm() {
		FragmentTransaction ft = fm.beginTransaction();
		ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
				, android.R.anim.slide_in_left, android.R.anim.slide_out_right);

		Bundle args = new Bundle();
		args.putInt(InterfaceJsonfile.PWDTYPE, 1);
		findbackpwdfm = new ZQ_FindbackpwdFragment();
		findbackpwdfm.setArguments(args);
		ft.add(R.id.zq_loginwel_fm, findbackpwdfm);
//		.hide(currentFm);
		ft.addToBackStack(null);
		ft.commit();
		currentFm = findbackpwdfm;
	}

	public void gobackLoginFm() {
		fm.popBackStack();
		FragmentTransaction ft = fm.beginTransaction();
		ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
				, android.R.anim.slide_in_left, android.R.anim.slide_out_right);

		ft.show(loginfm);
		ft.commit();
		currentFm = loginfm;
	}


	public void gobackLoginwelFm() {
		fm.popBackStack();
		FragmentTransaction ft = fm.beginTransaction();
		ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
				, android.R.anim.slide_in_left, android.R.anim.slide_out_right);

		ft.show(loginwelfm);
		ft.commit();
		currentFm = loginwelfm;
	}

	@Override
	public void onBackPressed() {
		if (currentFm instanceof ZQ_LoginWelcomeFragment) {
			finish();
		} else if (currentFm instanceof ZQ_LoginFragment) {
			gobackLoginwelFm();
		} else if (currentFm instanceof ZQ_RegisterFragment) {
			gobackLoginFm();
		} else if (currentFm instanceof ZQ_FindbackpwdFragment) {
			gobackLoginFm();
		}
	}


}
