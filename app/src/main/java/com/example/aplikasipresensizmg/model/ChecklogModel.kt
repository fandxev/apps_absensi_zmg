package com.example.aplikasipresensizmg.model

import com.google.gson.annotations.SerializedName

data class ChecklogModel(
    @field:SerializedName("status")
    val status: String? = null,

    @field:SerializedName("message")
    val message: String? = null,

    )
