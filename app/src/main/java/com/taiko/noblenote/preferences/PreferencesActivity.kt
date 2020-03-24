package com.taiko.noblenote.preferences

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.taiko.noblenote.R


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

}