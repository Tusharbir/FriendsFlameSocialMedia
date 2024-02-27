package com.example.friendsflame.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.friendsflame.R
import com.example.friendsflame.utils.SharedPreferncesUtils.customToast
import com.example.friendsflame.adapters.FriendsAdapter
import com.example.friendsflame.adapters.ReceivedFriendsAdapter
import com.example.friendsflame.databinding.FragmentFriendsBinding
import com.example.friendsflame.models.UserViewModel
import com.example.friendsflame.utils.LoadingUtils


class FriendsFragment : Fragment() {

    private lateinit var binding: FragmentFriendsBinding
    val viewModel = UserViewModel()

    private lateinit var userAdapter: ReceivedFriendsAdapter


    private lateinit var friendsAdapter : FriendsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        LoadingUtils.showDialog(requireContext(),false)

        binding = FragmentFriendsBinding.inflate(layoutInflater, container, false)
        observeRequest()
        setupRecyclerView()

        viewModel.getFriendRequests()


        viewModel.getFriends()

        observeFriends()

        return binding.root
    }

    fun observeRequest(){
        viewModel.friendRequestDetails.observe(viewLifecycleOwner){
            listUserData ->
            if(listUserData.isEmpty()){
                customToast(requireContext(),"No Friend Requests")
            }
            else{
                userAdapter.updateUsers(listUserData)
            }
        }
    }

    fun observeFriends(){
        viewModel.friends.observe(viewLifecycleOwner){
            friendsList ->
                    if(friendsList.isNotEmpty()){
                        friendsAdapter = FriendsAdapter(friendsList){
                            val userIdReceived = it
                            val bundle = Bundle()
                            bundle.putString("friendUserId", userIdReceived)
                            val fragment = FriendsProfileFragment()
                            fragment.arguments = bundle
                            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(FriendsFragment::class.java.toString()).commit()
                        }
                        binding.friendsRecycler.layoutManager = LinearLayoutManager(requireContext())
                        binding.friendsRecycler.adapter = friendsAdapter}

            LoadingUtils.hideDialog()
        }

    }


    fun setupRecyclerView(){

        userAdapter = ReceivedFriendsAdapter(arrayListOf(), onViewDetailsClick = {
            userDetails ->
            val userDetailsBottomSheet = BottomSheetUserDetailsFragment.newInstance(userDetails)
            userDetailsBottomSheet.show(parentFragmentManager, userDetailsBottomSheet.tag)
        },
            onCardClick = {
                userId ->  val bundle = Bundle()
                bundle.putString("friendUserId", userId)
                val fragment = FriendsProfileFragment()
                fragment.arguments = bundle
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(FriendsFragment::class.java.toString()).commit()
            })

        binding.friendRequests.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            adapter = userAdapter
        }
    }
}