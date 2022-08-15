package com.example.diplomadov.model

import java.io.Serializable

data class User(
    var id: String,
    var name: String,
    var email: String,
    var token: String = "",
    var profile: Int = 0
) : Serializable {
    constructor() : this("", "", "", "", 0)
}
