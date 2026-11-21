package com.example.listedecontactsv3

import android.graphics.Color
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ContactAdapter(
    private val onClick: (Contact) -> Unit,
    private val onFavori: (Contact) -> Unit,
    private val onModifier: (Contact) -> Unit,
    private val onAppeler: (Contact) -> Unit,
    private val onSupprimer: (Contact) -> Unit
) : ListAdapter<Contact, ContactAdapter.ContactViewHolder>(DIFF_CALLBACK) {

    private var taillePolice = 16f
    private var couleurTexte = Color.BLACK
    private var afficherAppel = false

    fun appliquerPreferences(
        taille: Float,
        couleur: Int,
        appel: Boolean
    ) {
        taillePolice = taille
        couleurTexte = couleur
        afficherAppel = appel
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_contact,
            parent,
            false
        )
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ContactViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivPhoto =
            itemView.findViewById<ImageView>(R.id.ivContactPhoto)

        private val tvNom =
            itemView.findViewById<TextView>(R.id.tvNom)

        private val tvPrenom =
            itemView.findViewById<TextView>(R.id.tvPrenom)

        private val tvTelephone =
            itemView.findViewById<TextView>(R.id.tvTelephone)

        private val btnFavori =
            itemView.findViewById<ImageButton>(R.id.btnFavori)

        private val btnModifier =
            itemView.findViewById<ImageButton>(R.id.btnModifier)

        private val btnSupprimer =
            itemView.findViewById<ImageButton>(R.id.btnSupprimer)

        private val btnAppeler =
            itemView.findViewById<ImageButton>(R.id.btnAppeler)

        private val avatarDefaultPaddingPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            12f,
            itemView.resources.displayMetrics
        ).toInt()

        private fun afficherAvatarParDefaut() {
            ivPhoto.setBackgroundResource(R.drawable.bg_contact_avatar)
            ivPhoto.setPadding(
                avatarDefaultPaddingPx,
                avatarDefaultPaddingPx,
                avatarDefaultPaddingPx,
                avatarDefaultPaddingPx
            )
            ivPhoto.setImageResource(android.R.drawable.ic_menu_myplaces)
        }

        private fun afficherPhotoContact(uri: Uri) {
            ivPhoto.background = null
            ivPhoto.setPadding(0, 0, 0, 0)
            ivPhoto.setImageURI(uri)
        }

        fun bind(contact: Contact) {
            tvNom.text = contact.nom
            tvPrenom.text = contact.prenom
            tvTelephone.text = contact.telephone

            tvNom.textSize = taillePolice
            tvPrenom.textSize = taillePolice
            tvTelephone.textSize = taillePolice

            tvNom.setTextColor(couleurTexte)
            tvPrenom.setTextColor(couleurTexte)

            if (!contact.photoUri.isNullOrEmpty()) {
                try {
                    val uri = Uri.parse(contact.photoUri)
                    afficherPhotoContact(uri)
                    if (ivPhoto.drawable == null) {
                        afficherAvatarParDefaut()
                    }
                } catch (_: Exception) {
                    afficherAvatarParDefaut()
                }
            } else {
                afficherAvatarParDefaut()
            }

            btnFavori.setImageResource(
                if (contact.favori) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )

            btnAppeler.visibility =
                if (afficherAppel) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                onClick(contact)
            }

            btnFavori.setOnClickListener {
                onFavori(contact)
            }

            btnModifier.setOnClickListener {
                onModifier(contact)
            }

            btnSupprimer.setOnClickListener {
                onSupprimer(contact)
            }

            btnAppeler.setOnClickListener {
                onAppeler(contact)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<Contact>() {
                override fun areItemsTheSame(
                    oldItem: Contact,
                    newItem: Contact
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: Contact,
                    newItem: Contact
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}