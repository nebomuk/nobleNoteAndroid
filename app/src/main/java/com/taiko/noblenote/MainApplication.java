package com.taiko.noblenote;

import android.app.Application;

import rx_activity_result.RxActivityResult;



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
