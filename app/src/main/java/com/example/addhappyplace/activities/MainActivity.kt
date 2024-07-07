package com.example.addhappyplace.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.addhappyplace.R
import com.example.addhappyplace.adapters.MainAdapter
import com.example.addhappyplace.database.DatabaseHandler
import com.example.addhappyplace.databinding.ActivityMainBinding
import com.example.addhappyplace.models.HappyPlaceModel
import com.example.addhappyplace.utils.SwipeToEditCallback

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setToolBar()

        binding.fabAddHappyPlace.setOnClickListener {
            val intent = Intent(this@MainActivity, AddHappyPlaceActivity::class.java)
            startActivityForResult(intent, MAIN_ACTIVITY_REQUEST_CODE)
        }

        getHappyPlacesListFromLocalDB()


        val editSwipeHandler = object : SwipeToEditCallback(this@MainActivity) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.rvMainList.adapter as MainAdapter
                adapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition, MAIN_ACTIVITY_REQUEST_CODE)
            }
        }

        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(binding.rvMainList)
    }

    private fun setupMainRecyclerView(happyPlaceList: ArrayList<HappyPlaceModel>) {
        val countItem = happyPlaceList.size

        binding.rvMainList.layoutManager = LinearLayoutManager(this)
        val mainAdapter = MainAdapter(applicationContext, happyPlaceList)
        binding.rvMainList.adapter = mainAdapter

        // To scrolling automatic when data entered
        binding.rvMainList
            .smoothScrollToPosition(countItem - 1)

        // When input data automatically to last index
        binding.rvMainList
            .layoutManager!!.smoothScrollToPosition(binding
                .rvMainList, null, countItem - 1)

        mainAdapter.setOnClickListener(object : MainAdapter.OnClickListener{
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity, DetailActivity::class.java)
                intent.putExtra(EXTRA_DETAILS, model)
                startActivity(intent)
            }
        })
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

    // Taking data class from database when it clicks the add button on AddHappyPlaceActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MAIN_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                getHappyPlacesListFromLocalDB()
            } else {
                Log.e("Activity", "Cancelled or Back Pressed")
            }
        }
    }

    companion object {
        var MAIN_ACTIVITY_REQUEST_CODE = 1
        var EXTRA_DETAILS = "extra_details"
    }
}