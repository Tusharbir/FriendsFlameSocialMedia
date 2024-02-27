package com.example.friendsflame.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.friendsflame.LoggedInUserDetails
import com.example.friendsflame.R
import com.example.friendsflame.databinding.FragmentEditProfileBinding
import com.example.friendsflame.models.UserViewModel
import com.example.friendsflame.utils.ImageFetchingUtility
import com.example.friendsflame.utils.LoadingUtils
import com.example.friendsflame.utils.SharedPreferncesUtils
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.launch
import java.io.File


class EditProfileFragment : Fragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private val viewModel: UserViewModel by viewModels()

    private var selectedImage : Uri? = null
    private var compressedImage: File? = null

    private val resultLauncher : ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback {
            if(it.resultCode== AppCompatActivity.RESULT_OK){
                selectedImage = it.data?.data
                Log.d("selectedImage uri: ", selectedImage.toString())
                Log.d("selectedImage uri: ", selectedImage?.path.toString())
                Log.d("getRealPathFromUri", ImageFetchingUtility.getRealPathFromUri(requireContext(),selectedImage).toString())

                LoadingUtils.showDialog(requireContext(),false)

                lifecycleScope.launch {
                    compressedImage = Compressor.compress(requireContext(),
                        File(ImageFetchingUtility.getRealPathFromUri(requireContext(),selectedImage)!!)
                    ){
                        quality(75)
                        resolution(500,500)
                        format(Bitmap.CompressFormat.JPEG)
                    }
                    observeProfilePicture()
                }
            }
        }
    )


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ImageFetchingUtility.MY_PERMISSIONS_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchGallery()
            } else {
                SharedPreferncesUtils.customToast(requireContext(), "Permission denied")
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentEditProfileBinding.inflate(layoutInflater,container,false)

        binding.ivProfileImage.setOnClickListener {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                if(ImageFetchingUtility.hasStoragePermissions(requireContext())){
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    resultLauncher.launch(galleryIntent)
                }
                else{
                    ImageFetchingUtility.checkAndRequestPermissions(requireContext())
                }
            }
            else{
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                resultLauncher.launch(galleryIntent)
            }
        }

        binding.idSaveProfile.setOnClickListener {
            updateUserData()
        }

        fetchUserData()


        // Inflate the layout for this fragment
        return binding.root
    }

    private suspend fun observeProfilePicture() {
        Glide.with(binding.ivProfileImage.context).load(Uri.fromFile(compressedImage!!)).into(binding.ivProfileImage)
        LoadingUtils.hideDialog()
    }


    fun launchGallery(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(galleryIntent)
    }

    fun fetchUserData(){
        val userDetails = SharedPreferncesUtils.getSharedData(requireContext())
        binding.etUsernameProfile.setText(userDetails!!.username)
        binding.etUsernameProfile.editableText
        binding.etNameProfile.setText(userDetails.name)
        binding.etEmailProfile.setText(userDetails.email)
        Log.d("userprofileurl", userDetails.profileUrl)

        if(userDetails.profileUrl!=""){
            Glide.with(binding.ivProfileImage.context).load(userDetails.profileUrl).into(binding.ivProfileImage)
        }
    }

    fun observeStatus(){
        viewModel.status.observe(viewLifecycleOwner){
            if(it.first){
                SharedPreferncesUtils.customToast(requireContext(), "Data Updated Successfully!")
            }
            else
            {
                SharedPreferncesUtils.customToast(requireContext(), it.second.toString())
            }
        }
        LoadingUtils.hideDialog()
    }

    fun updateUserData(){

        val userDetails = SharedPreferncesUtils.getSharedData(requireContext())
        val username = binding.etUsernameProfile.text.toString()
        val name = binding.etNameProfile.text.toString()
        val email = userDetails!!.email
        val userId = userDetails.userId

        LoadingUtils.showDialog(requireContext(),false)

        if(username==userDetails.username){
            Log.d("selectedImage", selectedImage.toString())
            if(selectedImage==null){
                viewModel.updateProfileData(username = username , name = name, email = email , userId = userId, profileUrl = userDetails.profileUrl)
                SharedPreferncesUtils.saveUserDetails(
                    requireContext(),
                    LoggedInUserDetails(username, name, email, userId, userDetails.profileUrl, true)
                )
                observeStatus()
            }
            else{
                viewModel.uploadProfileImage(compressedImage!!)
                viewModel.profileImageString.observe(viewLifecycleOwner){ url ->
                    Glide.with(binding.ivProfileImage.context).load(url).into(binding.ivProfileImage)
                    viewModel.updateProfileData(username = username, name = name, email = email , userId = userId, profileUrl = url)
                    SharedPreferncesUtils.saveUserDetails(
                        requireContext(),
                        LoggedInUserDetails(username, name, email, userId, url, true)
                    )
                    observeStatus()
                }
            }
        }
        else
        {
            viewModel.checkUsername(username)
            viewModel.usernameStatus.observe(viewLifecycleOwner){
                if(it.first){
                    if(selectedImage==null){
                        viewModel.updateProfileData(username = username , name = name, email = email , userId = userId, profileUrl = userDetails.profileUrl)
                        SharedPreferncesUtils.saveUserDetails(
                            requireContext(),
                            LoggedInUserDetails(
                                username,
                                name,
                                email,
                                userId,
                                userDetails.profileUrl,
                                true
                            )
                        )
                        observeStatus()
                    }
                    else{
                        viewModel.uploadProfileImage(compressedImage!!)
                        viewModel.profileImageString.observe(viewLifecycleOwner){
                                url ->
                            Glide.with(binding.ivProfileImage.context).load(url).into(binding.ivProfileImage)
                            viewModel.updateProfileData(username = username, name = name, email = email , userId = userId, profileUrl = url)
                            SharedPreferncesUtils.saveUserDetails(
                                requireContext(),
                                LoggedInUserDetails(username, name, email, userId, url, true)
                            )
                            observeStatus()
                        }
                    }
                }
                else
                {
                    binding.etUsernameProfileLayout.setError(it.second)
                    LoadingUtils.hideDialog()
                }
            }
        }
    }


}

