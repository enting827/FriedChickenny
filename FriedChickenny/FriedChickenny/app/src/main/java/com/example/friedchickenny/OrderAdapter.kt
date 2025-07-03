package com.example.friedchickenny

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class OrderAdapter(context: Context) : ListAdapter<OrderItem, customViewHolderOrder>(groupDiffCallbackOrder()) {

    // Create view holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): customViewHolderOrder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.order_summary_item_layout, parent, false)
        return customViewHolderOrder(cellForRow)
    }

    // Bind data to view holder
    override fun onBindViewHolder(holder: customViewHolderOrder, position: Int) {
        val item = getItem(position)
        holder.orderQuan.text = "x" +item.quantity.toString()
        holder.orderName.text = item.name
        holder.orderSubtotal.text = item.formattedSubtotal
        holder.orderSelectedChoice.text = item.selectedChoice
    }

} //end myAdapter

// Custom view holder for RecyclerView items
class customViewHolderOrder(view: View): RecyclerView.ViewHolder(view){
    val orderQuan = itemView.findViewById<TextView>(R.id.orderQuan)
    val orderName = itemView.findViewById<TextView>(R.id.orderName)
    val orderSubtotal = itemView.findViewById<TextView>(R.id.orderSubtotal)
    val orderSelectedChoice = itemView.findViewById<TextView>(R.id.orderSelectedChoice)
}

class groupDiffCallbackOrder : DiffUtil.ItemCallback<OrderItem>() {
    // Checks if two item (old and new) have the same ID.
    override fun areItemsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean {
        return oldItem.id == newItem.id
    }

    // Checks if the content of the two item (old and new) are the same.
    override fun areContentsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean {
        return oldItem == newItem
    }
}