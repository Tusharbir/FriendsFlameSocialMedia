package com.example.friendsflame.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.friendsflame.LoggedInUserDetails
import com.example.friendsflame.databinding.ItemUsersListBinding

class UsersAdapter(private var users: ArrayList<LoggedInUserDetails>, val onProfileClick:(String)->Unit) : RecyclerView.Adapter<UsersAdapter.ViewHolder>(){
    class ViewHolder(val binding: ItemUsersListBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemUsersListBinding.inflate(layoutInflater,parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        with(holder.binding){
            tvUsernameName.text = user.username
            tvUserName.text = user.name
            if(user.profileUrl!=""){
                Glide.with(tvUserProfileImage.context).load(user.profileUrl).into(tvUserProfileImage)
            }

            tvUserProfileImage.setOnClickListener {
                onProfileClick(user.userId)
            }
            root.setOnClickListener {
                onProfileClick(user.userId)
            }
        }
    }


}