package com.example.addhappyplace.activities

import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.addhappyplace.R
import com.example.addhappyplace.databinding.ActivityDetailBinding
import com.example.addhappyplace.models.HappyPlaceModel

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setToolbar()
        var happyPlaceModel: HappyPlaceModel? = null

        // Capture data value from MainActivity, then store on happyPlaceModel variable
        if (intent.hasExtra(MainActivity.EXTRA_DETAILS)) {
            happyPlaceModel = intent.getParcelableExtra(MainActivity.EXTRA_DETAILS)
        }

        binding.ivDetail.setImageURI(Uri.parse(happyPlaceModel!!.image))
        binding.tvDescription.text = happyPlaceModel.description
        binding.tvLocation.text = happyPlaceModel.location

    }

    private fun setToolbar() {
        // Call object actionBar
        setSupportActionBar(binding.tbDetail)
        // Back to home
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
            supportActionBar!!.title = "Details"
            // Change font style text
            binding.tbDetail.setTitleTextAppearance(this@DetailActivity,
                R.style.Base_Theme_AddHappyPlace
            )
            // Back press button
            binding.tbDetail.setNavigationOnClickListener {
                onBackPressed()
            }
        }
    }
}