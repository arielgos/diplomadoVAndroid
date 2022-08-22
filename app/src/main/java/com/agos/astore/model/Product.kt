package com.agos.astore.model

import java.io.Serializable

data class Product(
    var id: String,
    var name: String,
    var description: String,
    var tags: String = "",
    var image: String = "",
    var price: Double = 0.0,
    var status: Boolean = false
) : Serializable {
    constructor() : this("", "", "", "", "", 0.0, false)
}