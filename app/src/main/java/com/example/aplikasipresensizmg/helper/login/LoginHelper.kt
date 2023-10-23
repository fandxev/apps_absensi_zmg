package com.example.aplikasipresensizmg.helper.login

import com.example.aplikasipresensizmg.helper.sharedpreferences.SharedPreferencesHelper

class LoginHelper {
    companion object {
        fun checkLogin(): Boolean {
            //check apakah shared
            return SharedPreferencesHelper.prefs.contains(SharedPreferencesHelper.ACCESS_TOKEN)
        }
    }
}