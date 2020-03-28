package com.taiko.noblenote;

import android.app.Application;

import com.taiko.noblenote.document.SFile;

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


        SFile.register(this);



    }

    public EventBus getEventBus()
    {
        return mEventBus;
    }

}
