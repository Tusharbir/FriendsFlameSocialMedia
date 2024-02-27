package com.example.friendsflame.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.friendsflame.Comments
import com.example.friendsflame.LoggedInUserDetails
import com.example.friendsflame.R
import com.example.friendsflame.databinding.ItemPostCommentsBinding

class CommentAdapter(private var comments : ArrayList<Comments>, val onEditClick:(Comments, String)-> Unit, val onDeleteClick:(Comments)-> Unit, val profileClick:(String)->Unit): RecyclerView.Adapter<CommentAdapter.ViewHolder>() {


    private lateinit var context: Context

    class ViewHolder(val binding: ItemPostCommentsBinding): RecyclerView.ViewHolder(binding.root)


    fun getContext(context: Context){
        this.context = context
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPostCommentsBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]

        val params = holder.binding.deleteButton.layoutParams as LinearLayout.LayoutParams
        params.weight = 0.5f
        params.gravity = Gravity.START or Gravity.END
        holder.binding.deleteButton.layoutParams = params



        with(holder.binding){



            tvCommentUserComment.text = comment.comment
            tvCommentUsername.text = comment.username
            if(comment.profileUrl!=""){
                Glide.with(ivCommentProfileUrl.context).load(comment.profileUrl).into(ivCommentProfileUrl)
            }
            else{
                ivCommentProfileUrl.setImageResource(R.drawable.ic_blank_picture)
            }

            deleteButton.visibility = if (comment.isDeletable) View.VISIBLE else View.GONE
                editButton.visibility = if (comment.isEditable && !comment.isOpen) View.VISIBLE else View.GONE

            commentEditText.visibility = if(comment.isOpen) View.VISIBLE else View.GONE
            editComment.visibility  = if(comment.isOpen) View.VISIBLE else View.GONE
            tvCommentUserComment.visibility = if(!comment.isOpen) View.VISIBLE else View.GONE

            if(editButton.visibility == View.GONE && deleteButton.visibility == View.VISIBLE){
                val params = deleteButton.layoutParams as LinearLayout.LayoutParams
                params.weight = 1.5f
                params.gravity = Gravity.START or Gravity.END

                deleteButton.layoutParams = params
            }

            var commentText = comment.comment

            if(commentEditText.isVisible){
                commentEditText.setText(commentText)
                commentEditText.setSelection(commentText.length)
                commentEditText.isFocusableInTouchMode = true
                commentEditText.isFocusable = true
                commentEditText.requestFocus()
                Handler(Looper.getMainLooper()).postDelayed({
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(commentEditText, InputMethodManager.SHOW_IMPLICIT)
                }, 100)


                val params = deleteButton.layoutParams as LinearLayout.LayoutParams

                params.weight = 0.5f
                params.gravity = Gravity.TOP
                deleteButton.layoutParams = params
            }


            editButton.setOnClickListener {
                getContext(it.context)
                comments.forEachIndexed { index, comments -> comments.isOpen = index == position && !comments.isOpen}
                notifyDataSetChanged()

            }


            editComment.setOnClickListener {
                Log.d("THis function called", "editcomeentlistenrer clicked")
                val newText = commentEditText.text.toString().trim()
                Log.d("New Text", newText)
                if(newText!=comment.comment && newText!="" && newText.isNotEmpty() && newText.length<=300 ){
                        onEditClick(comment,newText)
                        commentEditText.isFocusable = true
                        commentEditText.visibility = View.GONE
                        commentEditText.setText(newText)
                        editComment.visibility = View.GONE
                        editButton.visibility = View.VISIBLE
                        commentText = newText
                        tvCommentUserComment.text = newText
                        tvCommentUserComment.visibility = View.VISIBLE
                        comment.comment = newText
                        notifyItemChanged(position)
                        comment.isOpen = false
                    }
                else if(newText.isEmpty()){
                    commentEditText.setError("Cant Post Empty Comments", getDrawable(holder.itemView.context,R.drawable.ic_error))
                }
                else if(newText.length>300){
                    commentEditText.setError("Limit Exceeded", getDrawable(holder.itemView.context,R.drawable.ic_error))
                }
                else{
                        commentEditText.visibility = View.GONE
                        editComment.visibility = View.GONE
                        commentEditText.isFocusable = true
                        editButton.visibility = View.VISIBLE
                        tvCommentUserComment.text = comment.comment
                        tvCommentUserComment.visibility = View.VISIBLE
                    comment.isOpen = false
                    notifyItemChanged(position)
                    }
                }


            deleteButton.setOnClickListener {
                onDeleteClick(comment)
                comments.remove(comment)
                notifyItemRemoved(position)
                notifyDataSetChanged()
            }

            ivCommentProfileUrl.setOnClickListener {
                profileClick(comment.userId)
            }
            tvCommentUsername.setOnClickListener {
                profileClick(comment.userId)
            }
        }

    }

    fun updateData(newComments: Comments){
        if(!comments.contains(newComments)){
            comments.add(0,newComments)
            notifyDataSetChanged()
        }
    }

    fun getKeyboard(view: View){

    }
}