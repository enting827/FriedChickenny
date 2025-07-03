package com.example.friedchickenny

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class MenuAdapter(context: Context) : ListAdapter<FoodItem, customVHMenu>(groupDiffCallbackMenu()) {
    // Create view holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): customVHMenu {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.menu_item_layout, parent, false)
        return customVHMenu(cellForRow)
    }

    // Bind data to view holder
    override fun onBindViewHolder(holder: customVHMenu, position: Int) {
        val item = getItem(position)
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .centerCrop()
            .override(80, 80)
            .into(holder.foodImg)
        holder.foodName.text = item.name
        holder.foodPrice.text = item.formattedPrice
        updateQuantityOrdered(item.id, holder.quantityOrdered)

        // When user click on the item,
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, FoodDetails::class.java)
            intent.putExtra("menuItem", item)
            intent.putExtra("from", "menuAdapter")
            holder.itemView.context.startActivity(intent)
        }
    }

    private fun updateQuantityOrdered(itemId: String, quantityOrderedTextView: TextView) {
        val db = FirebaseFirestore.getInstance()
        val cartCollection = db.collection("cart")

        // Query to sum up quantities for the item ID
        cartCollection.whereEqualTo("id", itemId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                var totalQuantity = 0
                for (document in querySnapshot.documents) {
                    val quantity = document.getLong("quantity") ?: 0
                    totalQuantity += quantity.toInt()
                }

                if (totalQuantity > 0){
                    quantityOrderedTextView.text = "x$totalQuantity"
                    quantityOrderedTextView.setBackgroundResource(R.drawable.rounded_shape)
                }
            }
    }

} //end

// Custom view holder for RecyclerView items
class customVHMenu(view: View): RecyclerView.ViewHolder(view){
    val foodImg = itemView.findViewById<ImageView>(R.id.foodImg)
    val foodName = itemView.findViewById<TextView>(R.id.foodName)
    val foodPrice = itemView.findViewById<TextView>(R.id.foodPrice)
    val quantityOrdered = itemView.findViewById<TextView>(R.id.quantityOrdered)
}

class groupDiffCallbackMenu : DiffUtil.ItemCallback<FoodItem>() {
    // Checks if two list item (old and new) have the same ID.
    override fun areItemsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
        return oldItem.id == newItem.id
    }

    // Checks if the content of the item (old and new) are the same.
    override fun areContentsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
        return oldItem == newItem
    }
}