package com.agos.astore.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Cart(
    var productId: String? = "",
    var productName: String? = "",
    var productImage: String? = "",
    var message: String? = "",
    var price: Double? = 0.0,
    var quantity: Int? = 0,
    var total: Double? = 0.0
) {

}