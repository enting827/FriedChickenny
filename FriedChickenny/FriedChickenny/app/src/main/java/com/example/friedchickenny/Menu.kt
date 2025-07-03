package com.example.friedchickenny

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuAdapter
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class Menu : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)

//---------------------- Select Home In Main Nav -----------------------
        // so when user click back button from cart, home page indicator shown in mainNav
        val mainNav = activity?.findViewById<BottomNavigationView>(R.id.mainNav)
        if (mainNav != null) {
            mainNav.getMenu().findItem(R.id.navMenu).setChecked(true)
        }

//-------------------------- Filter tab --------------------------
        // Setup
        val tabLayout = view.findViewById<TabLayout>(R.id.menuFilter)
        val nestedScrollView = view.findViewById<androidx.core.widget.NestedScrollView>(R.id.menuScroll)

        // Set Tab Selected Listener
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> nestedScrollView.scrollToView(view.findViewById(R.id.chickenSec))
                    1 -> nestedScrollView.scrollToView(view.findViewById(R.id.riceBoxSec))
                    2 -> nestedScrollView.scrollToView(view.findViewById(R.id.drinkSec))
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

//-------------------------- Menu List --------------------------
        //Setup
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()// Get a Firestore instance
        val collectionRef = db.collection("menu") // Access a collection
        val menu_list = mutableListOf<FoodItem>()

        // Read data from collection and add into menu_list
        collectionRef.get().addOnSuccessListener { documents ->
            for (document in documents) {
                val menuItem = FoodItem(
                    document.getString("id") ?: "",
                    document.getString("imageUrl") ?: "",
                    document.getString("name") ?: "",
                    document.getString("description") ?: "",
                    document.getString("category") ?: "",
                    document.get("selections") as? List<String> ?: listOf(),
                    document.getDouble("price") ?: 0.0,
                    document.getBoolean("recommend") ?: false
                )
                menu_list.add(menuItem)
            }
            menu_list.sortBy { it.id }

            // Separate items by category
            val chickenItems = menu_list.filter { it.category.lowercase() == "chicken" }
            val riceBoxItems = menu_list.filter { it.category.lowercase() == "rice box" }
            val drinkItems = menu_list.filter { it.category.lowercase() == "drink" }

            // RecyclerViews
            CommonUtils.setupMenuRv(requireContext(), view, R.id.rvChicken, chickenItems)
            CommonUtils.setupMenuRv(requireContext(), view, R.id.rvRiceBox, riceBoxItems)
            CommonUtils.setupMenuRv(requireContext(), view, R.id.rvDrink, drinkItems)
        }


        // Inflate the layout for this fragment
        return view
    }

    // Extension function to scroll to a view inside NestedScrollView
    private fun NestedScrollView.scrollToView(view: View) {
        this.post {
            this.smoothScrollTo(0, view.top)
        }
    }

}