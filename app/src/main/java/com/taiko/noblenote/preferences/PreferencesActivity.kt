package com.taiko.noblenote.preferences

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.taiko.noblenote.Pref
import com.taiko.noblenote.R
import com.taiko.noblenote.VolumeNotAccessibleDialog
import com.taiko.noblenote.document.VolumeUtil
import kotlinx.android.synthetic.main.toolbar.*


class PreferencesActivity : AppCompatActivity() {

    companion object
    {
        const val LAUNCH_SAF_FOLDER_PICKER = "show_saf_folder_picker"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preferences_activity)

        setSupportActionBar(toolbar)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, PreferenceFragment())
                .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBackPressed() {

        val dlg = VolumeNotAccessibleDialog.create(this);
        if(!VolumeUtil.fileOrContentUriAccessible(this, Pref.rootPath.value))
        {
            dlg.show();
        }
        else
        {
            super.onBackPressed()
        }
    }

}