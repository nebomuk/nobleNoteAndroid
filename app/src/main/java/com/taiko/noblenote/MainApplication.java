package com.taiko.noblenote;

import android.app.Application;

import rx_activity_result.RxActivityResult;

/**
 * Created by Taiko on 22.09.2016.
 */

public class MainApplication extends Application
{

    private UICommunicator mUICommunicator;

    @Override
    public void onCreate()
    {
        super.onCreate();
        RxActivityResult.register(this);
        mUICommunicator = new UICommunicator();
    }


    public UICommunicator getUICommunicator()
    {
        return mUICommunicator;
    }
}
