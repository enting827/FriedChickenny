package com.example.friedchickenny

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class CartAdapter(context: Context,
                  val subtotalItemsTextView: TextView,
                  val deliveryFeesView: TextView,
                  val taxChargeView: TextView,
                  val totalPriceView: TextView) : ListAdapter<FoodItem, customVHCart>(groupDiffCallbackCart()) {

    // Global Variable
    val collectionRef = FirebaseFirestore.getInstance().collection("cart")

    // Create view holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): customVHCart {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.cart_item_layout, parent, false)
        return customVHCart(cellForRow)
    }

    // Bind data to view holder
    override fun onBindViewHolder(holder: customVHCart, position: Int) {
        val item = getItem(position)

//-------------------------- Display --------------------------
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .centerCrop()
            .override(90, 90)
            .into(holder.cartItemImg)

        holder.cartName.text = item.name
        holder.cartChoice.text = item.selectedChoice
        holder.cartSubtotal.text = item.formattedSubtotal
        holder.cartQuantity.text = item.quantity.toString()

//-------------------------- Button Action --------------------------
        // Increase quantity button click listener
        holder.cartAddBtn.setOnClickListener {
            val updatedList = currentList.toMutableList()
            val currentItem = updatedList[position]
            val newQuantity = CommonUtils.incrementQuantity(currentItem.quantity)
            currentItem.quantity = newQuantity
            notifyItemChanged(position)

            // Update Firestore with new quantity
            updateQuantityInFirestore(currentItem)
        }

        // Decrease quantity button click listener
        holder.cartSubBtn.setOnClickListener {
            val updatedList = currentList.toMutableList()
            val currentItem = updatedList[position]
            val newQuantity = currentItem.quantity - 1

            if (newQuantity > 0) {
                currentItem.quantity = newQuantity
                notifyItemChanged(position)

                // Update Firestore with new quantity
                updateQuantityInFirestore(currentItem)
            } else {
                // Quantity is less than 0, remove item from Firestore and local list
                deleteItem(position)
            }

        }

        holder.cartItemLay.setOnClickListener{
            val intent = Intent(holder.itemView.context, FoodDetails::class.java)
            intent.putExtra("cartItem", item)
            intent.putExtra("from", "cartAdapter")
            holder.itemView.context.startActivity(intent)
        }

    }

//-------------------------- Functions --------------------------
    // For delete item
    fun deleteItem(position: Int) {
        val item = getItem(position)
        updateQuantityInFirestore(item)
        collectionRef.whereEqualTo("id", item.id)
            .whereEqualTo("selectedChoice", item.selectedChoice)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // There should be only one document matching both criteria, so we delete it
                    val document = documents.documents[0]
                    collectionRef.document(document.id)
                        .delete()
                        .addOnSuccessListener {
                            // Remove the item from the local dataset
                            val updatedList = currentList.toMutableList()
                            updatedList.removeAt(position)
                            submitList(updatedList)

                            // Update pricing after item deletion
                            CommonUtils.updatePricing(updatedList, subtotalItemsTextView, deliveryFeesView, taxChargeView, totalPriceView)
                        }
                }
            }
    }

    // Update quantity in Firestore
    fun updateQuantityInFirestore(item: FoodItem) {
        collectionRef.whereEqualTo("id", item.id)
            .whereEqualTo("selectedChoice", item.selectedChoice)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // There should be only one document matching both criteria, so we update it
                    val document = documents.documents[0]
                    collectionRef.document(document.id)
                        .update("quantity", item.quantity)
                        .addOnSuccessListener {
                            // Update pricing after quantity update
                            CommonUtils.updatePricing(currentList, subtotalItemsTextView, deliveryFeesView, taxChargeView, totalPriceView)
                        }
                }
            }
    }


} //end adapter

// Custom view holder for RecyclerView items
class customVHCart(view: View): RecyclerView.ViewHolder(view){
    val cartItemImg = itemView.findViewById<ImageView>(R.id.cartItemImg)
    val cartName = itemView.findViewById<TextView>(R.id.cartName)
    val cartChoice = itemView.findViewById<TextView>(R.id.cartChoice)
    val cartSubtotal = itemView.findViewById<TextView>(R.id.cartSubtotal)
    val cartQuantity = itemView.findViewById<TextView>(R.id.cartQuan)
    val cartSubBtn = itemView.findViewById<ImageButton>(R.id.cartSubBtn)
    val cartAddBtn = itemView.findViewById<ImageButton>(R.id.cartAddBtn)
    val cartItemLay = itemView.findViewById<LinearLayout>(R.id.cartItemLay)

}

class groupDiffCallbackCart : DiffUtil.ItemCallback<FoodItem>() {
    // Checks if two list item (old and new) have the same ID.
    override fun areItemsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
        return oldItem.id == newItem.id
    }

    // Checks if the content of the item (old and new) are the same.
    override fun areContentsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
        return oldItem == newItem
    }
}