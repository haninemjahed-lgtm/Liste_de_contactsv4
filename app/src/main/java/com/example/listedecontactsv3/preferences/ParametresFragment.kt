package com.example.listedecontactsv3.preferences

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.listedecontactsv3.ContactViewModel
import com.example.listedecontactsv3.R

class ParametresFragment : PreferenceFragmentCompat() {

    private val vm: ContactViewModel by activityViewModels()

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        setPreferencesFromResource(
            R.xml.preferences,
            rootKey
        )

        findPreference<Preference>("benchmark_100_contacts")?.setOnPreferenceClickListener { pref ->
            pref.isEnabled = false
            vm.executerBenchmark100Contacts { sansTx, avecTx ->
                if (!isAdded) return@executerBenchmark100Contacts
                pref.isEnabled = true
                AlertDialog.Builder(requireContext())
                    .setTitle("Benchmark 100 contacts")
                    .setMessage(
                        "Sans transaction : $sansTx ms\n" +
                                "Avec transaction unique : $avecTx ms\n\n" +
                                "200 contacts de test ont été ajoutés à la base."
                    )
                    .setPositiveButton("OK", null)
                    .show()
            }
            true
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<View>(R.id.bottomBarCard)?.visibility = View.VISIBLE
        val density = resources.displayMetrics.density
        val topPadding = (32 * density).toInt()
        val bottomPadding = (24 * density).toInt()
        listView.setBackgroundResource(R.drawable.bg_bottom_area)
        listView.clipToPadding = false
        listView.setPadding(0, topPadding, 0, bottomPadding)
    }
}