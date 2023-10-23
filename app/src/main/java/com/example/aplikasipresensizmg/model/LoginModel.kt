package com.example.aplikasipresensizmg.model

class LoginModel {
    var status: Int = 0
     var message: String = ""
     var access_token: String = ""
     var token_type: String = ""
     lateinit var user : UserModel
}