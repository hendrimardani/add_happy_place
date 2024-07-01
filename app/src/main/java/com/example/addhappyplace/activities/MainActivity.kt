package com.example.addhappyplace.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.addhappyplace.R
import com.example.addhappyplace.adapters.MainAdapter
import com.example.addhappyplace.database.DatabaseHandler
import com.example.addhappyplace.databinding.ActivityMainBinding
import com.example.addhappyplace.models.HappyPlaceModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setToolBar()

        binding.fabAddHappyPlace.setOnClickListener {
            val intent = Intent(this@MainActivity, AddHappyPlaceActivity::class.java)
            startActivity(intent)
        }

        getHappyPlacesListFromLocalDB()
    }

    private fun setupMainRecyclerView(happyPlaceList: ArrayList<HappyPlaceModel>) {
        binding.rvMainList.layoutManager = LinearLayoutManager(this)
//        binding.rvMainList.setHasFixedSize(true)
        val mainAdapter = MainAdapter(happyPlaceList)
        binding.rvMainList.adapter = mainAdapter
    }


    private fun getHappyPlacesListFromLocalDB() {
        val dbHandler = DatabaseHandler(this)
        val getHappyPlaceList = dbHandler.getHappyPlacesList()

        if (getHappyPlaceList.size > 0) {
            for (i in getHappyPlaceList) {
                binding.rvMainList.visibility = View.VISIBLE
                binding.tvNoRecordsAvailables.visibility = View.GONE
                setupMainRecyclerView(getHappyPlaceList)
                Log.e("TEST", i.title)
                Log.e("TEST", i.image)
                Log.e("TEST", i.description)
                Log.e("TEST", i.date)
                Log.e("TEST", i.location)
                Log.e("TEST", i.latitude.toString())
                Log.e("TEST", i.longitude.toString())

            }
        } else {
            binding.rvMainList.visibility = View.GONE
            binding.tvNoRecordsAvailables.visibility = View.VISIBLE
        }
    }

    private fun setToolBar() {
        // Call object actionBar
        setSupportActionBar(binding.tbMain)
        // Back to home
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
            supportActionBar!!.title = "Home"
            // Change font style text
            binding.tbMain.setTitleTextAppearance(this@MainActivity,
                R.style.Base_Theme_AddHappyPlace
            )
        }
    }
}