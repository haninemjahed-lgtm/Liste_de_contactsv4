package com.example.listedecontactsv3

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.listedecontactsv3.preferences.ParametresFragment

class MainActivity : AppCompatActivity(
    R.layout.activity_main
) {

    private enum class OngletActif {
        CONTACTS, FAVORIS, PARAMETRES
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        val navContacts = findViewById<LinearLayout>(R.id.navContacts)
        val navFavoris = findViewById<LinearLayout>(R.id.navFavoris)
        val navParametres = findViewById<LinearLayout>(R.id.navParametres)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragment_container,
                    ListeContactsFragment()
                )
                .commit()
            mettreAJourOngletActif(OngletActif.CONTACTS)
        }

        navContacts.setOnClickListener {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragment_container,
                    ListeContactsFragment()
                )
                .commit()
            mettreAJourOngletActif(OngletActif.CONTACTS)
        }

        navFavoris.setOnClickListener {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragment_container,
                    FavorisFragment()
                )
                .commit()
            mettreAJourOngletActif(OngletActif.FAVORIS)
        }

        navParametres.setOnClickListener {
            ouvrirParametres()
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val fragmentCourant = supportFragmentManager.findFragmentById(R.id.fragment_container)
            when (fragmentCourant) {
                is FavorisFragment -> mettreAJourOngletActif(OngletActif.FAVORIS)
                is ParametresFragment -> mettreAJourOngletActif(OngletActif.PARAMETRES)
                is ListeContactsFragment -> mettreAJourOngletActif(OngletActif.CONTACTS)
            }
        }
    }

    private fun ouvrirParametres() {
        if (supportFragmentManager.findFragmentById(R.id.fragment_container) !is ParametresFragment) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragment_container,
                    ParametresFragment()
                )
                .addToBackStack(null)
                .commit()
        }
        mettreAJourOngletActif(OngletActif.PARAMETRES)
    }

    private fun mettreAJourOngletActif(actif: OngletActif) {
        val bleuActif = Color.parseColor("#1565C0")
        val grisInactif = Color.parseColor("#546E7A")

        val ivContacts = findViewById<ImageView>(R.id.ivNavContacts)
        val tvContacts = findViewById<TextView>(R.id.tvNavContacts)
        val ivFavoris = findViewById<ImageView>(R.id.ivNavFavoris)
        val tvFavoris = findViewById<TextView>(R.id.tvNavFavoris)
        val ivParametres = findViewById<ImageView>(R.id.iconParametres)
        val tvParametres = findViewById<TextView>(R.id.textParametres)

        ivContacts.setColorFilter(if (actif == OngletActif.CONTACTS) bleuActif else grisInactif)
        tvContacts.setTextColor(if (actif == OngletActif.CONTACTS) bleuActif else grisInactif)

        ivFavoris.setColorFilter(if (actif == OngletActif.FAVORIS) bleuActif else grisInactif)
        tvFavoris.setTextColor(if (actif == OngletActif.FAVORIS) bleuActif else grisInactif)

        ivParametres.setColorFilter(if (actif == OngletActif.PARAMETRES) bleuActif else grisInactif)
        tvParametres.setTextColor(if (actif == OngletActif.PARAMETRES) bleuActif else grisInactif)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_parametres -> {
                ouvrirParametres()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}