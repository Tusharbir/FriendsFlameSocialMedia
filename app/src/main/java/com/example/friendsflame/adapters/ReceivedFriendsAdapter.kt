package com.example.friendsflame.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.friendsflame.FriendRequestsUsers
import com.example.friendsflame.R
import com.example.friendsflame.databinding.ItemUserReceivedBinding
import com.example.friendsflame.databinding.ItemUsersBinding


class ReceivedFriendsAdapter(private var users: ArrayList<FriendRequestsUsers>, val onViewDetailsClick : (FriendRequestsUsers) ->Unit, val onCardClick: (String) -> Unit) : RecyclerView.Adapter<ReceivedFriendsAdapter.ViewHolder>() {

    class ViewHolder(val binding : ItemUserReceivedBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemUserReceivedBinding.inflate(layoutInflater,parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val users = users[position]
        with(holder.binding){
            tvFriendsRequestUsername.text = users.username
            tvFriendRequestsName.text = users.name
            if(users.profileUrl!=""){
                Glide.with(ivFriendsRequestProfile.context).load(users.profileUrl).into(ivFriendsRequestProfile)
            }
            else
            {
                ivFriendsRequestProfile.setImageResource(R.drawable.ic_blank_picture)
            }
            btFriendsViewDetails.setOnClickListener {
                onViewDetailsClick.invoke(users)
            }

            root.setOnClickListener {
                onCardClick.invoke(users.userId)
            }

        }
    }

    fun updateUsers(newUsers: List<FriendRequestsUsers>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }
}