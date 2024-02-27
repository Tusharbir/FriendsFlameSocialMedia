package com.example.friendsflame.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.friendsflame.FriendRequestsUsers
import com.example.friendsflame.R
import com.example.friendsflame.utils.SharedPreferncesUtils.customToast
import com.example.friendsflame.databinding.FragmentBottomSheetUserDetailsBinding
import com.example.friendsflame.models.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BottomSheetUserDetailsFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomSheetUserDetailsBinding
    private var userDetails = FriendRequestsUsers()
    private val viewModel = UserViewModel()

    companion object {
        fun newInstance(user: FriendRequestsUsers): BottomSheetUserDetailsFragment {
            val fragment = BottomSheetUserDetailsFragment()
            val args = Bundle()
            args.putSerializable("user", user)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBottomSheetUserDetailsBinding.inflate(inflater, container, false)
        userDetails = arguments?.getSerializable("user") as FriendRequestsUsers

        updateUI()

        binding.btAcceptRequest.setOnClickListener {
            acceptFriend()
        }

        return binding.root
    }




    private fun updateUI() {
        userDetails.let { user ->
            binding.tvDetailsName.text = user.name
            binding.tvDetailsUsername.text = user.username
            if(user.profileUrl!=""){
                Glide.with(binding.ivDetailsProfile.context).load(user.profileUrl).into(binding.ivDetailsProfile)
            } else {
                binding.ivDetailsProfile.setImageResource(R.drawable.ic_blank_picture)
            }
        }
    }

    private fun acceptFriend(){
            viewModel.acceptFriend(userDetails.userId)
            observeStatus()
            dismiss()
    }

    private fun observeStatus(){
        viewModel.status.observe(viewLifecycleOwner){
            if(it.first){
                customToast(requireContext(), it.second.toString())
            }
            else{
                customToast(requireContext(), it.second.toString())
            }
        }
    }
}