package com.example.friendsflame.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.friendsflame.FriendRequestsUsers
import com.example.friendsflame.R
import com.example.friendsflame.databinding.ItemFriendsBinding
import com.example.friendsflame.databinding.ItemUsersBinding

class FriendsAdapter(private var friends: ArrayList<FriendRequestsUsers>, val onProfileClick: (String) -> Unit): RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemFriendsBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFriendsBinding.inflate(layoutInflater,parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = friends[position]
        with(holder.binding) {
            tvFriendsUsername.text = friend.username
            tvFriendsName.text = friend.name
            if (friend.profileUrl != "") {
                Glide.with(ivFriendsProfile.context).load(friend.profileUrl).into(ivFriendsProfile)
            } else {
                ivFriendsProfile.setImageResource(R.drawable.ic_blank_picture)
            }
            root.setOnClickListener {
                onProfileClick(friend.userId)
            }
        }
    }
}