package com.example.friedchickenny

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    lateinit var navView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


//----------------Menu----------------
        navView = findViewById(R.id.mainNav)

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navHome -> {
                    replaceFragment(Home())
                    true
                }
                R.id.navMenu -> {
                    replaceFragment(Menu())
                    true
                }
                R.id.navCart -> {
                    checkIncompleteOrder()
                    true
                }
                R.id.navProfile -> {
                    replaceFragment(Profile())
                    true
                }
                else -> false
            }
        }

        // Go to cart page, if showCart is true (from FoodDetails)
        if (intent.getBooleanExtra("showCart", false)) {
            navView.selectedItemId = R.id.navCart
        } // Go to menu page, if showMenu is true (from FoodDetails)
        else if (intent.getBooleanExtra("showMenu", false)) {
            navView.selectedItemId = R.id.navMenu
        }// Set the default fragment
        else if (savedInstanceState == null) {
            navView.selectedItemId = R.id.navHome
        }


    } // end onCreate


    fun replaceFragment(fragment: Fragment) {
        // Pass the BottomNavigationView reference to the fragment
        val bundle = Bundle()
        bundle.putBoolean("showMainNav", fragment !is Cart)
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.pageContent, fragment)
            .addToBackStack(null)
            .commit()


    }

    fun checkIncompleteOrder() {
        val db = FirebaseFirestore.getInstance()
        val orderRef = db.collection("orders")

        orderRef.whereEqualTo("completed", false).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // No incomplete orders, navigate to Cart fragment
                    replaceFragment(Cart())
                } else {
                    // Incomplete order found, navigate to Receipt activity
                    for (document in documents) {
                        val orderId = document.id
                        val intent = Intent(this, Receipt::class.java)
                        intent.putExtra("orderId", orderId)
                        startActivity(intent)
                    }
                }
            }
    }


}