package com.example.friedchickenny

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderItem(
    val id: String, //FoodID
    val name: String,
    val selectedChoice: String,
    val quantity:Int,
    val price: Double,
    val subtotal: Double
):Parcelable{
    val formattedSubtotal: String
        get() = "RM" + String.format("%.2f", subtotal)

}
