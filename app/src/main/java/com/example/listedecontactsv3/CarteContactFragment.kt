package com.example.listedecontactsv3

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class CarteContactFragment :
    Fragment(R.layout.fragment_carte_contact) {

    companion object {

        private const val ARG_ID = "contact_id"

        fun newInstance(id: Long): CarteContactFragment {

            return CarteContactFragment().apply {

                arguments = Bundle().apply {
                    putLong(ARG_ID, id)
                }
            }
        }
    }

    private val vm: ContactViewModel by activityViewModels()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        val btnRetour =
            view.findViewById<Button>(
                R.id.btnRetour
            )

        val tvMessage =
            view.findViewById<TextView>(
                R.id.tvMessage
            )

        val mapContainer =
            view.findViewById<View>(
                R.id.map
            )

        btnRetour.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val contactId =
            requireArguments().getLong(ARG_ID)

        vm.getByIdAsync(contactId) { contact ->

            if (!isAdded) {
                return@getByIdAsync
            }

            val lat = contact?.latitude
            val lng = contact?.longitude

            if (
                contact == null ||
                lat == null ||
                lng == null ||
                lat !in -90.0..90.0 ||
                lng !in -180.0..180.0
            ) {

                tvMessage.visibility =
                    View.VISIBLE

                mapContainer.visibility =
                    View.GONE

                tvMessage.text =
                    "Aucune position GPS disponible pour ce contact."

                return@getByIdAsync
            }

            tvMessage.visibility =
                View.GONE

            mapContainer.visibility =
                View.VISIBLE

            val mapFragment =
                childFragmentManager
                    .findFragmentById(
                        R.id.map
                    ) as? SupportMapFragment

            if (mapFragment == null) {

                tvMessage.visibility =
                    View.VISIBLE

                tvMessage.text =
                    "Impossible de charger la carte."

                return@getByIdAsync
            }

            mapFragment.getMapAsync { googleMap ->

                val position =
                    LatLng(lat, lng)

                googleMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(
                            "${contact.prenom} ${contact.nom}"
                        )
                )

                googleMap.moveCamera(
                    CameraUpdateFactory
                        .newLatLngZoom(
                            position,
                            14f
                        )
                )
            }
        }
    }
}