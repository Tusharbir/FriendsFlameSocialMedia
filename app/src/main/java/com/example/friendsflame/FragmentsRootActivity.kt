package com.example.friendsflame

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.PermissionRequest
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.friendsflame.databinding.ActivityFragmentsRootBinding
import com.example.friendsflame.fragments.FriendsFragment
import com.example.friendsflame.fragments.HomeFragment
import com.example.friendsflame.fragments.ProfileFragment
import com.example.friendsflame.fragments.SearchFragment
import com.example.friendsflame.models.UserViewModel
import com.example.friendsflame.utils.ImageFetchingUtility
import com.example.friendsflame.utils.LoadingUtils
import com.example.friendsflame.utils.SharedPreferncesUtils.customToast
import com.karumi.dexter.Dexter
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class FragmentsRootActivity : AppCompatActivity() {

    private val viewModel = UserViewModel()
    private var selectedImage : Uri? = null
    private lateinit var imagePreview : ImageView
    private lateinit var capturedImage :Uri
    private var compressedImage: File? = null

    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()){
        imagePreview.setImageURI(null)
        imagePreview.setImageURI(capturedImage)
        imagePreview.visibility = View.VISIBLE
        selectedImage = capturedImage
    }


    private val resultLauncher : ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback {
            if(it.resultCode== RESULT_OK){
                lifecycleScope.launch {
                    selectedImage = it.data?.data
                    compressedImage = Compressor.compress(this@FragmentsRootActivity,File(ImageFetchingUtility.getRealPathFromUri(this@FragmentsRootActivity,selectedImage)!!), Dispatchers.Main){
                        quality(75)
                        resolution(1080,1080)
                        format(Bitmap.CompressFormat.JPEG)
                    }
                    runOnUiThread{
                        imagePreview.setImageURI(Uri.fromFile(compressedImage))
                        imagePreview.visibility = View.VISIBLE
                        selectedImage = Uri.fromFile(compressedImage)
                        LoadingUtils.hideDialog()
                    }
                }
            }
        }
    )



    private lateinit var binding: ActivityFragmentsRootBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




        binding = ActivityFragmentsRootBinding.inflate(layoutInflater)

        val bottomNav = binding.bottomNavigation
        capturedImage = createImageUri()

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navProfile -> navigationFunction(ProfileFragment())
                R.id.navFriends -> navigationFunction(FriendsFragment())
                R.id.navSearch -> navigationFunction(SearchFragment())
                R.id.navAddPost -> showDialog()
                else -> navigationFunction(HomeFragment())
            }
            true
        }
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.navHome
        }
        setContentView(binding.root)
    }


    private fun navigationFunction(fragment: Fragment)
    {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container,fragment)
            commit()

        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
//        Log.d("back press count",supportFragmentManager.backStackEntryCount.toString() + " // "+supportFragmentManager.fragments.map { it. })
    }


    private fun getGallery(){

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if(ImageFetchingUtility.hasStoragePermissions(this)){
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                resultLauncher.launch(galleryIntent)
            }
            else{
                ImageFetchingUtility.checkAndRequestPermissions(this)
            }
        }
        else{
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            resultLauncher.launch(galleryIntent)
        }
    }


    private fun showDialog()
    {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_post, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        imagePreview = dialogView.findViewById<ImageView>(R.id.iv_ImagePreview)
        val captionText = dialogView.findViewById<EditText>(R.id.et_caption)
        val openGallery = dialogView.findViewById<ImageButton>(R.id.bt_uploadImage)
        val openCamera = dialogView.findViewById<ImageButton>(R.id.bt_camera)
        val postButton = dialogView.findViewById<Button>(R.id.postButton)

        openGallery.setOnClickListener {
            getGallery()
        }

        openCamera.setOnClickListener {
            contract.launch(capturedImage)
        }

        postButton.visibility = View.VISIBLE
        postButton.setOnClickListener {
            if(selectedImage == null || captionText.text.isEmpty()){
                customToast(this, "Add image and caption to it for better interactions!!")
            }
            else{
                LoadingUtils.showDialog(this,false)
                viewModel.uploadPost(selectedImage!!, captionText.text.toString())
                getStatus()
                dialog.dismiss()
            }
        }

        dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun getStatus()
    {  Handler(mainLooper).postDelayed({
        viewModel.postStatus.observe(this){
            if(it.first)
            {
                Log.d("post successfull", it.toString())
            }
            else
            {
                customToast(this,it.second.toString())
            }
            LoadingUtils.hideDialog()
        }
    },3000)
    }

    fun createImageUri():Uri{
        val image = File(filesDir,"camera_photos.png")
        return FileProvider.getUriForFile(this,
            "com.example.friendsflame.FileProvider",image
        )
    }
}