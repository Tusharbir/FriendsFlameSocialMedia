package com.example.friendsflame.fragments

import android.content.Context
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
import android.widget.PopupMenu
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.friendsflame.LoggedInUserDetails
import com.example.friendsflame.LoginActivity
import com.example.friendsflame.MainActivity
import com.example.friendsflame.PostDetails
import com.example.friendsflame.R
import com.example.friendsflame.adapters.FriendsPostAdapter
import com.example.friendsflame.utils.SharedPreferncesUtils.clearSharedPreferences
import com.example.friendsflame.utils.SharedPreferncesUtils.customToast
import com.example.friendsflame.utils.SharedPreferncesUtils.getSharedData
import com.example.friendsflame.utils.SharedPreferncesUtils.saveUserDetails
import com.example.friendsflame.databinding.FragmentProfilesBinding
import com.example.friendsflame.models.UserViewModel
import com.example.friendsflame.utils.ImageFetchingUtility
import com.example.friendsflame.utils.LoadingUtils
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.launch
import java.io.File


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfilesBinding

    private val viewModel : UserViewModel by viewModels()
    private var adapter : FriendsPostAdapter? = null




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding=FragmentProfilesBinding.inflate(layoutInflater,container,false)


        binding.btEditProfile.setOnClickListener {
            val fragment = EditProfileFragment()
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container,fragment).addToBackStack(ProfileFragment::class.toString()).commit()
        }

        adapter = FriendsPostAdapter(){
            observeSinglePostData(it)
        }

        binding.recyclerPostsProfile.layoutManager = GridLayoutManager(requireContext(),3)
        binding.recyclerPostsProfile.adapter = adapter


        fetchPosts()
        fetchFriendsNumber()

        viewModel.fetchPosts()


        binding.profileOptionsMenu.setOnClickListener {
            val popUpMenu : PopupMenu = PopupMenu(requireContext(),binding.profileOptionsMenu)
            popUpMenu.menuInflater.inflate(R.menu.profile_options_menu, popUpMenu.menu)
            popUpMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {item ->
                when(item.itemId){
                R.id.options_profileLogout -> profileLogout()
                R.id.options_ProfileChangePassword -> changePassword()
                R.id.options_profileSavedPosts -> savedPosts()
            }
            true
            }
            )
            popUpMenu.show()

        }

        binding.llFriendsCounter.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container,FriendsFragment()).addToBackStack(ProfileFragment::class.java.toString()).commit()
        }

        updateUser()

        return binding.root
    }

    fun updateUser(){
        val userData = getSharedData(requireContext())
        if(userData!!.profileUrl!=""){
            Glide.with(binding.ivUserProfileImageOwn.context).load(userData.profileUrl).into(binding.ivUserProfileImageOwn)
        }
        binding.tvUserName.text = userData.username


    }

    fun fetchFriendsNumber(){
        viewModel.totalFriends {
            count -> if(count>0){
                binding.tvCounterFriends.text = count.toString()
        }
        }
    }

    fun fetchPosts(){
        viewModel.posts.observe(viewLifecycleOwner){
            posts ->
            posts.sortByDescending { it.timestamp }
            if(posts.size>0){
                binding.tvCounterPosts.text = posts.size.toString()
            }
            adapter?.updatePosts(posts)
            binding.progressBar.visibility = View.GONE
        }
    }





    private fun observeSinglePostData(postDetails: PostDetails){

        val bundle = Bundle()
        bundle.putSerializable("postDetails", postDetails)
        val fragment = FriendsSinglePostFragment()
        fragment.arguments = bundle
        parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(ProfileFragment::class.java.toString()).commit()

    }



    fun profileLogout(){
        viewModel.logout()
        clearSharedPreferences(requireContext())
        startActivity(Intent(requireContext(),LoginActivity::class.java))
        activity?.finish()
    }

    fun savedPosts(){
        val fragment = UsersSavedPostsFragment()
        parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(ProfileFragment::class.java.toString()).commit()
    }


    fun changePassword(){
        parentFragmentManager.beginTransaction().replace(R.id.fragment_container, ChangePasswordFragment()).addToBackStack(ProfileFragment().toString()).commit()
    }
}