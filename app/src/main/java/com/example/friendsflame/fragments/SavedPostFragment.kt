package com.example.friendsflame.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.friendsflame.PostDetails
import com.example.friendsflame.R
import com.example.friendsflame.adapters.PostAdapters
import com.example.friendsflame.databinding.FragmentSavedPostBinding
import com.example.friendsflame.models.UserViewModel

class SavedPostFragment : Fragment() {

    private lateinit var binding: FragmentSavedPostBinding
    private lateinit var postDetails: PostDetails

    private val viewModel: UserViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSavedPostBinding.inflate(layoutInflater,container,false)

        postDetails = ((arguments?.getSerializable("postDetails") as PostDetails?)!!)


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
            },
            onTotalLikesClick = {
                    post ->
            }, onProfileClick ={
                userId -> val bundle = Bundle()
                bundle.putString("friendUserId", userId)
                val fragment = FriendsProfileFragment()
                fragment.arguments = bundle
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(FriendsFragment::class.java.toString()).commit()
            })

        binding.recyclerSavedSinglePost.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSavedSinglePost.adapter = adapter

        return binding.root
    }

}