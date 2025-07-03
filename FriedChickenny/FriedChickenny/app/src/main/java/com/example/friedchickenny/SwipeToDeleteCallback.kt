package com.example.friedchickenny

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class SwipeToDeleteCallback(
    val context: Context,
    val adapter: CartAdapter
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val deletePaint = Paint().apply {
        color = Color.BLACK
        textSize = 48f
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
    }

    private val swipeThreshold = 0.7f // Swipe threshold as 70%

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        // Check if swiped distance is greater than 70% of item width
        val itemView = viewHolder.itemView
        val threshold = itemView.width * swipeThreshold
        val dX = viewHolder.itemView.translationX
        if (Math.abs(dX) > threshold) {
            adapter.deleteItem(position)
        } else {
            adapter.notifyItemChanged(viewHolder.adapterPosition) // Notify adapter to reset view
        }
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = viewHolder.itemView
            val background = Paint()
            background.color = Color.RED
            c.drawRect(
                itemView.right + dX, itemView.top.toFloat(),
                itemView.right.toFloat(), itemView.bottom.toFloat(), background
            )

            if (dX < 0) {
                // Only draw the "Delete" text if the view is swiped to the left (dX < 0)
                c.drawText("Delete", itemView.right - 200f, itemView.top + itemView.height / 2f + 20f, deletePaint)
            }

        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}
