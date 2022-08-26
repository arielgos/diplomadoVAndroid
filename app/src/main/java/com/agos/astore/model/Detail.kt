package com.agos.astore.model

import java.io.Serializable
import java.util.*

data class Detail(
    var productId: String? = "",
    var productName: String? = "",
    var productImage: String? = "",
    var price: Double = 0.0,
    var quantity: Int? = 0,
    var total: Double? = 0.0
) : Serializable {
    constructor() : this("", "", "", 0.0, 0, 0.0)
}