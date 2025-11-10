package de.blogspot.noblenoteandroid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.blogspot.noblenoteandroid.R


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

    }

}
