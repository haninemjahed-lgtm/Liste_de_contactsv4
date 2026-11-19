package com.example.listedecontactsv3

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.listedecontactsv3.preferences.ParametresFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(
    R.layout.activity_main
) {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        val navigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigation
            )

        if (savedInstanceState == null) {

            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragment_container,
                    ListeContactsFragment()
                )
                .commit()
        }

        navigation.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.nav_contacts -> {

                    supportFragmentManager
                        .beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            ListeContactsFragment()
                        )
                        .commit()

                    true
                }

                R.id.nav_favoris -> {

                    supportFragmentManager
                        .beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            FavorisFragment()
                        )
                        .commit()

                    true
                }

                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_parametres -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        ParametresFragment()
                    )
                    .addToBackStack(null)
                    .commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}