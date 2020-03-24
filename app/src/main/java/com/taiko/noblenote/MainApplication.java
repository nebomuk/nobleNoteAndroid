package com.taiko.noblenote;

import android.app.Application;

import com.taiko.noblenote.document.SFile;

import leakcanary.AppWatcher;
import leakcanary.LeakCanary;
import rx_activity_result.RxActivityResult;



public class MainApplication extends Application
{

    private EventBus mEventBus;

    @Override
    public void onCreate() {

        System.setProperty("rx.ring-buffer.size", "1024");

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
