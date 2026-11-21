package com.example.listedecontactsv3

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.listedecontactsv3.preferences.ParametresFragment
import com.example.listedecontactsv3.preferences.PreferencesManager
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

        val btnParametres =
            view.findViewById<ImageButton>(
                R.id.btnParametres
            )

        btnParametres?.setOnClickListener { anchor ->
            val popup = android.widget.PopupMenu(requireContext(), anchor)
            popup.menu.add(0, 1, 0, "Mes favoris")
            popup.menu.add(0, 2, 1, "Paramètres")
            popup.menu.add(0, 3, 2, "Nouveau contact")

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        parentFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragment_container, FavorisFragment())
                            .addToBackStack(null)
                            .commit()
                        true
                    }
                    2 -> {
                        if (parentFragmentManager.findFragmentById(R.id.fragment_container) !is ParametresFragment) {
                            parentFragmentManager
                                .beginTransaction()
                                .replace(R.id.fragment_container, ParametresFragment())
                                .addToBackStack(null)
                                .commit()
                        }
                        true
                    }
                    3 -> {
                        parentFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragment_container, DetailContactFragment.newInstanceForCreation())
                            .addToBackStack(null)
                            .commit()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        view.findViewById<View>(R.id.btnAjouterContact)?.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragment_container,
                    DetailContactFragment.newInstanceForCreation()
                )
                .addToBackStack(null)
                .commit()
        }

        adapter = ContactAdapter(
            onClick = { contact ->
                ouvrirDetail(contact.id)
            },

            onFavori = { contact ->
                vm.changerFavori(contact)
            },

            onModifier = { contact ->
                ouvrirDetail(contact.id)
            },

            onSupprimer = { contact ->
                confirmerSuppression(contact)
            },

            onAppeler = { contact ->
                val tel = contact.telephone.trim()
                if (tel.isBlank()) {
                    Toast.makeText(
                        requireContext(),
                        "Numéro de téléphone vide.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    try {
                        val intent = Intent(
                            Intent.ACTION_DIAL,
                            "tel:${android.net.Uri.encode(tel)}".toUri()
                        )
                        startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(
                            requireContext(),
                            "Aucune application téléphone disponible.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
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

    private fun ouvrirDetail(contactId: Long) {
        parentFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragment_container,
                DetailContactFragment.newInstance(contactId)
            )
            .addToBackStack(null)
            .commit()
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
                vm.delete(contact.id) { ok ->
                    if (!isAdded) return@delete
                    Toast.makeText(
                        requireContext(),
                        if (ok) "Contact supprimé" else "Contact introuvable",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }
}