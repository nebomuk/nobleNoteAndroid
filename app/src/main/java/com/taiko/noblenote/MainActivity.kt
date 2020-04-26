package com.taiko.noblenote

import android.os.Bundle
import android.transition.Fade
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity()
{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // nav_host fragments seems to ignore these orientation changes
        // and uses the same nav_graph that is used at the start of the app
        val isTwoPane = resources.getBoolean(R.bool.isTwoPane)

        if(isTwoPane)
        {
            setContentView(R.layout.activity_main_twopane);

        }
        else
        {
            setContentView(R.layout.activity_main);
        }

        // https://stackoverflow.com/questions/26600263/how-do-i-prevent-the-status-bar-and-navigation-bar-from-animating-during-an-acti
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP) {
            val fade = Fade()
            fade.excludeTarget(android.R.id.statusBarBackground, true)
            fade.excludeTarget(android.R.id.navigationBarBackground, true)
            window.exitTransition = fade
            window.enterTransition = fade
        }


    }



}
