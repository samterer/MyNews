package com.hzpd.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.hzpd.ui.App;
import com.lidroid.xutils.util.LogUtils;

import de.greenrobot.event.EventBus;

public class LocationService extends Service {

	public LocationClient mLocationClient;
	public MyLocationListener mMyLocationListener;


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mLocationClient = new LocationClient(this.getApplicationContext());
		mMyLocationListener = new MyLocationListener();
		mLocationClient.setDebug(App.debug);
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);//设置定位模式
		option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(2000);//设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true);//返回的定位结果包含地址信息
		option.setTimeOut(10000);
		option.setOpenGps(true);
		mLocationClient.setLocOption(option);
		mLocationClient.registerLocationListener(mMyLocationListener);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (null == intent) {
			stopSelf();
		} else {

			stopSelf();
		}
		return super.onStartCommand(intent, flags, startId);
	}


	private class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				LogUtils.i("BDLocation null");
				return;
			}

			if (61 == location.getLocType() || 65 == location.getLocType()
					|| 161 == location.getLocType()) {
				String address = location.getAddrStr();
				if (!TextUtils.isEmpty(address)) {
					EventBus.getDefault().post(address);
					stopSelf();
				}
			}

			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime());
			sb.append("\nerror code : ");
			sb.append(location.getLocType());
			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(location.getLongitude());
			sb.append("\nradius : ");
			sb.append(location.getRadius());
			if (location.getLocType() == BDLocation.TypeGpsLocation) {
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());
				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
			}

			LogUtils.i("address-->" + sb.toString());
		}
	}

	@Override
	public void onDestroy() {
		LogUtils.i("LocationService onDestroy");
		mLocationClient.stop();
		super.onDestroy();
	}

}
