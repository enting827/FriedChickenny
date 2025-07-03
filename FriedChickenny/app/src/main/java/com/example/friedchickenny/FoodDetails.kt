package com.example.friedchickenny

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.CompoundButtonCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class FoodDetails : AppCompatActivity() {

//    Global Variables
    lateinit var foodItem: FoodItem
    var quantity = 0
    var selectedChoice = ""
    var from = ""
    var fromCart = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_food_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


//-------------------------- Declaration --------------------------
        from = intent.getStringExtra("from")!!
        if (from ==  "menuAdapter"){
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            foodItem = intent.getParcelableExtra("menuItem")!! // Extract MenuItem from Intent extras
        } else if (from ==  "cartAdapter"){
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            foodItem = intent.getParcelableExtra("cartItem")!!
        }

        fromCart =  if (from == "cartAdapter") true else false

        val foodImgDet = findViewById<ImageView>(R.id.foodImgDet)
        val itemName = findViewById<TextView>(R.id.itemName)
        val itemDescr = findViewById<TextView>(R.id.itemDescr)
        val itemPrice = findViewById<TextView>(R.id.itemPrice)
        val selectionTitle = findViewById<TextView>(R.id.selectTitle)
        val selectionRadio = findViewById<RadioGroup>(R.id.selectionRadio)
        val idToSelectionMap = HashMap<Int, String>() //to know which selection user select as we auto generate id for radio button
        val color = ContextCompat.getColor(this, R.color.orange) //for radio button
        val quantityTxt = findViewById<TextView>(R.id.quantityTxt)
        val backBtn = findViewById<ImageButton>(R.id.backBtn)
        val addBtn = findViewById<ImageButton>(R.id.addBtn)
        val subBtn = findViewById<ImageButton>(R.id.subtractBtn)
        val addToCartButton = findViewById<Button>(R.id.addToCartBtn)
        quantity = if (fromCart) foodItem.quantity else 1
        val errorMsgIcon = findViewById<ImageView>(R.id.errorMsgIcon)
        val errorMsgSelect = findViewById<TextView>(R.id.errorMsgSelect)


//-------------------------- Display Data --------------------------
        // Load data to view
        Glide.with(this)
            .load(foodItem.imageUrl)
            .centerCrop()
            .into(foodImgDet)

        itemName.text = foodItem.name
        itemDescr.text = foodItem.description
        itemPrice.text = foodItem.formattedPrice
        selectionTitle.text = if (foodItem.category.lowercase() == "drink") getString(R.string.iceTitle) else getString(R.string.flavourTitle)
        quantityTxt.text = quantity.toString()

        // Dynamically add RadioButtons to the RadioGroup based on selections
        foodItem.selections.forEach { selection ->
            val radioButton = RadioButton(this).apply {
                text = selection
                id = View.generateViewId()

                // Set button tint color
                CompoundButtonCompat.setButtonTintList(this, ColorStateList.valueOf(color))

                // Set text size
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)

                // Set margins
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    // Set margins in dp
                    val marginDp = 10
                    val marginPx = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        marginDp.toFloat(),
                        resources.displayMetrics
                    ).toInt()
                    setMargins(0, marginPx, 0, marginPx)
                }
                layoutParams = params

                // Set padding to increase space between button and text
                val paddingDp = 10
                val paddingPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    paddingDp.toFloat(),
                    resources.displayMetrics
                ).toInt()
                setPadding(paddingPx, 0, 0, 0)

            }
            idToSelectionMap[radioButton.id] = selection  // Store the mapping
            selectionRadio.addView(radioButton)
        }

        // Check the appropriate RadioButton based on selectedChoice
        val radioId = foodItem.selections.indexOfFirst { it == foodItem.selectedChoice }
        if (radioId != -1) {
            selectionRadio.check(selectionRadio.getChildAt(radioId).id)
            selectedChoice = foodItem.selectedChoice
        }

