package com.example.listedecontactsv3.data

import android.content.ContentValues
import android.database.Cursor
import com.example.listedecontactsv3.Contact

class ContactDao(
    private val dbHelper: ContactDbHelper
) {

    private fun getSortOrder(tri: String): String {
        return when (tri) {
            "prenom" -> "${ContactContract.COLUMN_PRENOM} COLLATE NOCASE ASC"
            "favoris" -> "${ContactContract.COLUMN_FAVORI} DESC, ${ContactContract.COLUMN_NOM} COLLATE NOCASE ASC"
            else -> "${ContactContract.COLUMN_NOM} COLLATE NOCASE ASC"
        }
    }

    fun getAll(sortOrder: String = "nom"): List<Contact> {

        val db = dbHelper.readableDatabase

        val cursor = db.query(
            ContactContract.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            getSortOrder(sortOrder)
        )

        return cursor.use {

            val liste = mutableListOf<Contact>()

            while (it.moveToNext()) {
                liste.add(cursorToContact(it))
            }

            liste
        }
    }

    fun getFavoris(sortOrder: String = "nom"): List<Contact> {

        val db = dbHelper.readableDatabase

        val cursor = db.query(
            ContactContract.TABLE_NAME,
            null,
            "${ContactContract.COLUMN_FAVORI} = ?",
            arrayOf("1"),
            null,
            null,
            getSortOrder(sortOrder)
        )

        return cursor.use {

            val liste = mutableListOf<Contact>()

            while (it.moveToNext()) {
                liste.add(cursorToContact(it))
            }

            liste
        }
    }

    fun getById(id: Long): Contact? {

        val db = dbHelper.readableDatabase

        val cursor = db.query(
            ContactContract.TABLE_NAME,
            null,
            "${ContactContract.COLUMN_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                cursorToContact(it)
            } else {
                null
            }
        }
    }

    fun insert(contact: Contact): Long {

        val values = contactToValues(contact)

        return dbHelper.writableDatabase.insert(
            ContactContract.TABLE_NAME,
            null,
            values
        )
    }

    fun update(contact: Contact): Int {

        val values = contactToValues(contact)

        return dbHelper.writableDatabase.update(
            ContactContract.TABLE_NAME,
            values,
            "${ContactContract.COLUMN_ID} = ?",
            arrayOf(contact.id.toString())
        )
    }

    fun delete(id: Long): Int {

        return dbHelper.writableDatabase.delete(
            ContactContract.TABLE_NAME,
            "${ContactContract.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun setFavori(
        id: Long,
        favori: Boolean
    ): Int {

        val values = ContentValues().apply {
            put(
                ContactContract.COLUMN_FAVORI,
                if (favori) 1 else 0
            )
        }

        return dbHelper.writableDatabase.update(
            ContactContract.TABLE_NAME,
            values,
            "${ContactContract.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun search(query: String, sortOrder: String = "nom"): List<Contact> {

        val db = dbHelper.readableDatabase

        val q = "%${query.trim()}%"

        val cursor = db.query(
            ContactContract.TABLE_NAME,
            null,
            """
                ${ContactContract.COLUMN_NOM} LIKE ?
                OR ${ContactContract.COLUMN_PRENOM} LIKE ?
                OR ${ContactContract.COLUMN_TELEPHONE} LIKE ?
                OR ${ContactContract.COLUMN_EMAIL} LIKE ?
            """.trimIndent(),
            arrayOf(q, q, q, q),
            null,
            null,
            getSortOrder(sortOrder)
        )

        return cursor.use {

            val liste = mutableListOf<Contact>()

            while (it.moveToNext()) {
                liste.add(cursorToContact(it))
            }

            liste
        }
    }

    fun searchFavoris(query: String, sortOrder: String = "nom"): List<Contact> {

        val db = dbHelper.readableDatabase

        val q = "%${query.trim()}%"

        val selection = """
            ${ContactContract.COLUMN_FAVORI} = 1
            AND (
                ${ContactContract.COLUMN_NOM} LIKE ?
                OR ${ContactContract.COLUMN_PRENOM} LIKE ?
                OR ${ContactContract.COLUMN_TELEPHONE} LIKE ?
            )
        """.trimIndent()

        val cursor = db.query(
            ContactContract.TABLE_NAME,
            null,
            selection,
            arrayOf(q, q, q),
            null,
            null,
            getSortOrder(sortOrder)
        )

        return cursor.use {

            val liste = mutableListOf<Contact>()

            while (it.moveToNext()) {
                liste.add(cursorToContact(it))
            }

            liste
        }
    }

    private fun contactToValues(
        contact: Contact
    ): ContentValues {

        return ContentValues().apply {

            put(ContactContract.COLUMN_NOM, contact.nom)
            put(ContactContract.COLUMN_PRENOM, contact.prenom)
            put(ContactContract.COLUMN_TELEPHONE, contact.telephone)

            if (contact.email != null) {
                put(
                    ContactContract.COLUMN_EMAIL,
                    contact.email
                )
            } else {
                putNull(ContactContract.COLUMN_EMAIL)
            }

            if (contact.photoUri != null) {
                put(
                    ContactContract.COLUMN_PHOTO_URI,
                    contact.photoUri
                )
            } else {
                putNull(ContactContract.COLUMN_PHOTO_URI)
            }

            if (contact.latitude != null) {
                put(
                    ContactContract.COLUMN_LATITUDE,
                    contact.latitude
                )
            } else {
                putNull(ContactContract.COLUMN_LATITUDE)
            }

            if (contact.longitude != null) {
                put(
                    ContactContract.COLUMN_LONGITUDE,
                    contact.longitude
                )
            } else {
                putNull(ContactContract.COLUMN_LONGITUDE)
            }

            put(
                ContactContract.COLUMN_FAVORI,
                if (contact.favori) 1 else 0
            )
        }
    }

    private fun cursorToContact(
        cursor: Cursor
    ): Contact {

        val id = cursor.getLong(
            cursor.getColumnIndexOrThrow(
                ContactContract.COLUMN_ID
            )
        )

        val nom = cursor.getString(
            cursor.getColumnIndexOrThrow(
                ContactContract.COLUMN_NOM
            )
        )

        val prenom = cursor.getString(
            cursor.getColumnIndexOrThrow(
                ContactContract.COLUMN_PRENOM
            )
        )

        val telephone = cursor.getString(
            cursor.getColumnIndexOrThrow(
                ContactContract.COLUMN_TELEPHONE
            )
        )

        val emailIndex =
            cursor.getColumnIndexOrThrow(
                ContactContract.COLUMN_EMAIL
            )

        val photoIndex =
            cursor.getColumnIndexOrThrow(
                ContactContract.COLUMN_PHOTO_URI
            )

        val latitudeIndex =
            cursor.getColumnIndexOrThrow(
                ContactContract.COLUMN_LATITUDE
            )

        val longitudeIndex =
            cursor.getColumnIndexOrThrow(
                ContactContract.COLUMN_LONGITUDE
            )

        val favoriIndex =
            cursor.getColumnIndexOrThrow(
                ContactContract.COLUMN_FAVORI
            )

        return Contact(
            id = id,
            nom = nom,
            prenom = prenom,
            telephone = telephone,
            email = if (cursor.isNull(emailIndex))
                null
            else
                cursor.getString(emailIndex),

            photoUri = if (cursor.isNull(photoIndex))
                null
            else
                cursor.getString(photoIndex),

            latitude = if (cursor.isNull(latitudeIndex))
                null
            else
                cursor.getDouble(latitudeIndex),

            longitude = if (cursor.isNull(longitudeIndex))
                null
            else
                cursor.getDouble(longitudeIndex),

            favori = cursor.getInt(favoriIndex) == 1
        )
    }
}