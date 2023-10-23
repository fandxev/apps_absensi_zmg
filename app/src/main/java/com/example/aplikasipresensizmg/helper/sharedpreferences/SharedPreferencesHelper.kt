package com.example.aplikasipresensizmg.helper.sharedpreferences

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper {

    companion object {
         lateinit var prefs : SharedPreferences

        private const val PREF_NAME = "pref_presensi_zmg"

        const val ACCESS_TOKEN = "access_token"
        const val ID_USER = "id"
        const val NAME = "name"
        const val NIP = "nip"
        const val EMAIL = "email"
        const val EMAIL_VERIFIED_AT = "email_verified_at"
        const val ROLE = "role"
        const val STATUS = "status"
        const val CREATED_AT = "created_at"
        const val UPDATED_AT = "updated_at"
        fun init(context : Context){
            prefs = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE)
        }

        fun read(key: String, value:String) : String?{
            return prefs.getString(key,value)
        }

        fun write(key: String, value : String){
            val prefsEditor : SharedPreferences.Editor = prefs.edit()
            with(prefsEditor)
            {
                putString(key,value)
                commit()
            }
        }

        fun removesesion(context : Context): Boolean {
            prefs = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE)
            val prefsEditor : SharedPreferences.Editor = prefs.edit()
            prefsEditor.clear().commit()
            return true
        }
    }



}