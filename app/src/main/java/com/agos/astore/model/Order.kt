package com.agos.astore.model

import java.io.Serializable
import java.util.*

data class Order(
    var id: String? = "",
    var userId: String,
    var date: Date,
    var total: Double = 0.0,
    var status: Int = 0,
    var details: MutableList<Detail> = mutableListOf()
) : Serializable {
    constructor() : this("", "", Date(), 0.0, 0, mutableListOf())
}