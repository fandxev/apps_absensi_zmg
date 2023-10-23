package com.example.aplikasipresensizmg

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate

class TampilErrorActivity : AppCompatActivity() {
    var errorMessage:String? = null
    var tag:String? = null
    lateinit var txt_error_message: TextView
    lateinit var txt_error_tag: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tampil_error)
        errorMessage =  intent.getStringExtra("errorMessage")
        tag = intent.getStringExtra("tag")
        Log.d("fandyDebugError","error message: $errorMessage \n tag = $tag")
        val lihat_mode: Button = findViewById(R.id.btn_lihat_mode)
        txt_error_message = findViewById(R.id.txt_error_message)
        txt_error_tag = findViewById(R.id.txt_error_tag)
        txt_error_message.text = errorMessage
        txt_error_tag.text = tag
        lihat_mode.setOnClickListener {
            checkLightModeOrDarkMode()
        }
        val btn_error_kembali: Button = findViewById(R.id.btn_error_kembali)
        btn_error_kembali.setOnClickListener{
            onBackPressed()
        }
    }

    fun checkLightModeOrDarkMode(){
        Toast.makeText(this@TampilErrorActivity,"checkLightModeOrDarkMode()",Toast.LENGTH_LONG).show()


        when (this@TampilErrorActivity.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                Toast.makeText(this@TampilErrorActivity,"mode gelap",Toast.LENGTH_LONG).show()
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                Toast.makeText(this@TampilErrorActivity,"mode terang",Toast.LENGTH_LONG).show()
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                Toast.makeText(this@TampilErrorActivity,"undefined",Toast.LENGTH_LONG).show()

            }
        }

    }
}