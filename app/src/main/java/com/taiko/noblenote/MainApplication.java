package com.taiko.noblenote;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.squareup.leakcanary.LeakCanary;

import rx_activity_result.RxActivityResult;



public class MainApplication extends Application implements Application.ActivityLifecycleCallbacks
{

    private EventBus mEventBus;

    @Override
    public void onCreate()
    {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        super.onCreate();
        RxActivityResult.register(this);
        mEventBus = new EventBus();


    }



    public EventBus getEventBus()
    {
        return mEventBus;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle)
    {

    }

    @Override
    public void onActivityStarted(Activity activity)
    {

    }

    @Override
    public void onActivityResumed(Activity activity)
    {

    }

    @Override
    public void onActivityPaused(Activity activity)
    {

    }

    @Override
    public void onActivityStopped(Activity activity)
    {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle)
    {

    }

    @Override
    public void onActivityDestroyed(Activity activity)
    {

    }
}
