package com.example.friedchickenny

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.shuhart.stepview.StepView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class Checkout : AppCompatActivity() {

    //Global Variable
    private var orderId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_check_out)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//-------------------------- Declaration --------------------------
        val orderItems = intent.getParcelableArrayListExtra<OrderItem>("orderItems")
        val allSubtotal = intent.getDoubleExtra("allSubtotal", 0.0)
        val deliveryFee = intent.getDoubleExtra("deliveryFee", 0.0)
        val taxCharge = intent.getDoubleExtra("taxCharge", 0.0)
        val totalPrice = intent.getDoubleExtra("totalPrice", 0.0)
        val stepView = findViewById<StepView>(R.id.checkOutProgress)
        val rvOrder = findViewById<RecyclerView>(R.id.rvOrderSum)
        val backBtnC = findViewById<ImageButton>(R.id.backBtnC)
        val subtotalItemsTV = findViewById<TextView>(R.id.subtotalItemsCO)
        val deliveryFeesTV = findViewById<TextView>(R.id.deliveryFeesCO)
        val taxChargeTV = findViewById<TextView>(R.id.taxChargeCO)
        val totalPriceTV = findViewById<TextView>(R.id.totalPriceCO)
        val placeOrderBtn = findViewById<Button>(R.id.placeOrderBtn)
        val remarkEditTxt = findViewById<EditText>(R.id.remarkEditTxt)
        val cutlerySwitch = findViewById<Switch>(R.id.cutleryLabel)
        val userId=1

// ------------ Display recipient & deliver address ------------
        fetchUserDetails(userId)


// ------------ Progress Bar / StepView ------------
        stepView.setSteps(listOf("Menu", "Cart", "Checkout"))// Initialize the StepView

        // Set the current step based on the user's progress
        stepView.go(2, true)  // 0 for Menu, 1 for Cart, 2 for Checkout

//-------------------------- Display Order Summary --------------------------
        //RecyclerView Setup
        rvOrder.layoutManager = LinearLayoutManager(this)
//        // Add divider for the items in RecyclerView
//        rvOrder.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        val orderAdapter = OrderAdapter(this)
        rvOrder.adapter = orderAdapter
        // Submit list to adapter
        orderAdapter.submitList(orderItems)

//-------------------------- Display Pricing --------------------------
        subtotalItemsTV.text= CommonUtils.formatPrice(allSubtotal)
        deliveryFeesTV.text= CommonUtils.formatPrice(deliveryFee)
        taxChargeTV.text= CommonUtils.formatPrice(taxCharge)
        totalPriceTV.text= CommonUtils.formatPrice(totalPrice)

//-------------------------- Action --------------------------
        backBtnC.setOnClickListener{
            finish()
        }

        placeOrderBtn.setOnClickListener{
            //  wait for the Firestore operation to complete and then proceed with starting the Receipt activity with the orderId
            saveOrderToDB(orderItems, allSubtotal, deliveryFee, taxCharge, totalPrice, remarkEditTxt.text.toString(), cutlerySwitch.isChecked, getSelectedPaymentMethod()) { orderId ->
                // Callback function to handle orderId retrieval
                if (orderId != null) {
                    clearCartDB()
                    val intent = Intent(this, Receipt::class.java)
                    intent.putExtra("orderId", orderId)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
//-------------------------- Function --------------------------

    // Function to fetch user details from Firestore
    private fun fetchUserDetails(userId: Int?) {
        val recipientTV = findViewById<TextView>(R.id.recipient)
        val deliveryAddressTV = findViewById<TextView>(R.id.address)

        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("users")

        usersRef.whereEqualTo("userID", userId).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val contact = document.getString("contact") ?: ""
                    val address = document.getString("address") ?: ""

                    // Update UI with fetched data
                    recipientTV.text = "$name | $contact"
                    deliveryAddressTV.text = address
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Checkout", "Error fetching user details: ", exception)
            }
    }


    // Function to clear cart from Firestore
    private fun clearCartDB(){
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("cart")

        collectionRef.get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    collectionRef.document(document.id).delete()
                }
            }
    }


    // Function to save order to Firestore
    private fun saveOrderToDB(orderItems: ArrayList<OrderItem>?, allSubtotal: Double, deliveryFee: Double, taxCharge: Double, totalPrice: Double, remark: String, cutlery: Boolean, paymentMethod: String, callback: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val orderRef = db.collection("orders")

        val orderData = hashMapOf(
            "orderDatetime" to getCurrentDatetime(),
            "orderItems" to orderItems,
            "allSubtotal" to allSubtotal,
            "deliveryFee" to deliveryFee,
            "taxCharge" to taxCharge,
            "totalPrice" to totalPrice,
            "remark" to remark,
            "cutlery" to cutlery,
            "paymentMethod" to paymentMethod,
            "completed" to false
        )

        orderRef.add(orderData)
            .addOnSuccessListener { documentReference ->
                orderId = documentReference.id
                callback(orderId) // Invoke the callback with orderId
            }
    }

    // Function to get current datetime for order datetime
    private fun getCurrentDatetime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        return sdf.format(Date())
    }

    // Function to get selected payment method
    private fun getSelectedPaymentMethod(): String {
        return when (findViewById<RadioGroup>(R.id.payRadio).checkedRadioButtonId) {
            R.id.tngRadio -> getString(R.string.TNGRadio)
            R.id.creditDebitRadio -> getString(R.string.creditDebitRadio)
            R.id.onlineBankRadio -> getString(R.string.onlineBankRadio)
            else -> ""
        }
    }
}