package com.agos.astore.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Message(
    var uid: String? = "",
    var name: String? = "",
    var message: String? = "",
    var date: Long? = 0,
    var own: Boolean = true
) {
}