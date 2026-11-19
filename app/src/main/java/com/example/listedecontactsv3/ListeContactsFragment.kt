package com.example.listedecontactsv3

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.listedecontactsv3.preferences.PreferencesManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.launch

class ListeContactsFragment :
    Fragment(R.layout.fragment_liste_contacts) {

    private val vm: ContactViewModel by activityViewModels()

    private lateinit var adapter: ContactAdapter

    override fun onResume() {
        super.onResume()
        adapter.appliquerPreferences(
            PreferencesManager.taillePolice(requireContext()),
            PreferencesManager.couleurTexte(requireContext()),
            PreferencesManager.appelDepuisListe(requireContext())
        )
        vm.chargerContacts()
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val rv =
            view.findViewById<RecyclerView>(
                R.id.rvContacts
            )

        val tvVide =
            view.findViewById<TextView>(
                R.id.tvVide
            )

        val etRecherche =
            view.findViewById<EditText>(
                R.id.etRecherche
            )

        val btnAjouter =
            view.findViewById<ExtendedFloatingActionButton>(
                R.id.btnAjouter
            )

        adapter = ContactAdapter(

            onClick = { contact ->
                parentFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        DetailContactFragment
                            .newInstance(contact.id)
                    )
                    .addToBackStack(null)
                    .commit()
            },

            onFavori = { contact ->
                vm.changerFavori(contact)
            },

            onSupprimer = { contact ->
                confirmerSuppression(contact)
            },

            onAppeler = { contact ->
                val intent = Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:${contact.telephone}")
                )

                startActivity(intent)
            }
        )

        rv.layoutManager =
            LinearLayoutManager(requireContext())

        rv.adapter = adapter

        etRecherche.setText(
            vm.recherche.value
        )

        etRecherche.doAfterTextChanged {
            vm.rechercher(
                it?.toString().orEmpty()
            )
        }

        btnAjouter.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragment_container,
                    DetailContactFragment
                        .newInstanceForCreation()
                )
                .addToBackStack(null)
                .commit()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                vm.contactsFiltres.collect { liste ->

                    adapter.submitList(liste)

                    tvVide.visibility =
                        if (liste.isEmpty()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                }
            }
        }
    }

    private fun confirmerSuppression(
        contact: Contact
    ) {

        AlertDialog.Builder(requireContext())
            .setTitle("Supprimer le contact")
            .setMessage(
                "Voulez-vous supprimer ${contact.prenom} ${contact.nom} ?"
            )
            .setNegativeButton(
                "Annuler",
                null
            )
            .setPositiveButton(
                "Supprimer"
            ) { _, _ ->

                vm.delete(contact.id)

                Toast.makeText(
                    requireContext(),
                    "Contact supprimé",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }
}