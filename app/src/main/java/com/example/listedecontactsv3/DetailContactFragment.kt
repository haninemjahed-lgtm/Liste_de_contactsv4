package com.example.listedecontactsv3

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import java.io.File
import java.io.FileOutputStream

class DetailContactFragment :
    Fragment(R.layout.fragment_detail_contact) {

    private val vm: ContactViewModel by activityViewModels()

    private var contactId: Long = 0L
    private var photoUri: String? = null

    private lateinit var ivPhoto: ImageView
    private lateinit var etNom: EditText
    private lateinit var etPrenom: EditText
    private lateinit var etTelephone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText

    private val choisirPhoto =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                photoUri = uri.toString()
                ivPhoto.setImageURI(uri)
            }
        }

    private val prendrePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                photoUri = sauvegarderBitmap(bitmap)
                ivPhoto.setImageBitmap(bitmap)
            }
        }

    companion object {

        private const val ARG_ID = "contact_id"

        fun newInstance(id: Long): DetailContactFragment {
            return DetailContactFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_ID, id)
                }
            }
        }

        fun newInstanceForCreation(): DetailContactFragment {
            return DetailContactFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_ID, 0L)
                }
            }
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        contactId = requireArguments().getLong(ARG_ID, 0L)

        ivPhoto = view.findViewById(R.id.ivPhoto)
        etNom = view.findViewById(R.id.etNom)
        etPrenom = view.findViewById(R.id.etPrenom)
        etTelephone = view.findViewById(R.id.etTelephone)
        etEmail = view.findViewById(R.id.etEmail)
        etLatitude = view.findViewById(R.id.etLatitude)
        etLongitude = view.findViewById(R.id.etLongitude)

        val btnGalerie = view.findViewById<Button>(R.id.btnGalerie)
        val btnCamera = view.findViewById<Button>(R.id.btnCamera)
        val btnAppeler = view.findViewById<Button>(R.id.btnAppeler)
        val btnEmail = view.findViewById<Button>(R.id.btnEmail)
        val btnCarte = view.findViewById<Button>(R.id.btnCarte)
        val btnEnregistrer = view.findViewById<Button>(R.id.btnEnregistrer)
        val btnAnnuler = view.findViewById<Button>(R.id.btnAnnuler)
        val btnSupprimer = view.findViewById<Button>(R.id.btnSupprimer)

        // MODE CREATION
        if (contactId == 0L) {

            btnSupprimer.visibility = View.GONE

        } else {

            // MODE MODIFICATION
            btnSupprimer.visibility = View.VISIBLE

            vm.getByIdAsync(contactId) { contact ->

                if (!isAdded || contact == null) {
                    return@getByIdAsync
                }

                etNom.setText(contact.nom)
                etPrenom.setText(contact.prenom)
                etTelephone.setText(contact.telephone)
                etEmail.setText(contact.email ?: "")

                etLatitude.setText(
                    contact.latitude?.toString() ?: ""
                )

                etLongitude.setText(
                    contact.longitude?.toString() ?: ""
                )

                photoUri = contact.photoUri

                if (!contact.photoUri.isNullOrEmpty()) {
                    try {
                        ivPhoto.setImageURI(
                            contact.photoUri.toUri()
                        )
                    } catch (_: Exception) {
                    }
                }
            }
        }

        // PHOTO GALERIE
        btnGalerie.setOnClickListener {
            choisirPhoto.launch("image/*")
        }

        // PHOTO CAMERA
        btnCamera.setOnClickListener {
            prendrePhoto.launch(null)
        }

        // APPEL
        btnAppeler.setOnClickListener {
            appeler()
        }

        // EMAIL
        btnEmail.setOnClickListener {
            envoyerEmail()
        }

        // CARTE
        btnCarte.setOnClickListener {

            if (contactId == 0L) {

                Toast.makeText(
                    requireContext(),
                    "Enregistrez d'abord le contact.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragment_container,
                    CarteContactFragment.newInstance(contactId)
                )
                .addToBackStack(null)
                .commit()
        }

        // ENREGISTRER
        btnEnregistrer.setOnClickListener {
            enregistrer()
        }

        // ANNULER
        btnAnnuler.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // SUPPRIMER
        btnSupprimer.setOnClickListener {
            confirmerSuppression()
        }
    }

    private fun enregistrer() {

        val nom = etNom.text.toString().trim()
        val prenom = etPrenom.text.toString().trim()
        val telephone = etTelephone.text.toString().trim()
        val email = etEmail.text.toString().trim()

        // VALIDATION NOM
        if (nom.isEmpty()) {

            etNom.error = "Nom obligatoire"
            etNom.requestFocus()

            return
        }

        // VALIDATION PRENOM
        if (prenom.isEmpty()) {

            etPrenom.error = "Prénom obligatoire"
            etPrenom.requestFocus()

            return
        }

        // VALIDATION TELEPHONE
        if (telephone.isEmpty()) {

            etTelephone.error = "Téléphone obligatoire"
            etTelephone.requestFocus()

            return
        }

        // GPS
        val latitudeText =
            etLatitude.text.toString().trim()

        val longitudeText =
            etLongitude.text.toString().trim()

        val latitude =
            if (latitudeText.isEmpty()) {
                null
            } else {
                latitudeText.toDoubleOrNull()
            }

        val longitude =
            if (longitudeText.isEmpty()) {
                null
            } else {
                longitudeText.toDoubleOrNull()
            }

        // Latitude invalide
        if (
            latitudeText.isNotEmpty() &&
            latitude == null
        ) {

            etLatitude.error = "Latitude invalide"
            etLatitude.requestFocus()

            return
        }

        // Longitude invalide
        if (
            longitudeText.isNotEmpty() &&
            longitude == null
        ) {

            etLongitude.error = "Longitude invalide"
            etLongitude.requestFocus()

            return
        }

        // Une seule coordonnée
        if (
            (latitude == null) !=
            (longitude == null)
        ) {

            Toast.makeText(
                requireContext(),
                "Veuillez saisir latitude et longitude ensemble.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        // Vérification latitude
        if (
            latitude != null &&
            latitude !in -90.0..90.0
        ) {

            etLatitude.error =
                "Latitude entre -90 et 90"

            return
        }

        // Vérification longitude
        if (
            longitude != null &&
            longitude !in -180.0..180.0
        ) {

            etLongitude.error =
                "Longitude entre -180 et 180"

            return
        }

        val contact = Contact(

            id = contactId,

            nom = nom,

            prenom = prenom,

            telephone = telephone,

            email =
                email.ifEmpty {
                    null
                },

            photoUri = photoUri,

            latitude = latitude,

            longitude = longitude
        )

        // AJOUT
        if (contactId == 0L) {

            vm.insert(contact)

            Toast.makeText(
                requireContext(),
                "Contact ajouté avec succès",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            // MODIFICATION
            vm.update(contact)

            Toast.makeText(
                requireContext(),
                "Contact modifié avec succès",
                Toast.LENGTH_SHORT
            ).show()
        }

        parentFragmentManager.popBackStack()
    }

    private fun confirmerSuppression() {

        AlertDialog.Builder(requireContext())

            .setTitle("Supprimer le contact")

            .setMessage(
                "Voulez-vous vraiment supprimer ce contact ?"
            )

            .setPositiveButton("Supprimer") { _, _ ->

                vm.delete(contactId)

                Toast.makeText(
                    requireContext(),
                    "Contact supprimé",
                    Toast.LENGTH_SHORT
                ).show()

                parentFragmentManager.popBackStack()
            }

            .setNegativeButton(
                "Annuler",
                null
            )

            .show()
    }

    private fun appeler() {

        val numero =
            etTelephone.text.toString().trim()

        if (numero.isEmpty()) {

            Toast.makeText(
                requireContext(),
                "Numéro de téléphone vide.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val intent =
            Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:$numero")
            )

        try {

            startActivity(intent)

        } catch (_: Exception) {

            Toast.makeText(
                requireContext(),
                "Aucune application téléphone disponible.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun envoyerEmail() {

        val email =
            etEmail.text.toString().trim()

        if (email.isEmpty()) {

            Toast.makeText(
                requireContext(),
                "Adresse e-mail vide.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val intent =
            Intent(Intent.ACTION_SENDTO).apply {

                data =
                    Uri.parse("mailto:$email")
            }

        try {

            startActivity(intent)

        } catch (_: Exception) {

            Toast.makeText(
                requireContext(),
                "Aucune application e-mail disponible.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun sauvegarderBitmap(
        bitmap: Bitmap
    ): String? {

        return try {

            val fichier =
                File(
                    requireContext().filesDir,
                    "photo_${System.currentTimeMillis()}.jpg"
                )

            FileOutputStream(fichier).use { output ->

                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    90,
                    output
                )
            }

            Uri.fromFile(fichier).toString()

        } catch (_: Exception) {

            null
        }
    }
}