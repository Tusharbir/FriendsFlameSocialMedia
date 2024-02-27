package com.example.friendsflame.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.friendsflame.R
import com.example.friendsflame.adapters.PostAdapters
import com.example.friendsflame.adapters.UserAdapter
import com.example.friendsflame.databinding.FragmentSearchBinding
import com.example.friendsflame.models.UserViewModel


class SearchFragment : Fragment() {

    val viewModel = UserViewModel()
    private lateinit var userAdapter: UserAdapter
    private lateinit var binding : FragmentSearchBinding
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding=FragmentSearchBinding.inflate(layoutInflater,container,false)

        viewModel.fetchAllUsersWithStatus()
        observeFetchedUsers()
        setupRecyclerView()


        binding.searchUsers.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.searchUsers(it) }
                return true
            }
        })


        return binding.root
    }

    fun observeFetchedUsers(){

        viewModel.users.observe(viewLifecycleOwner) { users ->
            userAdapter.updateUsers(users)
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            userAdapter.updateUsers(results)
        }
    }

    fun setupRecyclerView(){

        userAdapter = UserAdapter(arrayListOf()) { userId ->
            viewModel.sendFriendRequest(userId)
        }

        binding.usersRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
        }
    }
    }




