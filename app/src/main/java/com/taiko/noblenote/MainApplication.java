package com.taiko.noblenote;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import kotlin.jvm.JvmStatic;
import rx_activity_result.RxActivityResult;



public class MainApplication extends Application
{

    private EventBus mEventBus;

    private static MainApplication mInstance;

    public static MainApplication getInstance()
    {
        return mInstance;
    }


    @Override
    public void onCreate()
    {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        //LeakCanary.install(this);
        // the default LeakDirectoryProvider writes to Downloads, must manually delete the created folder

        mInstance = this;

        super.onCreate();
        RxActivityResult.register(this);
        mEventBus = new EventBus();


    }



    public EventBus getEventBus()
    {
        return mEventBus;
    }

}
