package com.example.friendsflame.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.friendsflame.PostDetails
import com.example.friendsflame.R
import com.example.friendsflame.databinding.ItemPostBinding
import com.example.friendsflame.models.UserViewModel
import com.example.friendsflame.repository.UserRepository
import com.google.firebase.Timestamp
import java.util.concurrent.TimeUnit



class PostAdapters(private var posts: ArrayList<PostDetails>, private val onLikeClick:(String, String) -> Unit, val onSaveClick:(String,String)->Unit, val onCommentClick:(PostDetails) -> Unit, val onTotalLikesClick:(PostDetails)->Unit, val onProfileClick:(String)->Unit) : RecyclerView.Adapter<PostAdapters.ViewHolder>(){



    class ViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater =LayoutInflater.from(parent.context)
        val binding = ItemPostBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val postItem = posts[position]
        with(holder.binding){
            tvPostUsername.text = postItem.username
            tvPostCaption.text = postItem.caption
            Glide.with(ivPostImage.context).load(postItem.imageUrl).into(ivPostImage)

            tvCaptionUsername.text = "${postItem.username}: "

            val timeDifference = getTimeDifferenceAsString(postItem.timestamp)
            tvPostDate.text = timeDifference

            if(postItem.profileUrl!=""){
                Glide.with(ivProfile.context).load(postItem.profileUrl).into(ivProfile)
            }
            else
            {
                ivProfile.setImageResource(R.drawable.ic_blank_picture)
            }
            var totalLikes = postItem.likeCount

            tvTotalLikes.text = "${postItem.likeCount} Likes"


            val initialHeartIconResId = if (postItem.isPostLiked) {
                R.drawable.ic_heart_filled
            } else {
                R.drawable.ic_heart
            }
            ivPostLikeButton.setImageResource(initialHeartIconResId)

            ivPostLikeButton.setOnClickListener {

                postItem.isPostLiked = !postItem.isPostLiked

                val heartIcon = if(postItem.isPostLiked){
                    R.drawable.ic_heart_filled
                }
                else{
                    R.drawable.ic_heart
                }

                if(postItem.isPostLiked){
                    totalLikes++
                    tvTotalLikes.text = "$totalLikes Likes"
                }
                else{
                    totalLikes--
                    tvTotalLikes.text = "$totalLikes Likes"
                }



                ivPostLikeButton.setImageResource(heartIcon)
                onLikeClick(postItem.userId,postItem.postId)
            }


            val initialSavedPostIcon = if (postItem.isPostSaved) {
                R.drawable.ic_save_post_filled
            } else {
                R.drawable.ic_save_post
            }
            ivSavePostButton.setImageResource(initialSavedPostIcon)

            ivSavePostButton.setOnClickListener {

                postItem.isPostSaved = !postItem.isPostSaved
                val savePostIcon = if(postItem.isPostSaved){
                    R.drawable.ic_save_post_filled
                }
                else{
                    R.drawable.ic_save_post
                }
                ivSavePostButton.setImageResource(savePostIcon)
                onSaveClick(postItem.userId,postItem.postId)
            }

            ivCommentButton.setOnClickListener {
                onCommentClick(postItem)
            }

            totalLikesLayout.setOnClickListener {
                onTotalLikesClick(postItem)
            }

            ivProfile.setOnClickListener {
                onProfileClick(postItem.userId)
            }
            tvPostUsername.setOnClickListener {
                onProfileClick(postItem.userId)
            }

        }
    }

    fun updatePosts(newPosts: ArrayList<PostDetails>) {
        if(!posts.containsAll(newPosts)){
            posts = newPosts
            notifyDataSetChanged()
        }
    }


    fun getTimeDifferenceAsString(timestamp: Timestamp): String {
        val currentTime = System.currentTimeMillis()
        val timestampMillis = timestamp.toDate().time
        val diff = currentTime - timestampMillis

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            minutes < 1 -> "just now"
            minutes < 60 -> "$minutes minutes ago"
            hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
            else -> "$days day${if (days > 1) "s" else ""} ago"
        }
    }
}