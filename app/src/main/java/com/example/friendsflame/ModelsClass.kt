package com.example.friendsflame

import java.io.Serializable
import java.sql.Timestamp



data class PostDetails(
    val caption: String = "",
    val imageUrl: String="",
    val timestamp: com.google.firebase.Timestamp= com.google.firebase.Timestamp.now(),
    val userId: String="",
    var username: String ="",
    var profileUrl: String = "",
    val postId : String = "",
    var likeCount:Int =0,
    var isPostLiked: Boolean = false,
    var isPostSaved: Boolean = false,
    var comment: ArrayList<Comments> = ArrayList()
):Serializable


data class Comments(
    var commentId: String ="",
    val userId: String ="",
    var comment: String="",
    var profileUrl: String="",
    var username: String="",
    val timestamp: com.google.firebase.Timestamp= com.google.firebase.Timestamp.now(),
    var isEditable: Boolean = false,
    var isDeletable:Boolean=false,
    var isOpen : Boolean = false
): Serializable

data class LoggedInUserDetails(val username: String = "",
                               val name: String= "",
                               val email: String= "",
                               val userId: String = "",
                               val profileUrl: String="",
                               val login: Boolean = false)


data class FriendRequestsUsers(val username: String = "",
                               val name: String= "",
                               val userId: String = "",
                               val profileUrl: String="",
                               var status : String =""):Serializable


data class FriendsProfileDetails ( val userDetails : LoggedInUserDetails,
    val posts: Int,
    val friends: Int)



