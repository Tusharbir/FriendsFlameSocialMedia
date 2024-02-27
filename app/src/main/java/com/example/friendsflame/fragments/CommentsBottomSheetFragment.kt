package com.example.friendsflame.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.friendsflame.Comments
import com.example.friendsflame.LoggedInUserDetails
import com.example.friendsflame.PostDetails
import com.example.friendsflame.R
import com.example.friendsflame.adapters.CommentAdapter
import com.example.friendsflame.databinding.FragmentCommentsBottomSheetBinding
import com.example.friendsflame.models.UserViewModel
import com.example.friendsflame.utils.LoadingUtils
import com.example.friendsflame.utils.SharedPreferncesUtils.getSharedData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.Serializable


class CommentsBottomSheetFragment : BottomSheetDialogFragment(), Serializable {
    private lateinit var userDetails:LoggedInUserDetails
    private lateinit var binding : FragmentCommentsBottomSheetBinding
    private lateinit var postDetails : PostDetails
    private lateinit var comments : ArrayList<Comments>
    private val viewModel : UserViewModel by viewModels()



    private lateinit var commentAdapter : CommentAdapter

    companion object {
        fun newInstance(post: PostDetails): CommentsBottomSheetFragment {
            val fragment = CommentsBottomSheetFragment()
            val args = Bundle()
            args.putSerializable("post", post)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        val offsetFromTop = 200
        (dialog as? BottomSheetDialog)?.behavior?.apply {
//            isFitToContents = true
//            setPeekHeight(700, true)
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        userDetails = getSharedData(requireContext())!!


        binding = FragmentCommentsBottomSheetBinding.inflate(inflater,container,false)

        binding.progressBar.visibility = View.VISIBLE


        postDetails = arguments?.getSerializable("post") as PostDetails

//        comments = postDetails.comment
//        comments.sortByDescending { it.timestamp}
        viewModel.fetchComments(postDetails)
        observeComments()


//        binding.etAddComment.isFocusableInTouchMode = true

        setUpProfilePic()


//        populateRecyclerView(comments)




        binding.ivPostComment.setOnClickListener {
            postingComment()
        }

        // Inflate the layout for this fragment
        return binding.root
    }



    private fun observeComments(){

        viewModel.comments.observe(viewLifecycleOwner){
            comments ->
            comments.sortByDescending { it.timestamp }

            if(comments.isEmpty()){
                binding.tvNoComments.visibility = View.VISIBLE
            }

            binding.recyclerPostComments.layoutManager = LinearLayoutManager(requireContext())
            commentAdapter = CommentAdapter(comments, onEditClick ={
                    commentDetails, comment ->
                viewModel.updateComment(postDetails.userId, postDetails.postId,comment,commentDetails.commentId)
            }, onDeleteClick = {
                viewModel.deleteComment(postDetails.userId,postDetails.postId,it.commentId)
            }, profileClick = {userId ->
                dismiss()
                val bundle = Bundle()
                bundle.putString("friendUserId", userId)
                val fragment = FriendsProfileFragment()
                fragment.arguments = bundle
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(FriendsFragment::class.java.toString()).commit()
            }
            )
            binding.recyclerPostComments.adapter = commentAdapter
            binding.progressBar.visibility = View.GONE

        }
    }



    fun setUpProfilePic(){

        if(userDetails!!.profileUrl!=""){
            Glide.with(binding.commentSectionProfileUrl.context).load(userDetails.profileUrl).into(binding.commentSectionProfileUrl)
        }
        else{
            binding.commentSectionProfileUrl.setImageResource(R.drawable.ic_blank_picture)
        }

    }


//    fun populateRecyclerView(comments: ArrayList<Comments>){
//        binding.recyclerPostComments.layoutManager = LinearLayoutManager(requireContext())
//        commentAdapter = CommentAdapter(comments, onEditClick ={
//                commentDetails, comment ->
//            viewModel.updateComment(postDetails.userId, postDetails.postId,comment,commentDetails.commentId)
//        }, onDeleteClick = {
//            viewModel.deleteComment(postDetails.userId,postDetails.postId,it.commentId)
//        })
//        binding.recyclerPostComments.adapter = commentAdapter
//    }

    fun postingComment(){
        val comment = binding.etAddComment.text.toString().trim()
        if(comment.isEmpty()){
            binding.etAddCommentLayout.error = "Empty Comments Can't Be Posted"
            Handler(Looper.getMainLooper()).postDelayed({
//                binding.etAddCommentLayout.error = ""
                binding.etAddCommentLayout.isErrorEnabled = false
            },800)
        }
        else if(comment.length>300){
            binding.etAddCommentLayout.error = "Limit Exceeded"
            Handler(Looper.getMainLooper()).postDelayed({
                binding.etAddCommentLayout.isErrorEnabled = false
            },800)
        }
        else{
            viewModel.postComment(postDetails.userId, postDetails.postId, comment){
                commentID ->
                val postedComment = Comments(commentID,userDetails.userId, comment, userDetails.profileUrl, userDetails.username,com.google.firebase.Timestamp.now() ,true, true)
                binding.tvNoComments.visibility= View.GONE

                commentAdapter.updateData(postedComment)

                binding.etAddComment.text.clear()
            }
        }
    }


}