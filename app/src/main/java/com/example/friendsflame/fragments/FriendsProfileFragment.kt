package com.example.friendsflame.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.friendsflame.PostDetails
import com.example.friendsflame.R
import com.example.friendsflame.adapters.FriendsPostAdapter
import com.example.friendsflame.databinding.FragmentFriendsProfileBinding
import com.example.friendsflame.models.UserViewModel
import com.example.friendsflame.utils.LoadingUtils


class FriendsProfileFragment : Fragment() {

    private lateinit var binding: FragmentFriendsProfileBinding
    private val  viewModel = UserViewModel()
    private var adapter : FriendsPostAdapter? = null

    private lateinit var userId : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LoadingUtils.showDialog(requireContext(),false)

        binding = FragmentFriendsProfileBinding.inflate(layoutInflater,container,false)

        userId = arguments?.getString("friendUserId")!!

        viewModel.fetchUserWholeDetails(userId)
        viewModel.fetchPostsByUserId(userId)

        fetchUserDetails()
        fetchPosts()

        adapter = FriendsPostAdapter(){
                observeSinglePostData(it)
        }

        binding.friendsPostsRecycler.layoutManager = GridLayoutManager(requireContext(),3)
        binding.friendsPostsRecycler.adapter = adapter

        binding.llFriendsFriendscounter.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("userId",userId)
            val fragment = UsersBottomSheetFragment()
            fragment.arguments = bundle
            fragment.show(parentFragmentManager,fragment.tag)
        }

        return binding.root
    }

    fun fetchUserDetails(){
        viewModel.friendsUserProfile.observe(viewLifecycleOwner){
                binding.tvCounterFriendsProfileUsername.text = it.userDetails.username
                binding.tvCounterFriendsProfilePostsId.text = it.posts.toString()
                binding.tvCounterFriendsProfileFriends.text = it.friends.toString()
                binding.tvCounterFriendsProfileName.text = it.userDetails.name
            if (it.userDetails.profileUrl != "") {
                Glide.with(binding.ivFriendsProfileImage.context).load(it.userDetails.profileUrl).into(binding.ivFriendsProfileImage)
            } else {
                binding.ivFriendsProfileImage.setImageResource(R.drawable.ic_blank_picture)
            }
            LoadingUtils.hideDialog()
        }
    }

    fun fetchPosts(){
            viewModel.friendsPosts.observe(viewLifecycleOwner){
                posts ->
                posts.sortByDescending { it.timestamp }
                adapter?.updatePosts(posts)
            }
    }
    private fun observeSinglePostData(postDetails:PostDetails){
        val bundle = Bundle()
        bundle.putSerializable("postDetails", postDetails)
        val fragment = FriendsSinglePostFragment()
        fragment.arguments = bundle
        parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(FriendsProfileFragment::class.toString()).commit()
    }

}