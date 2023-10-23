package com.example.aplikasipresensizmg

class BankURL {
    companion object{
        //const val  BASE_URL : String = "https://admin.zonamahasiswa.net/api/"
       const val  BASE_URL : String = "https://administrasi.zonamahasiswa.id/api/"
        const val URL_LOGIN_RETROFIT : String = "login"
        const val URL_KIRIM_ABSENSI : String = "${BASE_URL}checklog"
        const val URL_LOGOUT : String ="${BASE_URL}logout"
    }
}