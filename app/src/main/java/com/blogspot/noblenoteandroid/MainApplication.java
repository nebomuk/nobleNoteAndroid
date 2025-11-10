package com.blogspot.noblenoteandroid;

import android.app.Application;

import com.blogspot.noblenoteandroid.filesystem.SFile;
import com.blogspot.noblenoteandroid.util.EventBus;

import rx_activity_result.RxActivityResult;



public class MainApplication extends Application
{

    private EventBus mEventBus;

    @Override
    public void onCreate() {

        System.setProperty("rx.ring-buffer.size", "1024");

        super.onCreate();
        RxActivityResult.register(this);
        mEventBus = new EventBus();


        SFile.register(this, Pref.INSTANCE.getRootPath());



    }

    public EventBus getEventBus()
    {
        return mEventBus;
    }

}
