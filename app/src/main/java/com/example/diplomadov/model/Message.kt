package com.example.diplomadov.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Message(
    var uid: String? = "",
    var name: String? = "",
    var message: String? = "",
    var date: Long? = 0,
    var own: Boolean = true
) {


    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "message" to message,
            "date" to date,
        )
    }
}