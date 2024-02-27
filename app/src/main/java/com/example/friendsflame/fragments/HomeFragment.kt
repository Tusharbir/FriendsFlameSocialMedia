package com.example.friendsflame.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment.STYLE_NORMAL
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.friendsflame.LoggedInUserDetails
import com.example.friendsflame.PostDetails
import com.example.friendsflame.R
import com.example.friendsflame.utils.SharedPreferncesUtils.getSharedData
import com.example.friendsflame.adapters.PostAdapters
import com.example.friendsflame.databinding.FragmentHomeBinding
import com.example.friendsflame.models.UserViewModel
import com.example.friendsflame.repository.UserRepository
import com.example.friendsflame.utils.LoadingUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.io.Serializable


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel : UserViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapters
    private lateinit var userData: LoggedInUserDetails
    private var allPosts = ArrayList<PostDetails>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        userData = getSharedData(requireContext())!!

        LoadingUtils.showDialog(requireContext(), false)
        setupRecyclerView()



        observePosts()
        viewModel.fetchPosts()

        observePostsFriends()
        viewModel.fetchPostsFriends()

        allPosts.clear()


        return binding.root
    }



    private fun observePosts() {
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            if(posts.isEmpty()){
                LoadingUtils.hideDialog()
            }
            allPosts.addAll(posts)
            updateRecyclerView()
            if(allPosts.isEmpty()){
                LoadingUtils.hideDialog()
            }
        }
    }


    private fun observePostsFriends() {

        viewModel.postsFriends.observe(viewLifecycleOwner) { posts ->
            allPosts.addAll(posts)
            updateRecyclerView()

        }
    }

    private fun updateRecyclerView() {
        allPosts.sortByDescending { it.timestamp }
        postAdapter.updatePosts(allPosts)
        LoadingUtils.hideDialog()
    }

    private fun setupRecyclerView() {
        Handler(Looper.getMainLooper()).postDelayed({
            if(allPosts.isEmpty()){
                binding.tvNoPosts.visibility= View.VISIBLE

                LoadingUtils.hideDialog()
            }
        },3000)


        recyclerView = binding.recyclerView
        postAdapter = PostAdapters(ArrayList(emptyList()),
            onLikeClick = {
                userId, postId -> viewModel.toggleLikeFunctionality(userId,postId) },
            onSaveClick = {
                          userId, postId ->viewModel.savePost(userId,postId) },
            onCommentClick = {
                postDetails -> val commentsBottomSheet = CommentsBottomSheetFragment.newInstance(postDetails)
                            commentsBottomSheet.show(parentFragmentManager, commentsBottomSheet.tag)
            },
            onTotalLikesClick = {post ->
                    val bundle = Bundle()
                    bundle.putSerializable("post",post)
                    val fragment = LikesBottomSheetFragment()
                    fragment.arguments = bundle
                    fragment.show(parentFragmentManager,fragment.tag)
            }, onProfileClick = {
                userId -> val bundle = Bundle()
                bundle.putString("friendUserId", userId)
                val fragment = FriendsProfileFragment()
                fragment.arguments = bundle
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(FriendsFragment::class.java.toString()).commit()
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = postAdapter

    }


}
