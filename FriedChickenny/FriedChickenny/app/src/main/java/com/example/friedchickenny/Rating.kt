package com.example.friedchickenny

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FirebaseFirestore

class Rating : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//-------------------------- Declaration --------------------------
        val view = inflater.inflate(R.layout.fragment_rating_dialog, container, false)
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val submitButton = view.findViewById<Button>(R.id.submitButton)
        val feedbackEditText = view.findViewById<TextView>(R.id.feedbackEditText)
        val closeBtn = view.findViewById<ImageButton>(R.id.closeBtn)
        val errorMsgIcon = view.findViewById<ImageView>(R.id.errorMsgIconR)
        val errorMsg = view.findViewById<TextView>(R.id.errorMsgRate)

//-------------------------- Button Action --------------------------
        // When close button is clicked, go to home page
        closeBtn.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
            dismiss()
        }

        // When submit button is clicked,
        submitButton.setOnClickListener {
            val rating = ratingBar.rating
            val feedback = feedbackEditText.text.toString()

            if (rating == 0F){
                errorMsgIcon.setImageResource(R.drawable.iconerror)
                errorMsg.setText(R.string.errorMsgRate)
            } else {
                // save to database
                val db = FirebaseFirestore.getInstance()
                val feedbackRef = db.collection("feedback")

                val feedbackItem = hashMapOf(
                    "rating" to rating,
                    "feedback" to feedback
                )

                feedbackRef.add(feedbackItem)
                    .addOnSuccessListener { documentReference ->
                        // Go Back to Home Page
                        val intent = Intent(context, MainActivity::class.java)
                        startActivity(intent)
                        dismiss()
                    }
            }
        }

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), theme)

        // Set the content view to rating layout if needed
        dialog.setContentView(R.layout.fragment_rating_dialog)

        // Get the window parameters and adjust width
        val window = dialog.window
        val layoutParams = window?.attributes
        layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
        window?.attributes = layoutParams

        // Optional: Add onBackPressed behavior
        dialog.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                // If user click on back, go to home page
                val intent = Intent(context, MainActivity::class.java)
                startActivity(intent)
                dismiss()
                true
            } else {
                false
            }
        }

        return dialog
    }
}
