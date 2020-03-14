package com.taiko.noblenote;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;
import com.taiko.noblenote.document.SFile;

import rx_activity_result.RxActivityResult;



public class MainApplication extends Application
{

    private EventBus mEventBus;

    @Override
    public void onCreate() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }

        System.setProperty("rx.ring-buffer.size", "1024");

        //LeakCanary.install(this);
        // the default LeakDirectoryProvider writes to Downloads, must manually delete the created folder

        super.onCreate();
        RxActivityResult.register(this);
        mEventBus = new EventBus();


        SFile.register(this);



    }

    public EventBus getEventBus()
    {
        return mEventBus;
    }

}
