package com.example.listedecontactsv3.preferences

import android.content.Context
import android.graphics.Color
import androidx.preference.PreferenceManager

object PreferencesManager {

    private const val KEY_TAILLE = "taille_police"
    private const val KEY_COULEUR = "couleur_preferee"
    private const val KEY_APPEL = "appel_depuis_liste"
    private const val KEY_TRI = "ordre_tri"

    fun taillePolice(
        context: Context
    ): Float {

        val prefs =
            PreferenceManager
                .getDefaultSharedPreferences(context)

        return prefs
            .getString(KEY_TAILLE, "16")
            ?.toFloatOrNull()
            ?: 16f
    }

    fun couleurTexte(
        context: Context
    ): Int {

        val prefs =
            PreferenceManager
                .getDefaultSharedPreferences(context)

        return try {

            Color.parseColor(
                prefs.getString(
                    KEY_COULEUR,
                    "#000000"
                ) ?: "#000000"
            )

        } catch (e: Exception) {

            Color.BLACK
        }
    }

    fun appelDepuisListe(
        context: Context
    ): Boolean {

        val prefs =
            PreferenceManager
                .getDefaultSharedPreferences(context)

        return prefs.getBoolean(
            KEY_APPEL,
            false
        )
    }

    fun ordreTri(
        context: Context
    ): String {

        val prefs =
            PreferenceManager
                .getDefaultSharedPreferences(context)

        return prefs.getString(
            KEY_TRI,
            "nom"
        ) ?: "nom"
    }
}