package com.example.aplikasipresensizmg.helper.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.provider.ContactsContract.Data

class DataHandler(context: Context) {

    private val dbHelper = DatabaseHelper(context)
    private val database: SQLiteDatabase = dbHelper.writableDatabase

    fun insertData(idUserInServer:String,name: String, nip: String,role:String): Long {
        val values = ContentValues()
        values.put(DatabaseHelper.COLUMN_IDUSERINSERVER, idUserInServer)
        values.put(DatabaseHelper.COLUMN_NAME, name)
        values.put(DatabaseHelper.COLUMN_NIP, nip)
        values.put(DatabaseHelper.COLUMN_ROLE, role)
        return database.insert(DatabaseHelper.TABLE_NAME, null, values)
    }

    fun insertDataIfNotExists(idUserInServer:String,name: String, nip: String,role:String):Long{
        val cursor = database.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_NAME} WHERE ${DatabaseHelper.COLUMN_IDUSERINSERVER} = ?",
            arrayOf(idUserInServer)
        )

        if(cursor.count == 0){
            //Data belum ada, maka lakukan operasi insert
            val values = ContentValues()
            values.put(DatabaseHelper.COLUMN_IDUSERINSERVER, idUserInServer)
            values.put(DatabaseHelper.COLUMN_NAME, name)
            values.put(DatabaseHelper.COLUMN_NIP, nip)
            values.put(DatabaseHelper.COLUMN_ROLE, role)
            return database.insert(DatabaseHelper.TABLE_NAME,null,values)
        }
        else
        {
            return 0
        }


    }

    fun updateData(id:Long,idUserInServer:String,name: String, nip: String,role:String): Int {
        val values = ContentValues()
        values.put(DatabaseHelper.COLUMN_IDUSERINSERVER, idUserInServer)
        values.put(DatabaseHelper.COLUMN_NAME, name)
        values.put(DatabaseHelper.COLUMN_NIP, nip)
        values.put(DatabaseHelper.COLUMN_ROLE, role)
        return database.update(
            DatabaseHelper.TABLE_NAME,
            values,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun deleteData(id: Long): Int {
        return database.delete(
            DatabaseHelper.TABLE_NAME,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun getAllData(): ArrayList<HistoriUserData> {
        val dataItemList = ArrayList<HistoriUserData>()
        val cursor: Cursor = database.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_NAME}", null)
        while (cursor.moveToNext()) {
           // val id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID))
           // val idUserInServer = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_IDUSERINSERVER))
            val name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME))
            val nip = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NIP))
           // val role = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ROLE))

            //dataItemList.add(HistoriUserData(id,idUserInServer,name,nip,role))
            dataItemList.add(HistoriUserData(name,nip))
        }
        cursor.close()
        return dataItemList
    }

    fun getFewNewestData(numberOfMaxData:Int): ArrayList<HistoriUserData>{
        val dataItemList = ArrayList<HistoriUserData>()
        val cursor: Cursor = database.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_NAME} ORDER BY ${DatabaseHelper.COLUMN_ID} DESC LIMIT $numberOfMaxData", null)
        while (cursor.moveToNext()) {
            // val id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID))
            // val idUserInServer = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_IDUSERINSERVER))
            val name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME))
            val nip = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NIP))
            // val role = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ROLE))

            //dataItemList.add(HistoriUserData(id,idUserInServer,name,nip,role))
            dataItemList.add(HistoriUserData(name,nip))
        }
        cursor.close()
        return dataItemList
    }
}