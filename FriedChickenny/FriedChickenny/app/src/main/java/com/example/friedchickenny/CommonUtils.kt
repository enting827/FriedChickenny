package com.example.friedchickenny

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

object CommonUtils {

    fun setupMenuRv(context: Context?, view: View, recyclerViewId: Int, items: List<FoodItem>) {
        val recyclerView = view.findViewById<RecyclerView>(recyclerViewId)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        val adapter = MenuAdapter(context!!)
        recyclerView.adapter = adapter
        adapter.submitList(items)
    }

    fun incrementQuantity(currentQuantity: Int): Int {
        return currentQuantity + 1
    }

    fun decrementQuantity(currentQuantity: Int): Int {
        return if (currentQuantity > 1) {
            currentQuantity - 1
        } else {
            1 // Ensure quantity doesn't go negative
        }
    }

    fun updateQuantityText(quantityTxt: TextView, quantity: Int) {
        quantityTxt.text = quantity.toString()
    }

    fun updatePricing(cartList: List<FoodItem>, subtotalItemsTextView: TextView, deliveryFeesView: TextView, taxChargeView: TextView, totalPriceView: TextView) {
        var subtotalItems = calcAllSubtotal(cartList)
        // Subtotal
        subtotalItemsTextView.text = "RM${String.format("%.2f", subtotalItems)}"

        // Delivery Fees
        val deliveryFees = getDeliveryFees()
        deliveryFeesView.text = "RM"+String.format("%.2f", deliveryFees)

        // Tax
        val taxCharge = getTaxCharge(subtotalItems)
        taxChargeView.text = "RM"+String.format("%.2f",  taxCharge)

        // Total Price
        totalPriceView.text = "RM"+String.format("%.2f",  getTotalPrice(subtotalItems, deliveryFees, taxCharge))
    }

    fun calcAllSubtotal (cartList: List<FoodItem>): Double {
        var subtotalItems = 0.0
        for (item in cartList) {
            subtotalItems += item.price * item.quantity
        }

        return subtotalItems
    }

    fun getDeliveryFees(): Double{
        return 3.0
    }

    fun getTaxCharge(subtotalItems:Double): Double{
        return 0.06 * subtotalItems
    }

    fun getTotalPrice(subtotalItems:Double, deliveryFees:Double, taxCharge:Double): Double{
        return subtotalItems + deliveryFees + taxCharge
    }

    fun formatPrice (price: Double?): String{
        return "RM" + String.format("%.2f", price)
    }

    fun fixOrderItem(orderItems: List<Map<String, Any>>): MutableList<OrderItem> {
        var orderItemList = mutableListOf<OrderItem>()

        for (item in orderItems) {
            val orderItem = OrderItem(
                id = item["id"] as String,
                name = item["name"] as String,
                selectedChoice = item["selectedChoice"] as String,
                quantity = (item["quantity"] as Long).toInt(),
                price = item["price"] as Double,
                subtotal = item["subtotal"] as Double
            )
            orderItemList.add(orderItem)
        }

        return orderItemList
    }
}