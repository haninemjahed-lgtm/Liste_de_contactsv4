package com.example.listedecontactsv3

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.listedecontactsv3.preferences.PreferencesManager
import java.io.File
import java.io.FileOutputStream

class DetailContactFragment : Fragment() {

    private val vm: ContactViewModel by activityViewModels()

    private var contactId: Long = 0L
    private var photoUri: String? = null
    private var favori: Boolean = false
    private var cameraTempUri: Uri? = null

    private lateinit var ivPhoto: ImageView
    private lateinit var btnGalerie: Button
    private lateinit var btnCamera: Button
    private lateinit var btnFavori: Button
    private lateinit var etNom: EditText
    private lateinit var etPrenom: EditText
    private lateinit var etTelephone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText
    private lateinit var btnAppeler: Button
    private lateinit var btnEmail: Button
    private lateinit var btnCarte: Button
    private lateinit var btnEnregistrer: Button
    private lateinit var btnAnnuler: Button
    private lateinit var btnSupprimer: Button

    private val choisirPhoto =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                val cheminInterne = copierDansStockageInterne(uri)
                if (cheminInterne != null) {
                    photoUri = cheminInterne
                    afficherPhoto(photoUri)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Impossible de copier la photo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    private val prendrePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { succes ->
            if (succes && cameraTempUri != null) {
                val cheminInterne = copierDansStockageInterne(cameraTempUri!!)
                if (cheminInterne != null) {
                    photoUri = cheminInterne
                    afficherPhoto(photoUri)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Impossible d'enregistrer la photo prise",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactId = arguments?.getLong(ARG_CONTACT_ID, 0L) ?: 0L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_detail_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivPhoto = view.findViewById(R.id.ivPhoto)
        btnGalerie = view.findViewById(R.id.btnGalerie)
        btnCamera = view.findViewById(R.id.btnCamera)
        btnFavori = view.findViewById(R.id.btnFavori)
        etNom = view.findViewById(R.id.etNom)
        etPrenom = view.findViewById(R.id.etPrenom)
        etTelephone = view.findViewById(R.id.etTelephone)
        etEmail = view.findViewById(R.id.etEmail)
        etLatitude = view.findViewById(R.id.etLatitude)
        etLongitude = view.findViewById(R.id.etLongitude)
        btnAppeler = view.findViewById(R.id.btnAppeler)
        btnEmail = view.findViewById(R.id.btnEmail)
        btnCarte = view.findViewById(R.id.btnCarte)
        btnEnregistrer = view.findViewById(R.id.btnEnregistrer)
        btnAnnuler = view.findViewById(R.id.btnAnnuler)
        btnSupprimer = view.findViewById(R.id.btnSupprimer)

        appliquerPreferencesAffichage()

        if (savedInstanceState != null) {
            photoUri = savedInstanceState.getString(STATE_PHOTO_URI)
            favori = savedInstanceState.getBoolean(STATE_FAVORI, false)
            afficherPhoto(photoUri)
            afficherFavori(btnFavori)
        } else {
            afficherFavori(btnFavori)
            afficherPhoto(null)

            if (contactId != 0L) {
                vm.getByIdAsync(contactId) { contact ->
                    if (!isAdded) return@getByIdAsync
                    if (contact == null) {
                        Toast.makeText(
                            requireContext(),
                            "Contact introuvable",
                            Toast.LENGTH_SHORT
                        ).show()
                        parentFragmentManager.popBackStack()
                        return@getByIdAsync
                    }

                    etNom.setText(contact.nom)
                    etPrenom.setText(contact.prenom)
                    etTelephone.setText(contact.telephone)
                    etEmail.setText(contact.email.orEmpty())
                    etLatitude.setText(contact.latitude?.toString().orEmpty())
                    etLongitude.setText(contact.longitude?.toString().orEmpty())

                    photoUri = contact.photoUri
                    afficherPhoto(photoUri)

                    favori = contact.favori
                    afficherFavori(btnFavori)
                }
            }
        }

        btnSupprimer.visibility = if (contactId != 0L) View.VISIBLE else View.GONE

        btnGalerie.setOnClickListener {
            choisirPhoto.launch("image/*")
        }

        btnCamera.setOnClickListener {
            preparerEtLancerCamera()
        }

        btnFavori.setOnClickListener {
            favori = !favori
            afficherFavori(btnFavori)
        }

        btnAppeler.setOnClickListener {
            val tel = etTelephone.text.toString().trim()
            if (tel.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Numéro de téléphone vide.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            try {
                startActivity(Intent(Intent.ACTION_DIAL, "tel:${Uri.encode(tel)}".toUri()))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(
                    requireContext(),
                    "Aucune application téléphone disponible.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnEmail.setOnClickListener {
            val mail = etEmail.text.toString().trim()
            if (mail.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Adresse e-mail absente.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            try {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:${Uri.encode(mail)}".toUri()
                }
                startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(
                    requireContext(),
                    "Aucune application de messagerie disponible.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnCarte.setOnClickListener {
            val coords = lireEtValiderCoordonnees() ?: return@setOnClickListener
            if (coords.first == null || coords.second == null) {
                Toast.makeText(
                    requireContext(),
                    "Coordonnées GPS absentes pour ce contact.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CarteContactFragment.newInstance(contactId))
                .addToBackStack(null)
                .commit()
        }

        btnEnregistrer.setOnClickListener {
            enregistrer()
        }

        btnAnnuler.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnSupprimer.setOnClickListener {
            confirmerSuppression()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_PHOTO_URI, photoUri)
        outState.putBoolean(STATE_FAVORI, favori)
    }

    private fun afficherPhoto(uriStr: String?) {
        val pxPaddingDefaut = (20 * resources.displayMetrics.density).toInt()
        if (!uriStr.isNullOrBlank()) {
            ivPhoto.setBackgroundColor(Color.TRANSPARENT)
            ivPhoto.setPadding(0, 0, 0, 0)
            ivPhoto.scaleType = ImageView.ScaleType.CENTER_CROP
            ivPhoto.setImageURI(uriStr.toUri())
        } else {
            ivPhoto.setBackgroundResource(R.drawable.bg_contact_avatar)
            ivPhoto.setPadding(pxPaddingDefaut, pxPaddingDefaut, pxPaddingDefaut, pxPaddingDefaut)
            ivPhoto.scaleType = ImageView.ScaleType.CENTER_INSIDE
            ivPhoto.setImageResource(android.R.drawable.ic_menu_myplaces)
        }
    }

    private fun appliquerPreferencesAffichage() {
        val ctx = requireContext()
        val taille = PreferencesManager.taillePolice(ctx)
        val couleur = PreferencesManager.couleurTexte(ctx)

        listOf(etNom, etPrenom, etTelephone).forEach { champ ->
            champ.textSize = taille
        }
        etNom.setTextColor(couleur)
        etPrenom.setTextColor(couleur)
    }

    private fun afficherFavori(bouton: Button) {
        bouton.text = if (favori) "★ Favori" else "☆ Favori"
    }

    private fun preparerEtLancerCamera() {
        try {
            val fichierTemp = File(
                requireContext().cacheDir,
                "capture_${System.currentTimeMillis()}.jpg"
            )
            val uri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                fichierTemp
            )
            cameraTempUri = uri
            prendrePhoto.launch(uri)
        } catch (_: Exception) {
            Toast.makeText(
                requireContext(),
                "Impossible d'ouvrir la caméra.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun copierDansStockageInterne(uri: Uri): String? {
        return try {
            val fichier = File(
                requireContext().filesDir,
                "photo_${System.currentTimeMillis()}.jpg"
            )
            requireContext().contentResolver.openInputStream(uri)?.use { entree ->
                FileOutputStream(fichier).use { sortie ->
                    entree.copyTo(sortie)
                }
            } ?: return null
            Uri.fromFile(fichier).toString()
        } catch (_: Exception) {
            null
        }
    }

    private fun lireEtValiderCoordonnees(): Pair<Double?, Double?>? {
        val latText = etLatitude.text.toString().trim()
        val lonText = etLongitude.text.toString().trim()

        if (latText.isEmpty() && lonText.isEmpty()) {
            return null to null
        }

        if (latText.isEmpty() || lonText.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Renseignez à la fois la latitude et la longitude, ou laissez les deux vides.",
                Toast.LENGTH_SHORT
            ).show()
            return null
        }

        val lat = latText.toDoubleOrNull()
        val lon = lonText.toDoubleOrNull()

        if (lat == null || lon == null || lat !in -90.0..90.0 || lon !in -180.0..180.0) {
            Toast.makeText(
                requireContext(),
                "Coordonnées GPS invalides (latitude : -90..90, longitude : -180..180).",
                Toast.LENGTH_SHORT
            ).show()
            return null
        }

        return lat to lon
    }

    private fun enregistrer() {
        val nom = etNom.text.toString().trim()
        val prenom = etPrenom.text.toString().trim()
        val telephone = etTelephone.text.toString().trim()
        val email = etEmail.text.toString().trim()

        if (nom.isEmpty() || prenom.isEmpty() || telephone.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Nom, prénom et téléphone sont obligatoires.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val coords = lireEtValiderCoordonnees() ?: return

        val contact = Contact(
            id = contactId,
            nom = nom,
            prenom = prenom,
            telephone = telephone,
            email = email.ifEmpty { null },
            photoUri = photoUri,
            latitude = coords.first,
            longitude = coords.second,
            favori = favori
        )

        if (contactId == 0L) {
            vm.insert(contact) { ok ->
                if (!isAdded) return@insert
                Toast.makeText(
                    requireContext(),
                    if (ok) "Contact ajouté avec succès" else "Échec de l'ajout",
                    Toast.LENGTH_SHORT
                ).show()
                if (ok) parentFragmentManager.popBackStack()
            }
        } else {
            vm.update(contact) { ok ->
                if (!isAdded) return@update
                Toast.makeText(
                    requireContext(),
                    if (ok) "Contact mis à jour" else "Contact introuvable ou échec de mise à jour",
                    Toast.LENGTH_SHORT
                ).show()
                if (ok) parentFragmentManager.popBackStack()
            }
        }
    }

    private fun confirmerSuppression() {
        AlertDialog.Builder(requireContext())
            .setTitle("Supprimer le contact")
            .setMessage("Voulez-vous vraiment supprimer ce contact ?")
            .setPositiveButton("Supprimer") { _, _ ->
                vm.delete(contactId) { ok ->
                    if (!isAdded) return@delete
                    Toast.makeText(
                        requireContext(),
                        if (ok) "Contact supprimé" else "Contact introuvable",
                        Toast.LENGTH_SHORT
                    ).show()
                    if (ok) parentFragmentManager.popBackStack()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    companion object {
        private const val ARG_CONTACT_ID = "arg_contact_id"
        private const val STATE_PHOTO_URI = "state_photo_uri"
        private const val STATE_FAVORI = "state_favori"

        fun newInstanceForCreation(): DetailContactFragment = newInstance(0L)

        fun newInstance(contactId: Long): DetailContactFragment {
            return DetailContactFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_CONTACT_ID, contactId)
                }
            }
        }
    }
}