package com.example.aplikasipresensizmg.helper

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import com.example.aplikasipresensizmg.TampilErrorActivity

class RedirectToTampilErrorActivity(context: Context, message:String, tag:String) {
    init {
        var i : Intent = Intent(context, TampilErrorActivity::class.java)
        i.putExtra("errorMessage",message)
        i.putExtra("tag",tag)
        context.startActivity(i)
    }
}