package com.example.friedchickenny

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.google.firebase.firestore.FirebaseFirestore

class Profile : Fragment() {

    // Global Variables
    private lateinit var nameEdit: EditText
    private lateinit var mobileEdit: EditText
    private lateinit var emailEdit: EditText
    private lateinit var addressEdit: EditText
    private lateinit var nameErrorIcon: ImageView
    private lateinit var mobileErrorIcon: ImageView
    private lateinit var emailErrorIcon: ImageView
    private lateinit var addressErrorIcon: ImageView
    private lateinit var nameError: TextView
    private lateinit var mobileError: TextView
    private lateinit var emailError: TextView
    private lateinit var addressError: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//-------------------------- Declaration --------------------------
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        nameEdit = view.findViewById(R.id.nameEdit)
        mobileEdit = view.findViewById(R.id.mobileEdit)
        emailEdit = view.findViewById(R.id.emailEdit)
        addressEdit = view.findViewById(R.id.addressEdit)
        val userId = 1
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("users")
        val saveBtn = view.findViewById<Button>(R.id.saveBtn)
        nameErrorIcon = view.findViewById(R.id.nameErrorIcon)
        mobileErrorIcon = view.findViewById(R.id.mobileErrorIcon)
        emailErrorIcon = view.findViewById(R.id.emailErrorIcon)
        addressErrorIcon = view.findViewById(R.id.addressErrorIcon)
        nameError = view.findViewById(R.id.nameError)
        mobileError = view.findViewById(R.id.mobileError)
        emailError = view.findViewById(R.id.emailError)
        addressError = view.findViewById(R.id.addressError)
        val orderHistoryCard = view.findViewById<CardView>(R.id.orderHistoryCard)

        // Read data from database and display
        collectionRef.whereEqualTo("userID", userId ).get().addOnSuccessListener { documents ->
            for (document in documents) {
                nameEdit.setText(document.getString("name") ?: "")
                mobileEdit.setText(document.getString("contact") ?: "")
                emailEdit.setText(document.getString("email") ?: "")
                addressEdit.setText(document.getString("address") ?: "")
            }
        }

//-------------------------- Error Handling --------------------------
        nameEdit.addTextChangedListener (object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateName(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        mobileEdit.addTextChangedListener (object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateMobile(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        emailEdit.addTextChangedListener (object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateEmail(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        addressEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateAddress(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

//-------------------------- Save Button --------------------------
        // When save is onclick update database
        saveBtn.setOnClickListener {
            // Hide keyboard
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)
            if (validateFields()) {
                // Create a map of fields to update
                val updates = hashMapOf<String, Any>(
                    "name" to nameEdit.text.trim().toString(),
                    "contact" to mobileEdit.text.trim().toString(),
                    "email" to emailEdit.text.trim().toString(),
                    "address" to addressEdit.text.trim().toString()
                )

                collectionRef.whereEqualTo("userID", userId)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            collectionRef.document(document.id)
                                .update(updates)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Saved Successfully", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
            } else {
                Toast.makeText(context, "Please fix the errors", Toast.LENGTH_SHORT).show()
            }
        }

//-------------------------- Save Button --------------------------
        orderHistoryCard.setOnClickListener{
            val intent = Intent(context, OrderHistory::class.java)
            startActivity(intent)
        }

        // Inflate the layout for this fragment
        return view
    }

//-------------------------- Functions --------------------------
    private fun validateFields(): Boolean {
        val nameValid = validateName(nameEdit.text.toString())
        val mobileValid = validateMobile(mobileEdit.text.toString())
        val emailValid = validateEmail(emailEdit.text.toString())
        val addressValid = validateAddress(addressEdit.text.toString())
        return nameValid && mobileValid && emailValid && addressValid
    }

    private fun validateName(name: String):Boolean {
        var result = true
        if (name.isBlank()) {
            nameError.visibility = View.VISIBLE
            nameErrorIcon.visibility = View.VISIBLE
            nameError.setText(R.string.emptyName)
            result = false
        } else if (!name.matches(Regex("^[a-zA-Z ]+$"))) {
            nameError.visibility = View.VISIBLE
            nameErrorIcon.visibility = View.VISIBLE
            nameError.setText(R.string.invalidName)
            result = false
        } else {
            nameError.visibility = View.GONE
            nameErrorIcon.visibility = View.GONE
        }

        return result
    }

    private fun validateMobile(mobile: String): Boolean {
        var result = true
        if (mobile.isBlank()) {
            mobileError.visibility = View.VISIBLE
            mobileErrorIcon.visibility = View.VISIBLE
            mobileError.setText(R.string.emptyMobile)
            result = false
        } else if (!mobile.matches(Regex("^[0-9+\\- ]+$"))) {
            mobileError.visibility = View.VISIBLE
            mobileErrorIcon.visibility = View.VISIBLE
            mobileError.setText(R.string.invalidMobile)
            result = false
        } else {
            mobileError.visibility = View.GONE
            mobileErrorIcon.visibility = View.GONE
        }
        return result
    }

    private fun validateEmail(email: String): Boolean {
        var result = true
        if (email.isBlank()) {
            emailError.visibility = View.VISIBLE
            emailErrorIcon.visibility = View.VISIBLE
            emailError.setText(R.string.emptyEmail)
            result = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError.visibility = View.VISIBLE
            emailErrorIcon.visibility = View.VISIBLE
            emailError.setText(R.string.invalidEmail)
            result = false
        } else {
            emailError.visibility = View.GONE
            emailErrorIcon.visibility = View.GONE
        }
        return result
    }

    private fun validateAddress(address: String): Boolean {
        var result = true
        if (address.isBlank()) {
            addressError.visibility = View.VISIBLE
            addressErrorIcon.visibility = View.VISIBLE
            addressError.setText(R.string.emptyAddress)
            result = false
        } else {
            addressError.visibility = View.GONE
            addressErrorIcon.visibility = View.GONE
        }
        return result
    }


}