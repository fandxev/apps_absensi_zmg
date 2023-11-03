package com.example.aplikasipresensizmg.helper.sharedpreferences

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHistoryUserLogin {

    companion object {
        lateinit var prefHistory : SharedPreferences

        private const val PREF_NAME = "pref_history_user_login"




        fun init(context: Context){
            prefHistory = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE)
        }

        fun read(key:String, value:String): String?{
            return prefHistory.getString(key,value)
        }

        fun write(key:String, value:String){
            prefHistory.edit().run {
                putString(key,value)
                commit()
            }
        }

        fun removeSession(context:Context):Boolean{
            try {
                prefHistory = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                prefHistory.edit().clear().commit()
            return true
            }
            catch (e:Exception){
                return false
            }
        }

    }
}