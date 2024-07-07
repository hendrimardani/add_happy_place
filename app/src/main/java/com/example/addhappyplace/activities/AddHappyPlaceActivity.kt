package com.example.addhappyplace.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.addhappyplace.R
import com.example.addhappyplace.database.DatabaseHandler
import com.example.addhappyplace.databinding.ActivityAddHappyPlaceBinding
import com.example.addhappyplace.models.HappyPlaceModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityAddHappyPlaceBinding
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private val cal = Calendar.getInstance()
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var mHappyPlaceDetails: HappyPlaceModel? = null

    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setToolBar()

        dateSetListener  = DatePickerDialog.OnDateSetListener {
            view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        updateDateInView()
        binding.etDate.setOnClickListener(this)
        binding.tvAddImage.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)

        // Edit item on edit text
        editValue()
    }

    private fun setToolBar() {
        // Call object actionBar
        setSupportActionBar(binding.tbAddHappyPlace)
        // Back to home
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = "Add Place"
            // Change font style text
            binding.tbAddHappyPlace.setTitleTextAppearance(this@AddHappyPlaceActivity,
                R.style.Base_Theme_AddHappyPlace
            )
        }
        binding.tbAddHappyPlace.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun editValue() {
        if (intent.hasExtra(MainActivity.EXTRA_DETAILS)) {
            mHappyPlaceDetails = intent.getParcelableExtra(MainActivity.EXTRA_DETAILS)
        }

        if (mHappyPlaceDetails != null) {
            supportActionBar!!.title = "Edit Happy Place"

            binding.etTitle.setText(mHappyPlaceDetails!!.title)
            binding.etDescription.setText(mHappyPlaceDetails!!.description)
            binding.etDate.setText(mHappyPlaceDetails!!.date)
            binding.etLocation.setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitude
            mLongitude = mHappyPlaceDetails!!.longitude
            saveImageToInternalStorage = Uri.parse(mHappyPlaceDetails!!.image)
            binding.ivAddImage.setImageURI(saveImageToInternalStorage)
            binding.btnSave.text = "Update"
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.et_date -> {
                DatePickerDialog(
                    this@AddHappyPlaceActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            R.id.tv_add_image -> {
                customDialogForPicture()
            }
            R.id.btn_save -> {
                val title = binding.etTitle.text
                val description = binding.etDescription.text
                val location = binding.etLocation.text
                val image = saveImageToInternalStorage.toString()

                if (title!!.isNotEmpty() && description!!.isNotEmpty() && location!!.isNotEmpty()
                    && image.isNotEmpty()) {
                    val happyPlaceModel = HappyPlaceModel(
                        id = if (mHappyPlaceDetails == null) 0 else mHappyPlaceDetails!!.id,
                        title = binding.etTitle.text.toString(),
                        description = binding.etDescription.text.toString(),
                        date = binding.etDate.text.toString(),
                        location = binding.etLocation.text.toString(),
                        image = saveImageToInternalStorage.toString(),
                        latitude = mLatitude,
                        longitude = mLongitude
                    )
                    val dbHandler = DatabaseHandler(this)
                    if (mHappyPlaceDetails == null) {
                        val addHandler = dbHandler.addHappyPlace(happyPlaceModel)
                        Log.e("Saved Image", "Ini add Handler $addHandler")
                        if (addHandler > 0) {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    } else {
                        val updateHandler = dbHandler.updateHappyPlace(happyPlaceModel)
                        if (updateHandler > 0) {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }
                } else Toast.makeText(this@AddHappyPlaceActivity, "Fill the Blank !", Toast.LENGTH_LONG).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun customDialogForPicture() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictoreDialogItems = arrayOf(
            "Select photo from gallery", "Capture photo from cameraa"
        )
        pictureDialog.setItems(pictoreDialogItems) {
            dialog ,which->
            when(which) {
                0 -> choosePhotoFromGallery()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show() // Display dialogs
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun choosePhotoFromGallery() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        val galleryIntent = Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(galleryIntent, GALLERY)
                    } else {
                        Toast.makeText(this@AddHappyPlaceActivity,
                            "Permission is denied",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            })
            .onSameThread()
            .check()
    }


    @SuppressLint("SuspiciousIndentation")
    private fun showRationalDialogForPermissions() {
        val buildDialog = AlertDialog.Builder(this)
            buildDialog.setMessage("It looks like you have turned off permission granted for this feature. " +
                    "It can be enabled under the Application settings")
            buildDialog.setPositiveButton("GO TO SETTINGS") {
                dialog, which ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                    Toast.makeText(this,
                        "After activing permission, you have to close the application. Then running again",
                        Toast.LENGTH_LONG).show()
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            buildDialog.setNegativeButton("Cancel") {
                dialog, which ->
                dialog.dismiss()
            }
        buildDialog.show()
    }

    private fun updateDateInView() {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etDate.setText(sdf.format(cal.time).toString())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        val selectedImageBitmap = MediaStore.Images.Media
                            .getBitmap(this.contentResolver, contentURI)
                        saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
                        Log.e("SAVED IMAGE : ", "Path: $saveImageToInternalStorage")
                        binding.ivAddImage.setImageBitmap(selectedImageBitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@AddHappyPlaceActivity,
                            "Failed load to image from gallery",
                            Toast.LENGTH_LONG).show()
                    }
                }
            } else if (requestCode == CAMERA) {
                val thumbNail = data!!.extras!!.get("data") as Bitmap
                saveImageToInternalStorage = saveImageToInternalStorage(thumbNail)
                Log.e("SAVED IMAGE : ", "Path: $saveImageToInternalStorage")
                binding.ivAddImage.setImageBitmap(thumbNail)
            }
        }
    }

    private fun takePhotoFromCamera() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(galleryIntent, CAMERA)
                        Toast.makeText(this@AddHappyPlaceActivity,
                            "Permission is denied",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            })
            .onSameThread()
            .check()
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(this)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }
}