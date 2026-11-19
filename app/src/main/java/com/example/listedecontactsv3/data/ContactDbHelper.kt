package com.example.listedecontactsv3.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ContactDbHelper private constructor(
    context: Context
) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {

        val createTable = """
            CREATE TABLE ${ContactContract.TABLE_NAME} (
                ${ContactContract.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${ContactContract.COLUMN_NOM} TEXT NOT NULL,
                ${ContactContract.COLUMN_PRENOM} TEXT NOT NULL,
                ${ContactContract.COLUMN_TELEPHONE} TEXT NOT NULL,
                ${ContactContract.COLUMN_EMAIL} TEXT,
                ${ContactContract.COLUMN_PHOTO_URI} TEXT,
                ${ContactContract.COLUMN_LATITUDE} REAL,
                ${ContactContract.COLUMN_LONGITUDE} REAL,
                ${ContactContract.COLUMN_FAVORI} INTEGER DEFAULT 0
            )
        """.trimIndent()

        db.execSQL(createTable)

        insererContactDemo(
            db,
            "Gharbi",
            "Nour",
            "55123456",
            "nour@mail.com",
            null,
            34.7406,
            10.7603,
            true
        )

        insererContactDemo(
            db,
            "Trabelsi",
            "Sarra",
            "98765432",
            "sarra@mail.com",
            null,
            36.8065,
            10.1815,
            false
        )

        insererContactDemo(
            db,
            "Jaziri",
            "Amel",
            "29876543",
            "amel@mail.com",
            null,
            35.8256,
            10.6084,
            true
        )

        insererContactDemo(
            db,
            "Mansour",
            "Sami",
            "50111222",
            "sami@mail.com",
            null,
            null,
            null,
            false
        )

        insererContactDemo(
            db,
            "Ben Ali",
            "Ahmed",
            "22123456",
            "ahmed@mail.com",
            null,
            36.4000,
            10.6167,
            false
        )
    }

    private fun insererContactDemo(
        db: SQLiteDatabase,
        nom: String,
        prenom: String,
        telephone: String,
        email: String?,
        photoUri: String?,
        latitude: Double?,
        longitude: Double?,
        favori: Boolean
    ) {

        val values = ContentValues().apply {

            put(ContactContract.COLUMN_NOM, nom)
            put(ContactContract.COLUMN_PRENOM, prenom)
            put(ContactContract.COLUMN_TELEPHONE, telephone)

            if (email != null) {
                put(ContactContract.COLUMN_EMAIL, email)
            }

            if (photoUri != null) {
                put(ContactContract.COLUMN_PHOTO_URI, photoUri)
            }

            if (latitude != null) {
                put(ContactContract.COLUMN_LATITUDE, latitude)
            }

            if (longitude != null) {
                put(ContactContract.COLUMN_LONGITUDE, longitude)
            }

            put(
                ContactContract.COLUMN_FAVORI,
                if (favori) 1 else 0
            )
        }

        db.insert(
            ContactContract.TABLE_NAME,
            null,
            values
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {

        if (oldVersion < 2) {

            db.execSQL(
                "ALTER TABLE ${ContactContract.TABLE_NAME} " +
                        "ADD COLUMN ${ContactContract.COLUMN_FAVORI} INTEGER DEFAULT 0"
            )
        }
    }

    companion object {

        private const val DATABASE_NAME = "contacts.db"

        private const val DATABASE_VERSION = 2

        @Volatile
        private var INSTANCE: ContactDbHelper? = null

        fun getInstance(context: Context): ContactDbHelper {

            return INSTANCE ?: synchronized(this) {

                val instance = ContactDbHelper(
                    context.applicationContext
                )

                INSTANCE = instance

                instance
            }
        }
    }
}