package com.example.friedchickenny

data class Orders(
    val id: String, //document id from database
    val orderDatetime: String,
    val orderItems: List<Map<String, Any>>?,
    val allSubtotal: Double,
    val deliveryFee: Double,
    val taxCharge: Double,
    val totalPrice: Double,
    val remark: String,
    val cutlery: Boolean,
    val paymentMethod: String,
    val completed: Boolean
){
    val formattedtotalPrice: String
        get() = "RM"+String.format("%.2f", totalPrice)
}