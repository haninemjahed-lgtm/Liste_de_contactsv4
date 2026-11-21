package com.example.listedecontactsv3

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.listedecontactsv3.data.ContactDao
import com.example.listedecontactsv3.data.ContactDbHelper
import com.example.listedecontactsv3.preferences.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dao =
        ContactDao(
            ContactDbHelper.getInstance(application)
        )

    private val _contacts =
        MutableStateFlow<List<Contact>>(emptyList())

    val contacts: StateFlow<List<Contact>> =
        _contacts.asStateFlow()

    private val _favoris =
        MutableStateFlow<List<Contact>>(emptyList())

    val favoris: StateFlow<List<Contact>> =
        _favoris.asStateFlow()

    private val _recherche =
        MutableStateFlow("")

    val recherche: StateFlow<String> =
        _recherche.asStateFlow()

    private val _rechercheFavoris =
        MutableStateFlow("")

    val rechercheFavoris: StateFlow<String> =
        _rechercheFavoris.asStateFlow()

    private val _contactsFiltres =
        MutableStateFlow<List<Contact>>(emptyList())

    val contactsFiltres: StateFlow<List<Contact>> =
        _contactsFiltres.asStateFlow()

    private val _favorisFiltres =
        MutableStateFlow<List<Contact>>(emptyList())

    val favorisFiltres: StateFlow<List<Contact>> =
        _favorisFiltres.asStateFlow()

    init {
        chargerContacts()
    }

    fun chargerContacts() {

        viewModelScope.launch {

            val sortOrder = PreferencesManager.ordreTri(getApplication())

            val result = withContext(Dispatchers.IO) {
                dao.getAll(sortOrder)
            }

            _contacts.value = result

            val fav = withContext(Dispatchers.IO) {
                dao.getFavoris(sortOrder)
            }

            _favoris.value = fav

            appliquerRecherche()
            appliquerRechercheFavoris()
        }
    }

    fun rechercher(
        query: String
    ) {

        _recherche.value = query

        viewModelScope.launch {

            val sortOrder = PreferencesManager.ordreTri(getApplication())

            val result = withContext(Dispatchers.IO) {

                if (query.trim().isEmpty()) {
                    dao.getAll(sortOrder)
                } else {
                    dao.search(query, sortOrder)
                }
            }

            _contactsFiltres.value = result
        }
    }

    fun rechercherFavoris(
        query: String
    ) {

        _rechercheFavoris.value = query

        viewModelScope.launch {

            val sortOrder = PreferencesManager.ordreTri(getApplication())

            val result = withContext(Dispatchers.IO) {

                if (query.trim().isEmpty()) {
                    dao.getFavoris(sortOrder)
                } else {
                    dao.searchFavoris(query, sortOrder)
                }
            }

            _favorisFiltres.value = result
        }
    }

    fun getById(
        id: Long
    ): Contact? {

        return _contacts.value.firstOrNull {
            it.id == id
        }
    }

    fun getByIdAsync(
        id: Long,
        onResult: (Contact?) -> Unit
    ) {

        viewModelScope.launch {

            val contact =
                withContext(Dispatchers.IO) {
                    dao.getById(id)
                }

            onResult(contact)
        }
    }

    fun insert(
        contact: Contact
    ) {

        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                dao.insert(contact)
            }

            chargerContacts()
        }
    }

    fun update(
        contact: Contact
    ) {

        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                dao.update(contact)
            }

            chargerContacts()
        }
    }

    fun delete(
        id: Long
    ) {

        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                dao.delete(id)
            }

            chargerContacts()
        }
    }

    fun changerFavori(
        contact: Contact
    ) {

        viewModelScope.launch {

            withContext(Dispatchers.IO) {

                dao.setFavori(
                    contact.id,
                    !contact.favori
                )
            }

            chargerContacts()
        }
    }

    fun insert(
        contact: Contact,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val id = withContext(Dispatchers.IO) {
                dao.insert(contact)
            }
            chargerContacts()
            onResult(id != -1L)
        }
    }

    fun update(
        contact: Contact,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val rows = withContext(Dispatchers.IO) {
                dao.update(contact)
            }
            chargerContacts()
            onResult(rows == 1)
        }
    }

    fun delete(
        id: Long,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val rows = withContext(Dispatchers.IO) {
                dao.delete(id)
            }
            chargerContacts()
            onResult(rows > 0)
        }
    }

    fun executerBenchmark100Contacts(
        onDone: (dureeSansTxMs: Long, dureeAvecTxMs: Long) -> Unit
    ) {
        viewModelScope.launch {
            val lot1 = List(100) { i ->
                Contact(
                    nom = "TestSansTx_${i + 1}",
                    prenom = "Contact",
                    telephone = "+216 20 ${(100 + i)}",
                    email = null,
                    photoUri = null,
                    latitude = null,
                    longitude = null,
                    favori = false
                )
            }
            val lot2 = List(100) { i ->
                Contact(
                    nom = "TestAvecTx_${i + 1}",
                    prenom = "Contact",
                    telephone = "+216 21 ${(100 + i)}",
                    email = null,
                    photoUri = null,
                    latitude = null,
                    longitude = null,
                    favori = false
                )
            }

            val (sansTx, avecTx) = withContext(Dispatchers.IO) {
                val tSans = dao.insertBatchSansTransaction(lot1)
                val tAvec = dao.insertBatchAvecTransaction(lot2)
                tSans to tAvec
            }

            chargerContacts()
            onDone(sansTx, avecTx)
        }
    }

    private fun appliquerRecherche() {

        val q = _recherche.value.trim()

        if (q.isEmpty()) {

            _contactsFiltres.value =
                _contacts.value

        } else {

            val texte =
                q.lowercase()

            _contactsFiltres.value =
                _contacts.value.filter {

                    it.nom.lowercase()
                        .contains(texte) ||

                            it.prenom.lowercase()
                                .contains(texte) ||

                            it.telephone
                                .contains(q) ||

                            (it.email ?: "")
                                .lowercase()
                                .contains(texte)
                }
        }
    }

    private fun appliquerRechercheFavoris() {

        val q =
            _rechercheFavoris.value.trim()

        if (q.isEmpty()) {

            _favorisFiltres.value =
                _favoris.value

        } else {

            val texte =
                q.lowercase()

            _favorisFiltres.value =
                _favoris.value.filter {

                    it.nom.lowercase()
                        .contains(texte) ||

                            it.prenom.lowercase()
                                .contains(texte) ||

                            it.telephone
                                .contains(q)
                }
        }
    }
}