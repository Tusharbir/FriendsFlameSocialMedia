package com.example.friendsflame.adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.friendsflame.FriendRequestsUsers
import com.example.friendsflame.LoggedInUserDetails
import com.example.friendsflame.PostDetails
import com.example.friendsflame.R
import com.example.friendsflame.databinding.ItemUsersBinding


class UserAdapter(private var users: ArrayList<FriendRequestsUsers>, private val onAddFriendClicked: (String) -> Unit) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(val binding : ItemUsersBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemUsersBinding.inflate(layoutInflater,parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val users = users[position]
        with(holder.binding){
            tvSearchUsername.text = users.username
            tvSearchName.text = users.name
            if(users.profileUrl!=""){
                Glide.with(tvSearchProfile.context).load(users.profileUrl).into(tvSearchProfile)
            }
            else
            {
                tvSearchProfile.setImageResource(R.drawable.ic_blank_picture)
            }



            btSearchAddFriend.visibility = View.VISIBLE
            btSearchPending.visibility = View.GONE

//            if(users.status=="pending"){
//                btSearchAddFriend.visibility = View.GONE
//                btSearchPending.visibility = View.VISIBLE
//            }
//            else if(users.status == "Add Friend"){
//                btSearchAddFriend.visibility = View.VISIBLE
//                btSearchPending.visibility = View.GONE
//            }
//            else{
//                btSearchAddFriend.visibility = View.GONE
//                btSearchPending.visibility = View.GONE
//            }

            btSearchAddFriend.isVisible = users.status != "Accepted" && users.status == "Add Friend"
            btSearchPending.isVisible = users.status == "pending"

            btSearchAddFriend.setOnClickListener {
                onAddFriendClicked(users.userId)
                users.status="pending"
                notifyItemChanged(position)
            }

        }
    }

    fun updateUsers(newUsers: List<FriendRequestsUsers>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }
}