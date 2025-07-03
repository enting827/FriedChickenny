package com.example.friedchickenny

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class Home : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

//---------------------- Select Home In Main Nav -----------------------
        // so when user click back button from cart, home page indicator shown in mainNav
        val mainNav = activity?.findViewById<BottomNavigationView>(R.id.mainNav)
        if (mainNav != null) {
            mainNav.getMenu().findItem(R.id.navHome).setChecked(true)
        }

//--------------------------Carousel--------------------------
        val imageList = ArrayList<SlideModel>() // Create image list

        imageList.add(SlideModel(R.drawable.carousel1, ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.carousel2, ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.carousel3, ScaleTypes.FIT))

        val imageSlider = view.findViewById<ImageSlider>(R.id.carousel)
        imageSlider.setImageList(imageList)


//--------------------------Recommendation--------------------------
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
                if (menuItem.recommend) {
                    menu_list.add(menuItem)
                }
            }
            menu_list.sortBy { it.id }
            CommonUtils.setupMenuRv(requireContext(), view, R.id.rvRecommend, menu_list)
        }
//--------------------------End of Recommendation--------------------------

        // Inflate the layout for this fragment
        return view
    }
}