//-------------------------- Button Action --------------------------
        // Click on Radio Button
        selectionRadio.setOnCheckedChangeListener { group, checkedId ->
            selectedChoice = idToSelectionMap[checkedId].toString()
        }

        // Navigate back to the previous activity when backBtn is clicked
        backBtn.setOnClickListener(){
            finish()
        }

        // Increment quantity when addBtn is clicked
        addBtn.setOnClickListener {
            quantity = quantityTxt.text.toString().toInt()
            quantity = CommonUtils.incrementQuantity(quantity)
            CommonUtils.updateQuantityText(quantityTxt, quantity)
        }

        // Decrement quantity when subBtn is clicked
        subBtn.setOnClickListener {
            quantity = quantityTxt.text.toString().toInt()
            quantity = CommonUtils.decrementQuantity(quantity)
            CommonUtils.updateQuantityText(quantityTxt, quantity)
        }

        addToCartButton.setOnClickListener(){
            if (selectedChoice==""){
                errorMsgIcon.setImageResource(R.drawable.iconerror)
                errorMsgSelect.setText(R.string.errorMsgSelect)
            }
            else {
                addToCart(foodItem, quantity, selectedChoice)
            }

        }

    }

    private fun addToCart(item: FoodItem, quantity: Int, selectedChoice:String) {
        val db = FirebaseFirestore.getInstance()
        val cartCollection = db.collection("cart")

        // Remove item when user access from cart page and selectedChoice changed
        if (fromCart && selectedChoice != foodItem.selectedChoice) {
            cartCollection.whereEqualTo("id", item.id).whereEqualTo("selectedChoice", foodItem.selectedChoice)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        for (document in querySnapshot.documents) {
                            // Delete the existing item
                            cartCollection.document(document.id)
                                .delete()
                        }
                    }
                }
        }

        // Query the cart to see if the item already exists
        cartCollection.whereEqualTo("id", item.id).whereEqualTo("selectedChoice", selectedChoice)
            .get()
            .addOnSuccessListener { querySnapshot  ->
                // The item does not exist in the cart, add it
                if (querySnapshot.isEmpty) {
                    foodItem.quantity = quantity
                    foodItem.selectedChoice = selectedChoice

                    foodItem?.let {
                        cartCollection.add(it)
                            .addOnSuccessListener {documentReference ->
                                Toast.makeText(this, "Add To Cart Successfully", Toast.LENGTH_SHORT).show()
                                endActivity()
                            }
                    }
                } else {
                    // The item already exists in the cart, update the quantity
                    for (document in querySnapshot.documents) {
                        val data = document.data
                        if (data != null){
                            val existingCartItem = cartItemFromDB(data)

                            // Determine the new quantity to be updated in the cart.
                            // If the item is being added from a non-cart context (e.g., menu) / selected choice different (from cart page), add the current quantity to the existing quantity.
                            // If the item is being updated from the cart itself, set the new quantity to the provided quantity directly.
                            val newQuantity = if (!fromCart || selectedChoice != foodItem.selectedChoice) existingCartItem?.quantity?.plus(quantity) ?: quantity else quantity

                            if (existingCartItem != null) {
                                // Update the existing document with new quantity
                                cartCollection.document(document.id)
                                    .update("quantity", newQuantity)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Update Successfully", Toast.LENGTH_SHORT).show()
                                        endActivity()
                                    }
                            }
                        }
                    }
                }
            }
    }

    private fun createCartItem(item: FoodItem): FoodItem? {
        return FoodItem(
            id = item.id,
            imageUrl = item.imageUrl,
            name = item.name,
            description = item.description,
            category = item.category,
            selections = item.selections,
            price = item.price,
            recommend = item.recommend,
            selectedChoice = selectedChoice,
            quantity = quantity
        )

    }

    private fun cartItemFromDB(data: MutableMap<String, Any>?): FoodItem? {
        if (data == null) return null
        return FoodItem(
            id = data["id"] as String,
            imageUrl = data["imageUrl"] as String,
            name = data["name"] as String,
            description = data["description"] as String,
            category = data["category"] as String,
            selections = data["selections"] as List<String>,
            price = data["price"] as Double,
            recommend = data["recommend"] as Boolean,
            selectedChoice = data["selectedChoice"] as String,
            quantity = (data["quantity"] as Long).toInt()
        )
    }

    private fun endActivity(){
        if (fromCart){
            // If it's an edit from the cart page, display cart page again so that can refresh the cart page
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("showCart", true)
            startActivity(intent)
            finish()
        }
        else {
            // Refresh Menu to show indicator
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("showMenu", true)
            startActivity(intent)
            finish()
        }
    }

}
