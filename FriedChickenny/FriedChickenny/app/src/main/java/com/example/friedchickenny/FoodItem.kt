package com.example.friedchickenny

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FoodItem(
    val id: String, //FoodID
    val imageUrl: String,
    val name: String,
    val description: String,
    val category: String,
    val selections: List<String>,
    val price: Double,
    val recommend: Boolean,
    var selectedChoice: String = "",
    var quantity:Int = 0 //indicate not add to cart yet
) : Parcelable{
    // Format price to 2 decimal places
    val formattedPrice: String
        get() = String.format("RM"+"%.2f", price)

    val formattedSubtotal: String
        get() = String.format("RM"+"%.2f", price*quantity)
}