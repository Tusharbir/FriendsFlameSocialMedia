package com.example.friendsflame.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.friendsflame.PostDetails
import com.example.friendsflame.databinding.ItemFriendsPostBinding
import com.example.friendsflame.databinding.ItemPostBinding

class FriendsPostAdapter(private val onClick: (PostDetails) -> Unit): RecyclerView.Adapter<FriendsPostAdapter.ViewHolder>() {
    private val posts = arrayListOf<PostDetails>()
    class ViewHolder(val binding: ItemFriendsPostBinding): RecyclerView.ViewHolder(binding.root){
        fun onBind(post: PostDetails, onClick: (PostDetails) -> Unit){
            Glide.with(binding.postImage.context).load(post.imageUrl).into(binding.postImage)
            binding.postImage.setOnClickListener {
                Log.d("back press count", "count of adapter")
                onClick(post)
            }
        }
    }


    fun updatePosts(posts : List<PostDetails>){
        this.posts.clear()
        this.posts.addAll(posts)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFriendsPostBinding.inflate(layoutInflater,parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        holder.onBind(post,onClick)

    }
}