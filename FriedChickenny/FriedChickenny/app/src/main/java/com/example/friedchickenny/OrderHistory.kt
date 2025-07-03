package com.example.friedchickenny

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class OrderHistory : AppCompatActivity() {

    //Global Variable
    private lateinit var orderListAdapter: OrderListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_order_history)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backBtnOH = findViewById<ImageButton>(R.id.backBtnOH)

        // Display Order History
        displayOrderHistory()

        backBtnOH.setOnClickListener {
            finish()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.rvOrderHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)
        orderListAdapter = OrderListAdapter(this)
        recyclerView.adapter = orderListAdapter

    }

    private fun displayOrderHistory() {
        val db = FirebaseFirestore.getInstance()
        val ordersRef = db.collection("orders")

        ordersRef.get()
            .addOnSuccessListener { documents ->
                val orderList = mutableListOf<Orders>()
                for (document in documents) {
                    val order = Orders(
                        id = document.id,
                        orderDatetime = document.getString("orderDatetime") ?: "",
                        orderItems = document.get("orderItems") as? List<Map<String, Any>>,
                        allSubtotal = document.getDouble("allSubtotal") ?: 0.0,
                        deliveryFee = document.getDouble("deliveryFee") ?: 0.0,
                        taxCharge = document.getDouble("taxCharge") ?: 0.0,
                        totalPrice = document.getDouble("totalPrice") ?: 0.0,
                        remark = document.getString("remark") ?: "",
                        cutlery = document.getBoolean("cutlery") ?: false,
                        paymentMethod = document.getString("paymentMethod") ?: "",
                        completed = document.getBoolean("completed") ?: false
                    )
                    orderList.add(order)
                }
                orderList.sortByDescending  { it.orderDatetime }
                orderListAdapter.submitList(orderList)
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                // For example, log the error or show a toast
            }
    }
}