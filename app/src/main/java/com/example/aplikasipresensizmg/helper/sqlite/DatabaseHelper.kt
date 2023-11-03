package com.example.aplikasipresensizmg.helper.sqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "HistoriUserDB"
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME = "HistoriUserTable"
        const val COLUMN_ID = "id"
        const val COLUMN_IDUSERINSERVER = "idUserInServer"
        const val COLUMN_NAME = "name"
        const val COLUMN_NIP = "nip"
        const val COLUMN_ROLE = "role"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $TABLE_NAME " +
                    "($COLUMN_ID INTEGER PRIMARY KEY, " +
                    "$COLUMN_IDUSERINSERVER TEXT, " +
                    "$COLUMN_NAME TEXT, " +
                    "$COLUMN_NIP TEXT, " +
                    "$COLUMN_ROLE TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}