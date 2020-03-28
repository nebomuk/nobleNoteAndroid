package com.taiko.noblenote.preferences

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.taiko.noblenote.Pref
import com.taiko.noblenote.R
import com.taiko.noblenote.VolumeNotAccessibleDialog
import com.taiko.noblenote.document.VolumeUtil
import rx.lang.kotlin.plusAssign


class PreferencesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preferences_activity)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, PreferenceFragment())
                .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBackPressed() {

        val dlg = VolumeNotAccessibleDialog.create(this);
        if(!VolumeUtil.volumeAccessible(this, Pref.rootPath.value))
        {
            dlg.show();
        }
        else
        {
            super.onBackPressed()
        }
    }

}