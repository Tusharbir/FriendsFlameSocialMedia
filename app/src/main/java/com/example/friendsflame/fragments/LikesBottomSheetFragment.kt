package com.example.friendsflame.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.friendsflame.LoggedInUserDetails
import com.example.friendsflame.PostDetails
import com.example.friendsflame.R
import com.example.friendsflame.adapters.UserAdapter
import com.example.friendsflame.adapters.UsersAdapter
import com.example.friendsflame.databinding.FragmentLikesBottomSheetBinding
import com.example.friendsflame.models.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class LikesBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var post : PostDetails
    private lateinit var binding : FragmentLikesBottomSheetBinding

    private val viewModel : UserViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLikesBottomSheetBinding.inflate(layoutInflater,container,false)

        binding.progressBar.visibility = View.VISIBLE

        post = arguments?.getSerializable("post") as PostDetails



        setUpRecycler()

        // Inflate the layout for this fragment
        return binding.root
    }

    fun setUpRecycler(){

        viewModel.getLikesList(post.userId, post.postId){
            usersList ->
            if(usersList.isEmpty()||usersList.size==0){
                binding.progressBar.visibility = View.GONE
                binding.tvNoOneLiked.visibility = View.VISIBLE
            }
            else{
                val adapter = UsersAdapter(usersList, onProfileClick = {
                    dismiss()
                    val bundle = Bundle()
                    bundle.putString("friendUserId", it)
                    val fragment = FriendsProfileFragment()
                    fragment.arguments = bundle
                    parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(FriendsFragment::class.java.toString()).commit()
                })
                binding.recyclerLikesUsersList.layoutManager = LinearLayoutManager(requireContext())
                binding.recyclerLikesUsersList.adapter = adapter
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}