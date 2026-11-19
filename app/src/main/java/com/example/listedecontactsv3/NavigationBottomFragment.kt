package com.example.listedecontactsv3

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class NavigationBottomFragment :
    Fragment(R.layout.fragment_navigation_bottom) {

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val navigation =
            view.findViewById<BottomNavigationView>(
                R.id.bottomNavigation
            )

        navigation.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.nav_contacts -> {

                    parentFragmentManager
                        .beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            ListeContactsFragment()
                        )
                        .commit()

                    true
                }

                R.id.nav_favoris -> {

                    parentFragmentManager
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
}