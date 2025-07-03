package com.example.friedchickenny

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

class OrderListAdapter(context: Context) : ListAdapter<Orders, customViewHolderOL>(groupDiffCallbackOL()) {

    // Create view holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): customViewHolderOL {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.order_history_item_layout, parent, false)
        return customViewHolderOL(cellForRow)
    }

    // Bind data to view holder
    override fun onBindViewHolder(holder: customViewHolderOL, position: Int) {
        val item = getItem(position)
        holder.orderIdTV.text = "Order id: " + item.id
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(item.orderDatetime)
        val formattedDate = SimpleDateFormat("dd MMMM yyyy hh:mm a", Locale.getDefault()).format(date) // Format Date object to desired string format
        holder.orderDateTimeTV.text = formattedDate
        holder.orderTotalPriceTV.text = item.formattedtotalPrice

        // Fix type casting from orderItems (list from database)
        var orderItemList = mutableListOf<OrderItem>()
        if (item.orderItems != null) {
            orderItemList = CommonUtils.fixOrderItem(item.orderItems)
        }

        // Setup display order details
        holder.rvOrderDetails.apply {
            val orderItemAdapter = OrderAdapter(context)
            layoutManager = LinearLayoutManager(context)
            adapter = orderItemAdapter

            orderItemAdapter.submitList(orderItemList)
        }

    }

} //end myAdapter

// Custom view holder for RecyclerView items
class customViewHolderOL(view: View): RecyclerView.ViewHolder(view){
    val orderIdTV = itemView.findViewById<TextView>(R.id.orderId)
    val orderDateTimeTV = itemView.findViewById<TextView>(R.id.orderDateTime)
    val orderTotalPriceTV = itemView.findViewById<TextView>(R.id.orderTotalPrice)
    val rvOrderDetails = itemView.findViewById<RecyclerView>(R.id.rvOrderDetails)


}

class groupDiffCallbackOL : DiffUtil.ItemCallback<Orders>() {
    // Checks if two list item (old and new) have the same id
    override fun areItemsTheSame(oldItem: Orders, newItem: Orders): Boolean {
        return oldItem.id == newItem.id
    }

    // Checks if the content of the two lists (old and new) are the same.
    override fun areContentsTheSame(oldItem: Orders, newItem: Orders): Boolean {
        return oldItem == newItem
    }
}