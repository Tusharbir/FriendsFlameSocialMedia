package com.example.friendsflame.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.friendsflame.R
import com.example.friendsflame.adapters.UsersAdapter
import com.example.friendsflame.databinding.FragmentUsersBottomSheetBinding
import com.example.friendsflame.models.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class UsersBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentUsersBottomSheetBinding
    private val viewModel : UserViewModel by viewModels()

    private lateinit var userId : String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUsersBottomSheetBinding.inflate(layoutInflater,container,false)

        userId = arguments?.getString("userId")!!

        Log.d("userId in Bottom Sheet", userId)


        setUpRecycler()

        return binding.root
    }

    fun setUpRecycler(){
        binding.progressBar.visibility= View.VISIBLE
        viewModel.getFriendsOfFriend(userId){
                usersList ->
            Log.d("userSList in Bottom Sheet", usersList.toString())
            if(usersList.isEmpty()||usersList.size==0){
                binding.progressBar.visibility = View.GONE
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
                binding.recyclerUsersList.layoutManager = LinearLayoutManager(requireContext())
                binding.recyclerUsersList.adapter = adapter
                binding.progressBar.visibility = View.GONE
            }
        }
    }

}