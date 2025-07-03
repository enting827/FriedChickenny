package com.example.friedchickenny

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.shuhart.stepview.StepView
import org.w3c.dom.Text

class Cart : Fragment() {
//    Global Variables
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()// Get a Firestore instance
    val collectionRef = db.collection("cart") // Access a collection
    lateinit var subtotalItemsTextView: TextView
    lateinit var deliveryFeesView: TextView
    lateinit var taxChargeView: TextView
    lateinit var totalPriceView: TextView
    lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//-------------------------- Declaration --------------------------
        val view = inflater.inflate(R.layout.fragment_cart, container, false) // Inflate the layout for this fragment
        // Disable Main Nav
        val showMainNav = arguments?.getBoolean("showMainNav", true) ?: true
        val navView = activity?.findViewById<BottomNavigationView>(R.id.mainNav)

        val stepView = view.findViewById<StepView>(R.id.cartProgress)
        val rvCart = view.findViewById<RecyclerView>(R.id.rvCart)
        val backBtn = view.findViewById<ImageButton>(R.id.backBtnC)
        val addItem = view.findViewById<TextView>(R.id.addMoreCart)
        val checkOutBtn = view.findViewById<Button>(R.id.checkOutBtn)
        val emptyCartMssg = view.findViewById<TextView>(R.id.emptyCartMssg)

//-------------------------- Display --------------------------
    // ------------ Disable Main Nav (bottom navigation menu)------------
        if (navView != null) {
            navView.visibility = if (showMainNav) View.VISIBLE else View.GONE
        }

    // ------------ Progress Bar / StepView ------------
        stepView.setSteps(listOf("Menu", "Cart", "Checkout"))// Initialize the StepView

        // Set the current step based on the user's progress
        stepView.go(1, true)  // 0 for Menu, 1 for Cart, 2 for Checkout

    // ------------ Cart List & Subtotal------------
        // Initialize RecyclerView
        rvCart.layoutManager = LinearLayoutManager(requireContext())
        rvCart.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        // Pricing TextView
        subtotalItemsTextView = view.findViewById(R.id.subtotalItems)
        deliveryFeesView = view.findViewById(R.id.deliveryFees)
        taxChargeView = view.findViewById(R.id.taxCharge)
        totalPriceView = view.findViewById(R.id.totalPrice)

        // Initialize CartAdapter
        cartAdapter = CartAdapter(requireContext(), subtotalItemsTextView, deliveryFeesView, taxChargeView, totalPriceView)
        rvCart.adapter = cartAdapter

//-------------------------- Delete Item From Cart --------------------------
        // Attach ItemTouchHelper to RecyclerView
        val swipeToDeleteCallback = SwipeToDeleteCallback(requireContext(), cartAdapter)
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(rvCart)

//-------------------------- Display Cart List & Pricing -----------------
        getCartList { cartList ->
            // If empty cart, display message
            if (cartList.isEmpty()) {
                // Show empty cart message
                emptyCartMssg.visibility = View.VISIBLE
                emptyCartMssg.text = getString(R.string.emptyCartMssg)
            } // Fetch cart items and update RecyclerView
            else {
                cartAdapter.submitList(cartList)
                CommonUtils.updatePricing(cartList, subtotalItemsTextView, deliveryFeesView, taxChargeView, totalPriceView)
            }

        }

//-------------------------- Button Action --------------------------
        // Navigate back to the home page when backBtn is clicked
        backBtn.setOnClickListener {
            val mainActivity = requireActivity() as MainActivity
            mainActivity.replaceFragment(Home())
        }

        addItem.setOnClickListener{
            val mainActivity = requireActivity() as MainActivity
            mainActivity.replaceFragment(Menu())
        }

        checkOutBtn.setOnClickListener{
            if (cartAdapter.itemCount > 0) {
                val cartItems = cartAdapter.currentList
                val allSubtotal = CommonUtils.calcAllSubtotal(cartItems)
                val deliveryFee = CommonUtils.getDeliveryFees()
                val taxCharge = CommonUtils.getTaxCharge(allSubtotal)
                val totalPrice = CommonUtils.getTotalPrice(allSubtotal, deliveryFee, taxCharge)

                val intent = Intent(context, Checkout::class.java)
                intent.putExtra("orderItems", getOrderItemList(cartItems))
                intent.putExtra("allSubtotal", allSubtotal)
                intent.putExtra("deliveryFee", deliveryFee)
                intent.putExtra("taxCharge", taxCharge)
                intent.putExtra("totalPrice", totalPrice)
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Your cart is hungry! Add some items first.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Ensure the BottomNavigationView is visible again when the fragment is destroyed
        val navView = activity?.findViewById<BottomNavigationView>(R.id.mainNav)
        navView?.visibility = View.VISIBLE
    }


    private fun getCartList(onSuccess: (List<FoodItem>) -> Unit) {
        val carts = mutableListOf<FoodItem>()
        collectionRef.get().addOnSuccessListener { documents ->
            for (document in documents) {
                val cartItem = FoodItem(
                    document.getString("id") ?: "",
                    document.getString("imageUrl") ?: "",
                    document.getString("name") ?: "",
                    document.getString("description") ?: "",
                    document.getString("category") ?: "",
                    document.get("selections") as? List<String> ?: listOf(),
                    document.getDouble("price") ?: 0.0,
                    document.getBoolean("recommend") ?: false,
                    document.getString("selectedChoice") ?: "",
                    document.getLong("quantity")?.toInt() ?: 0
                )
                carts.add(cartItem)
            }
            carts.sortBy { it.id }
            onSuccess(carts)
        }
    }

    private fun getOrderItemList(cartItems:List<FoodItem>): ArrayList<OrderItem> {
        val orderItems = arrayListOf<OrderItem>()

        for (cartItem in cartItems) {
            val orderItem = OrderItem(
                cartItem.id,
                cartItem.name,
                cartItem.selectedChoice,
                cartItem.quantity,
                cartItem.price,
                cartItem.price * cartItem.quantity // Subtotal for this item
            )
            orderItems.add(orderItem)
        }

        return orderItems

    }

}