package com.example.listedecontactsv3

import android.app.AlertDialog
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
import kotlinx.coroutines.launch

class FavorisFragment :
    Fragment(R.layout.fragment_favoris) {

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
                R.id.rvFavoris
            )

        val tvVide =
            view.findViewById<TextView>(
                R.id.tvFavorisVide
            )

        val recherche =
            view.findViewById<EditText>(
                R.id.etRechercheFavoris
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

                AlertDialog.Builder(requireContext())
                    .setTitle("Supprimer")
                    .setMessage(
                        "Supprimer ${contact.prenom} ${contact.nom} ?"
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
            },

            onAppeler = { contact ->

                val intent =
                    android.content.Intent(
                        android.content.Intent.ACTION_DIAL,
                        android.net.Uri.parse(
                            "tel:${contact.telephone}"
                        )
                    )

                startActivity(intent)
            }
        )

        rv.layoutManager =
            LinearLayoutManager(requireContext())

        rv.adapter = adapter

        recherche.setText(
            vm.rechercheFavoris.value
        )

        recherche.doAfterTextChanged {

            vm.rechercherFavoris(
                it?.toString().orEmpty()
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                vm.favorisFiltres.collect { liste ->

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
}