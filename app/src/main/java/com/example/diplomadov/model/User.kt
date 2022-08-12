package com.example.diplomadov.model

class User(
    var id: String,
    var name: String,
    var email: String,
    var token: String = "",
    var profile: Int = 0
) {
    constructor() : this("", "", "", "", 0)
}