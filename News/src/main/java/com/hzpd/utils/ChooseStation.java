package com.hzpd.utils;

import android.content.Context;

/**
 * Created by taoshuang on 2015/8/21.
 */
public class ChooseStation {

    private Context context;
    private String station;

    public ChooseStation() {

    }

    public ChooseStation(Context context, String station) {
        this.context = context;
        this.station = station;
        String str = SharePreferecesUtils.getParam(context, station, StationConfig.DEF).toString();
        if (str.equals(StationConfig.DEF)) {

        }else if (str.equals(StationConfig.YN)) {

        }else if (str.equals(StationConfig.TW)) {

        }
    }

}
