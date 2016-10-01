package com.taiko.noblenote;

import android.app.Application;

import rx_activity_result.RxActivityResult;

/**
 * Created by fabdeuch on 22.09.2016.
 */

public class MainApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        RxActivityResult.register(this);

    }
}
