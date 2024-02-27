package com.example.friendsflame.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.friendsflame.R
import com.example.friendsflame.adapters.FriendsPostAdapter
import com.example.friendsflame.adapters.PostAdapters
import com.example.friendsflame.databinding.FragmentUsersSavedPostsBinding
import com.example.friendsflame.models.UserViewModel


class UsersSavedPostsFragment : Fragment() {

    private val viewModel : UserViewModel by viewModels()
    private lateinit var binding: FragmentUsersSavedPostsBinding

    private var adapter : FriendsPostAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUsersSavedPostsBinding.inflate(layoutInflater,container,false)

        observePosts()
        viewModel.fetchSavedPosts()

        adapter = FriendsPostAdapter(){
                val bundle = Bundle()
                bundle.putSerializable("postDetails", it)
                val fragment = SavedPostFragment()
                fragment.arguments = bundle
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container,fragment).addToBackStack(UsersSavedPostsFragment::class.java.toString()).commit()
        }
        binding.recyclerSavedPost.layoutManager = GridLayoutManager(requireContext(),3)
        binding.recyclerSavedPost.adapter = adapter

        // Inflate the layout for this fragment
        return binding.root
    }


    private fun observePosts() {
        viewModel.savedPosts.observe(viewLifecycleOwner){
            posts ->
            posts.sortByDescending { it.timestamp }
            adapter?.updatePosts(posts)
        }
    }
}