package com.chidi.seamfixchat

import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.preference.RingtonePreference
import android.text.TextUtils
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*

class SettingsActivity : AppCompatActivity() {

    companion object {

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener =
            Preference.OnPreferenceChangeListener { preference, value ->

                val stringValue = value.toString()

                if (preference is ListPreference) {
                    // For list preferences, look up the correct display value in
                    // the preference's 'entries' list.
                    val listPreference = preference
                    val index = listPreference.findIndexOfValue(stringValue)

                    // Set the summary to reflect the new value.
                    preference.setSummary(
                        if (index >= 0)
                            listPreference.entries[index]
                        else
                            null
                    )

                } else if (preference is RingtonePreference) {
                    // For ringtone preferences, look up the correct display value
                    // using RingtoneManager.
                    if (TextUtils.isEmpty(stringValue)) {
                        // Empty values correspond to 'silent' (no ringtone).
                        preference.setSummary("Silent")

                    } else {
                        val ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue)
                        )

                        if (ringtone == null) {
                            // Clear the summary if there was a lookup error.
                            preference.setSummary(null)
                        } else {
                            // Set the summary to reflect the new ringtone display
                            // name.
                            val name = ringtone.getTitle(preference.getContext())
                            preference.setSummary(name)
                        }
                    }
                } else {
                    // For all other preferences, set the summary to the value's
                    // simple string representation.
                    preference.summary = stringValue
                }
                true
            }

        private fun bindPreferenceSummaryToValue(preference: Preference) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager
                    .getDefaultSharedPreferences(preference.context)
                    .getString(preference.key, "")
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            bindPreferenceSummaryToValue(findPreference("list_duration")!!)
        }
    }
}