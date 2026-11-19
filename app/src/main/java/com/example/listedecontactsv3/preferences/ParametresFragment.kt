package com.example.listedecontactsv3.preferences

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.listedecontactsv3.R

class ParametresFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        setPreferencesFromResource(
            R.xml.preferences,
            rootKey
        )
    }
}