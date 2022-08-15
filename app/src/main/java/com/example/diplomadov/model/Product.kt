package com.example.diplomadov.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
data class User(
    var id: String,
    var name: String,
    var email: String,
    var token: String = "",
    var profile: Int = 0
) : Serializable {
    constructor() : this("", "", "", "", 0)
}