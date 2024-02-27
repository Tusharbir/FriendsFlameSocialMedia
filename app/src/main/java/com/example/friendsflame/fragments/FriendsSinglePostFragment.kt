package com.example.friendsflame.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.friendsflame.PostDetails
import com.example.friendsflame.R
import com.example.friendsflame.adapters.PostAdapters
import com.example.friendsflame.databinding.FragmentFriendsSinglePostBinding
import com.example.friendsflame.models.UserViewModel
import com.example.friendsflame.utils.LoadingUtils


class FriendsSinglePostFragment : Fragment() {

    private lateinit var postDetails: PostDetails
    private lateinit var binding: FragmentFriendsSinglePostBinding

    private val viewModel = UserViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {



        binding = FragmentFriendsSinglePostBinding.inflate(layoutInflater,container,false)

        postDetails = (arguments?.getSerializable("postDetails") as PostDetails?)!!

        binding.tvFriendsSinglePostUsername.text = postDetails.username



        val postList = ArrayList<PostDetails> ()
        postList.clear()
        postList.add(postDetails)

        val adapter = PostAdapters(postList, onLikeClick ={
                userId, postId -> viewModel.toggleLikeFunctionality(userId,postId)
        }, onSaveClick = {userId, postId ->viewModel.savePost(userId,postId)},
            onCommentClick = {
                    postDetails ->
                val commentsBottomSheet = CommentsBottomSheetFragment.newInstance(postDetails)
                commentsBottomSheet.show(parentFragmentManager, commentsBottomSheet.tag)
            }, onTotalLikesClick = {
               post ->
            }, onProfileClick = {
                userId-> val bundle = Bundle()
                bundle.putString("friendUserId", userId)
                val fragment = FriendsProfileFragment()
                fragment.arguments = bundle
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(FriendsFragment::class.java.toString()).commit()
            })

        binding.recyclerFriendsSinglePost.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFriendsSinglePost.adapter = adapter

        return binding.root
    }



}