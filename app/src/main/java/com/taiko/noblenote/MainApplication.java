package com.taiko.noblenote;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import rx_activity_result.RxActivityResult;



public class MainApplication extends Application implements Application.ActivityLifecycleCallbacks
{

    private EventBus mEventBus;

    @Override
    public void onCreate()
    {
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
