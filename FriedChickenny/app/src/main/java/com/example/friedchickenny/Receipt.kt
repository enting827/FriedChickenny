package com.example.friedchickenny

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.ncorti.slidetoact.SlideToActView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Receipt : AppCompatActivity() {

    // Global Variable
    private lateinit var orderAdapter: OrderAdapter
    private val db = FirebaseFirestore.getInstance()
    private lateinit var orderRef: DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_receipt)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//-------------------------- Declaration --------------------------
        val orderId = intent.getStringExtra("orderId")
        val rvOrderDet = findViewById<RecyclerView>(R.id.rvOrderDet)
        val orderReceiveSlider = findViewById<com.ncorti.slidetoact.SlideToActView>(R.id.orderReceiveSlider)
        val backBtnR = findViewById<ImageButton>(R.id.backBtnR)
        orderRef = db.collection("orders").document(orderId!!)

//-------------------------- Display --------------------------
        orderId?.let {
            fetchOrderDetails(it)
        }

        // Initialize the RecyclerView and adapter
        rvOrderDet.layoutManager = LinearLayoutManager(this)
        orderAdapter = OrderAdapter(this)
        rvOrderDet.adapter = orderAdapter

//-------------------------- Actions --------------------------
        backBtnR.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        orderReceiveSlider.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(view: SlideToActView) {
                // Update the 'completed' field to true
                orderRef.update("completed", true)
                // Allow user to rate (a pop out)
                val dialog = Rating()
                dialog.show(supportFragmentManager, "Rating")
            }
        }
    }

//-------------------------- Function --------------------------
    private fun fetchOrderDetails(orderId: String) {
        val orderRef = db.collection("orders").document(orderId)

        orderRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val orderDatetime = document.getString("orderDatetime")
                    val allSubtotal = document.getDouble("allSubtotal")
                    val deliveryFee = document.getDouble("deliveryFee")
                    val taxCharge = document.getDouble("taxCharge")
                    val totalPrice = document.getDouble("totalPrice")
                    val remark = document.getString("remark")
                    val cutlery = document.getBoolean("cutlery")
                    val paymentMethod = document.getString("paymentMethod")
                    val orderItems = document.get("orderItems") as? List<Map<String, Any>>

                    var orderItemList = mutableListOf<OrderItem>()
                    // Fix type casting from orderItems (list from database)
                    if (orderItems != null) {
                        orderItemList = CommonUtils.fixOrderItem(orderItems)
                    }

                    // Update UI with order details
                    findViewById<TextView>(R.id.eta).text = orderDatetime?.let { getEta(it) }
                    findViewById<TextView>(R.id.subtotalItemsR).text = CommonUtils.formatPrice(allSubtotal)
                    findViewById<TextView>(R.id.deliveryFeesR).text = CommonUtils.formatPrice(deliveryFee)
                    findViewById<TextView>(R.id.taxChargeR).text = CommonUtils.formatPrice(taxCharge)
                    findViewById<TextView>(R.id.totalR).text = CommonUtils.formatPrice(totalPrice)
                    val remarkTV = findViewById<TextView>(R.id.remark)
                    if (remark!=""){
                        remarkTV.text = remark
                        remarkTV.visibility = View.VISIBLE
                    }
                    findViewById<TextView>(R.id.cutleryR).text = if (cutlery == true) "Yes" else "No"
                    findViewById<TextView>(R.id.payMethodR).text = paymentMethod
                    orderAdapter.submitList(orderItemList)
                }
            }
    }

    private fun getEta(orderDatetime: String): String {
        // Define the input and output date formats
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        // Parse the input date string to a Date object
        val date = inputFormat.parse(orderDatetime)

        // Create a Calendar object and set the time to the parsed date
        val calendar = Calendar.getInstance()
        if (date != null) {
            calendar.time = date
        }

        // Add 30 minutes to the calendar time
        calendar.add(Calendar.MINUTE, 30)

        // Format the new time to the desired output format
        return outputFormat.format(calendar.time)
    }

}