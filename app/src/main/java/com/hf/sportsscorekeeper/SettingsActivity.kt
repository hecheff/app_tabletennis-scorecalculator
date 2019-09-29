package com.hf.sportsscorekeeper

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {

    var initialScoreModeIs21 = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settings= PreferenceManager.getDefaultSharedPreferences(this)
        initialScoreModeIs21    = settings.getBoolean("scoreMode", true)

        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // Code for keeping state as-is when changing activity
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun getParentActivityIntent(): Intent? {
        return super.getParentActivityIntent()!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // If the setting has changed, reset
        val settings= PreferenceManager.getDefaultSharedPreferences(this)
        if(initialScoreModeIs21 != settings.getBoolean("scoreMode", true)) {
            finish()

            val intent = Intent(this, MainActivity::class.java).apply { }
            startActivity(intent)
        }
    }
}