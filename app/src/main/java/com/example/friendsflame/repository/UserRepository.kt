package com.example.friendsflame.repository

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.example.friendsflame.Comments
import com.example.friendsflame.FriendRequestsUsers
import com.example.friendsflame.FriendsProfileDetails

import com.example.friendsflame.LoggedInUserDetails
import com.example.friendsflame.PostDetails
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONException
import java.io.File
import java.util.UUID
import kotlin.random.Random

class UserRepository {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val user = mAuth.currentUser


    fun checkUserName(username: String, onComplete: (Boolean, String) -> Unit) {
        database.collection("users").whereEqualTo("username", username).get()
            .addOnSuccessListener { document ->
                if (document.isEmpty) {
                    onComplete(true, "Proceed with Registration")
                } else {
                    onComplete(false, "Username Already Exists")
                }
            }
    }


    fun firebaseAuthWithGoogle(
        username: String,
        account: GoogleSignInAccount,
        onComplete: (Boolean, String?, LoggedInUserDetails?) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(credential).addOnSuccessListener {
            CoroutineScope(Dispatchers.Main).launch {
                val isNewUser = mAuth.currentUser!!.metadata?.creationTimestamp == mAuth.currentUser!!.metadata?.lastSignInTimestamp
                withContext(Dispatchers.Main){
                    val userDetails = LoggedInUserDetails(
                        username = username,
                        name = account.displayName ?: "",
                        email = account.email ?: "",
                        userId = mAuth.currentUser!!.uid,
                        profileUrl = account.photoUrl.toString(),
                    )
                    if (isNewUser) {
                        Log.d("user id now", mAuth.currentUser.toString())
                        database.collection("users").document(mAuth.currentUser!!.uid).set(userDetails)
                            .addOnSuccessListener {
                                onComplete(true, null, userDetails)

                            }
                    } else {
                        database.collection("users").document(mAuth.currentUser?.uid.toString()).get()
                            .addOnSuccessListener { document ->
                                val user = document.toObject(LoggedInUserDetails::class.java)
                                onComplete(true, null, user)
                            }
                    }
                }
            }
        }.addOnFailureListener{
            Log.d("JARVIS","Failure: $it")
        }
    }





    fun firebaseAuthWithFacebook(
        fetchedUsername: String,
        fullName: String,
        email: String,
        profile: String,
        token: AccessToken,
        onComplete: (Boolean, String?, LoggedInUserDetails?) -> Unit
    ) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth.signInWithCredential(credential).addOnSuccessListener {

            val isNewUser =
                mAuth.currentUser!!.metadata?.creationTimestamp == mAuth.currentUser!!.metadata?.lastSignInTimestamp


            val userDetails =
                LoggedInUserDetails(
                    username = fetchedUsername.lowercase(),
                    name = fullName,
                    email = email,
                    userId = mAuth.currentUser!!.uid,
                    profileUrl = profile,
                )

            Log.d("facebookusername", userDetails.toString())

            if (isNewUser) {
                database.collection("users").document(mAuth.currentUser!!.uid).set(userDetails)
                    .addOnSuccessListener {
                        onComplete(true, null, userDetails)
                    }
            } else {
                database.collection("users").document(mAuth.currentUser?.uid.toString()).get()
                    .addOnSuccessListener { document ->
                        val user = document.toObject(LoggedInUserDetails::class.java)
                        onComplete(true, null, user)
                    }
            }
        }


            .addOnFailureListener {
                onComplete(false, it.message, null)
            }
    }


    fun registerUser(
        username: String,
        email: String,
        password: String,
        name: String,
        onComplete: (Boolean, String?, LoggedInUserDetails?) -> Unit
    ) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userdId = FirebaseAuth.getInstance().currentUser?.uid
                val userData = hashMapOf(
                    "username" to username,
                    "email" to email,
                    "name" to name,
                    "userId" to mAuth.currentUser?.uid,
                    "profileUrl" to ""
                )

                database.collection("users").document(userdId!!).set(userData)
                    .addOnSuccessListener {
                        val userDetails = LoggedInUserDetails(username, name, email, userdId, "")
                        onComplete(true, null, userDetails)
                    }
                    .addOnFailureListener {
                        onComplete(false, it.message.toString(), null)
                    }

            } else {
                onComplete(false, "email already exists", null)
            }
        }
            .addOnFailureListener { e ->
                onComplete(false, e.message.toString(), null)
            }
    }


    fun loginUser(
        loginCredential: String,
        password: String,
        onComplete: (Boolean, String?, LoggedInUserDetails?) -> Unit
    ) {
        if (loginCredential.contains("@")) {
            mAuth.signInWithEmailAndPassword(loginCredential, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        database.collection("users").document(mAuth.currentUser?.uid.toString())
                            .get().addOnSuccessListener { document ->
                                val user = document.toObject(LoggedInUserDetails::class.java)
                                onComplete(true, null, user)
                            }
                    }
                }.addOnFailureListener {
                    onComplete(false, it.message.toString(), null)
                }
        } else {
            database.collection("users").whereEqualTo("username", loginCredential).get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val email = documents.first().getString("email")
                        email?.let {
                            mAuth.signInWithEmailAndPassword(it, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        database.collection("users")
                                            .document(mAuth.currentUser?.uid.toString()).get()
                                            .addOnSuccessListener { document ->
                                                val user =
                                                    document.toObject(LoggedInUserDetails::class.java)
                                                onComplete(true, null, user)
                                            }
                                    }
                                }.addOnFailureListener {
                                    onComplete(false, it.message.toString(), null)
                                }
                        }
                    } else {
                        onComplete(false, "Account Doesn't Exists!", null)
                    }
                }
                .addOnFailureListener { e ->
                    onComplete(false, e.message, null)
                }
        }
    }


    fun changePassword(password: String, onComplete: (Boolean, String) -> Unit) {
        mAuth.currentUser!!.updatePassword(password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(true, "Password Changed Successfully")
            }
        }
            .addOnFailureListener {
                onComplete(false, it.message.toString())
            }
    }

    fun forgotPassword(email: String) {
        mAuth.sendPasswordResetEmail(email)
    }


    fun uploadPost(imageUri: Uri, caption: String, onComplete: (Boolean, String) -> Unit) {
        val imageRef =
            FirebaseStorage.getInstance().reference.child("post_images/${user!!.uid}/${UUID.randomUUID()}.jpg")
        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    database.collection("users").document(user!!.uid).collection("posts").get()
                        .addOnSuccessListener { posts ->
                            val postId = UUID.randomUUID().toString()
                            val post = hashMapOf(
                                "userId" to user.uid,
                                "caption" to caption,
                                "imageUrl" to downloadUri.toString(),
                                "timestamp" to com.google.firebase.Timestamp.now(),
                                "postId" to postId
                            )
                            database.collection("users").document(user.uid).collection("posts")
                                .document(postId).set(post).addOnSuccessListener {
                                    onComplete(true, "Post Added Successfully")
                                }
                                .addOnFailureListener {
                                    onComplete(false, it.message.toString())
                                }
                        }
                }
                    .addOnFailureListener { e ->
                        onComplete(false, e.message ?: "Failed to get image URL.")
                    }
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message ?: "Image upload failed.")
            }
    }

    fun fetchPosts(onComplete: (ArrayList<PostDetails>, String?) -> Unit) {
        val postList = ArrayList<PostDetails>()
        database.collection("users").document(user!!.uid).collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val totalIndex = documents.size()
                var currentIndex = 0

                for (document in documents) {

                    val post = document.toObject(PostDetails::class.java)

                    fetchUsersLiked(post.userId, post.postId) { usersLiked ->

                        post.isPostLiked = usersLiked.contains(user.uid)
                        post.likeCount = usersLiked.size

                        database.collection("users").document(user.uid).collection("savedPosts")
                            .document(post.postId).get().addOnCompleteListener { task ->

                                if (task.result.exists()) {
                                    post.isPostSaved = true
                                }
                                fetchUserDetails(post.userId) { user ->
                                    Log.d("for comment", "getting loop 3")
                                    post.username = user.username
                                    post.profileUrl = user.profileUrl


                                    if (!postList.contains(post)) {
                                        postList.add(post)
                                    }
                                    currentIndex++

                                    if (currentIndex == totalIndex) {
                                        Log.d("Posts Adding of mine", postList.toString())
                                        onComplete(postList, null)
                                    }
                                }
                            }


                    }


                }

            }

            .addOnFailureListener { e ->
                onComplete(arrayListOf(), e.message)
            }
    }


//    fun fetchPostsOfFriends(onComplete: (ArrayList<PostDetails>, String?) -> Unit){
//        val postList = ArrayList<PostDetails> ()
//        getFriends {
//                friends ->
//            if(friends.isEmpty()){
//                onComplete(arrayListOf(),null)
//            }
//            else{
//                for(friend in friends){
//                    database.collection("users").document(friend).collection("posts").orderBy("timestamp", Query.Direction.DESCENDING).get().addOnSuccessListener{
//                            documents ->
//                        val totalIndex = documents.size()-1
//                        var currentIndex = 0
//                        for(document in documents){
//                            val post = document.toObject(PostDetails::class.java)
//
//                            fetchUsersLiked(post.userId, post.postId){
//                                    usersLiked -> if(usersLiked.contains(user!!.uid)){
//                                post.isPostLiked = true
//                            }
//                            else {
//                                post.isPostLiked = false
//                            }
//                                fetchUserDetails(post.userId){
//                                        user -> post.username = user.username
//                                    post.profileUrl = user.profileUrl
//                                    Log.d("Friends Post Fetching", post.toString())
//
//                                    if (!postList.contains(post)){
//                                        postList.add(post)
//                                    }
//
//                                    if(currentIndex == totalIndex){
//                                        Log.d("Friends Post Adding", postList.toString())
//                                        onComplete(postList,null)
//                                    }
//                                    currentIndex++
//
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }


    fun fetchPostsOfFriends(onComplete: (ArrayList<PostDetails>, String?) -> Unit) {
        val postList = ArrayList<PostDetails>()
        var friendsProcessed = 0

        getFriends { friends ->
            if (friends.isEmpty()) {
                onComplete(arrayListOf(), null)
            } else {
                friends.forEach { friendId ->
                    database.collection("users").document(friendId)
                        .collection("posts").orderBy("timestamp", Query.Direction.DESCENDING)
                        .get().addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                friendsProcessed++
                                if (friendsProcessed == friends.size) {
                                    onComplete(postList, null)
                                }
                            } else {
                                var postsProcessed = 0
                                for (document in documents) {
                                    val post = document.toObject(PostDetails::class.java)
                                    fetchUsersLiked(post.userId, post.postId) { usersLiked ->
                                        post.isPostLiked = usersLiked.contains(user!!.uid)
                                        post.likeCount = usersLiked.size
                                        Log.d("Totoal likes of this ${post.caption}", usersLiked.size.toString())
                                        database.collection("users").document(user!!.uid)
                                            .collection("savedPosts").document(post.postId)
                                            .get().addOnCompleteListener { task ->
                                                if (task.result.exists()) {
                                                    post.isPostSaved = true
                                                }
                                                fetchUserDetails(post.userId) { userDetails ->
                                                    post.username = userDetails.username
                                                    post.profileUrl = userDetails.profileUrl
                                                    if (!postList.contains(post)) {
                                                        Log.d("Adding Post here: ", post.toString())
                                                        postList.add(post)
                                                    }
                                                    postsProcessed++
                                                    if (postsProcessed == documents.size()) {
                                                        friendsProcessed++
                                                        if (friendsProcessed == friends.size) {
                                                            onComplete(postList, null)
                                                        }
                                                    }
                                                }
                                            }
                                    }
                                }
                            }
                        }
                }
            }
        }
    }


    fun fetchPostByUserId(userId: String, onComplete: (ArrayList<PostDetails>) -> Unit) {
        val postList = ArrayList<PostDetails>()
        database.collection("users").document(userId).collection("posts").get()
            .addOnSuccessListener { posts ->
                if (posts.isEmpty) {
                    onComplete(ArrayList(emptyList()))
                } else {
                    for (document in posts) {
                        val post = document.toObject(PostDetails::class.java)
                        fetchUsersLiked(userId, post.postId) { usersLiked ->
                            post.isPostLiked = usersLiked.contains(user!!.uid)
                            post.likeCount = usersLiked.size

                            database.collection("users").document(user.uid).collection("savedPosts")
                                .document(post.postId).get().addOnCompleteListener { task ->
                                    if (task.result.exists()) {
                                        post.isPostSaved = true
                                    }
                                    fetchUserDetails(post.userId) { user ->
                                        post.username = user.username
                                        post.profileUrl = user.profileUrl
                                        if(!postList.contains(post)){
                                            postList.add(post)
                                        }
                                        if(postList.size==posts.size()){
                                            onComplete(postList)
                                        }
                                    }
                                }
                        }
                    }
                }
            }
    }

    fun fetchPostByPostId(userId: String, postId: String, onComplete: (PostDetails) -> Unit) {
        database.collection("users").document(userId).collection("posts").document(postId).get()
            .addOnSuccessListener {
                val post = it.toObject(PostDetails::class.java)
                fetchUsersLiked(userId, postId) { usersLiked ->
                    post!!.isPostLiked = usersLiked.contains(user!!.uid)
                    post.likeCount = usersLiked.size



                    database.collection("users").document(user.uid).collection("savedPosts")
                        .document(postId).get().addOnCompleteListener { task ->
                            if (task.result.exists()) {
                                post.isPostSaved = true
                            }
                            fetchUserDetails(post.userId) { user ->

                                post.username = user.username
                                post.profileUrl = user.profileUrl
                                onComplete(post)

                            }
                        }


//                            fetchUserDetails(post.userId){
//                                    userData ->  post.profileUrl = userData.profileUrl
//                                post.username = userData.username
//                                onComplete(post)
//                            }

                }
            }
    }


    fun fetchUsersLiked(userId: String, postId: String, onComplete: (ArrayList<String>) -> Unit) {
        val likedUsersList = ArrayList<String>()
        database.collection("users").document(userId)
            .collection("posts").document(postId)
            .collection("likes").get().addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    onComplete(ArrayList(emptyList()))
                } else {
                    for (document in documents) {
                        val user = document.id
                        likedUsersList.add(user)
                        onComplete(likedUsersList)
                    }
                }
            }
            .addOnFailureListener {
                onComplete(ArrayList(emptyList()))
            }
    }


    fun toggleLikeFunctionality(userId: String, postId: String) {
        fetchUsersLiked(userId, postId) { likedUsers ->
            if (likedUsers.contains(user?.uid)) {
                val postLikesRef = database.collection("users").document(userId)
                    .collection("posts").document(postId)
                    .collection("likes").document(user!!.uid)
                postLikesRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        postLikesRef.delete()
                    }
                }
            } else {
                database.collection("users").document(userId)
                    .collection("posts").document(postId)
                    .collection("likes").document(user!!.uid).set(mapOf("liked" to true))
            }
        }
    }


    fun postComment(
        userId: String,
        postId: String,
        comment: String,
        onComplete: (Boolean, String) -> Unit
    ) {

        val commentId = UUID.randomUUID().toString()
        val postCommentDetails = hashMapOf(
            "commentId" to commentId,
            "userId" to user!!.uid,
            "comment" to comment,
            "timestamp" to com.google.firebase.Timestamp.now(),
        )

        database.collection("users").document(userId).collection("posts")
            .document(postId).collection("comments").document(commentId).set(postCommentDetails)
            .addOnSuccessListener {
                onComplete(true, commentId)
            }
    }


    fun fetchComments(postDetails: PostDetails, onComplete: (ArrayList<Comments>) -> Unit) {
        val commentsList = ArrayList<Comments>()

        database.collection("users").document(postDetails.userId).collection("posts")
            .document(postDetails.postId).collection("comments").get()
            .addOnSuccessListener { documents ->

                if (documents.isEmpty) {
                    onComplete(ArrayList(emptyList()))
                }

                for (document in documents) {
                    val comment = document.toObject(Comments::class.java)
                    fetchUserDetails(comment.userId) { user ->
                        comment.profileUrl = user.profileUrl
                        comment.username = user.username

                        comment.isEditable = comment.userId == this.user!!.uid
                        comment.isDeletable =
                            postDetails.userId == this.user.uid || comment.userId == this.user.uid

                        comment.commentId = document.id
                        commentsList.add(comment)

                        fetchUserDetails(postDetails.userId) { user ->

                            postDetails.username = user.username
                            postDetails.profileUrl = user.profileUrl

                        }
                        if (documents.size() == commentsList.size) {
                            Log.d("Comments Fetched", commentsList.toString())
                            onComplete(commentsList)

                        }
                    }
                }
            }
    }

    fun updateComment(userId: String, postId: String, comment: String, commentId: String) {
        database.collection("users").document(userId).collection("posts").document(postId)
            .collection("comments").document(commentId).update("comment", comment)
    }

    fun deleteComment(userId: String, postId: String, commentId: String) {
        database.collection("users").document(userId).collection("posts").document(postId)
            .collection("comments").document(commentId).delete()
    }

//    fun fetchSavedPost(userId: String, postId: String, onComplete: (ArrayList<String>) -> Unit){
//        val savedUsersList = ArrayList<String>()
//        database.collection("savedPosts").document(userId).collection(postId).get().addOnSuccessListener {
//            documents -> if(documents.isEmpty){
//                onComplete(ArrayList(emptyList()))
//        }
//            else{
//                for(document in documents){
//                    val user = document.id
//                    savedUsersList.add(user)
//                }
//            onComplete(savedUsersList)
//        }
//        }
//    }


//    fun savePost(userId: String, postId: String){
//
//        fetchSavedPost(userId,postId){
//            usersList -> if(usersList.contains(user?.uid)){
//            val savedRef = database.collection("savedPosts").document(userId).collection(postId).document(user!!.uid)
//            savedRef.get().addOnCompleteListener {
//                    task -> if(task.result.exists()){
//                savedRef.delete()
//            }
//        }
//        }
//            else{
//            database.collection("savedPosts").document(userId).collection(postId).document(user!!.uid).set(
//                hashMapOf("saved post" to true)
//            )
//        }
//        }
//    }

    fun fetchSavedPosts(onComplete: (ArrayList<Pair<String, String>>) -> Unit) {
        database.collection("users").document(user!!.uid).collection("savedPosts").get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    onComplete(ArrayList(emptyList()))
                } else {
                    val userPostMap = ArrayList<Pair<String, String>>()
                    for (document in documents) {
                        val userId = document.getString("userId")
                        val postId = document.id
                        userPostMap.add(Pair(userId!!, postId))
                    }
                    onComplete(userPostMap)
                }
            }
    }

    fun fetchSavedPostsByPostIds(onComplete: (ArrayList<PostDetails>) -> Unit) {
        val postList = ArrayList<PostDetails>()
        fetchSavedPosts { savedPostsMap ->
            if (savedPostsMap.isEmpty()) {
                onComplete(ArrayList(emptyList()))
            } else {
                savedPostsMap.forEach { (userId, postId) ->
                    database.collection("users").document(userId)
                        .collection("posts").document(postId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val post = document.toObject(PostDetails::class.java)
                                if (post != null) {
                                    fetchUsersLiked(userId, post.postId) { usersLiked ->
                                        post.isPostLiked = usersLiked.contains(user!!.uid)
                                        post.likeCount = usersLiked.size

                                        database.collection("users").document(user.uid).collection("savedPosts")
                                            .document(post.postId).get().addOnCompleteListener { task ->
                                                if (task.result.exists()) {
                                                    post.isPostSaved = true
                                                }
                                                fetchUserDetails(post.userId) { user ->
                                                    post.username = user.username
                                                    post.profileUrl = user.profileUrl
                                                    if(!postList.contains(post)){
                                                        postList.add(post)
                                                    }
                                                    if(postList.size==savedPostsMap.size){
                                                        onComplete(postList)
                                                    }
                                                }
                                            }
                                    }

                                }
                            }
                        }
                }
            }
        }

    }


    fun savePost(userId: String, postId: String) {
        val savedRef = database.collection("users").document(user!!.uid).collection("savedPosts")
            .document(postId)
        savedRef.get().addOnCompleteListener { task ->
            if (task.result.exists()) {
                savedRef.delete()
            } else {
                database.collection("users").document(user.uid).collection("savedPosts")
                    .document(postId).set(hashMapOf("userId" to userId, "savedPost" to true))
            }
        }
    }


    fun fetchUserDetails(userId: String, onComplete: (LoggedInUserDetails) -> Unit) {
        database.collection("users").document(userId).get().addOnSuccessListener {
            val userDetails = it.toObject(LoggedInUserDetails::class.java)
            onComplete(userDetails!!)
        }
    }

    fun fetchUsersWholeDetails(userId: String, onComplete: (FriendsProfileDetails) -> Unit) {
        var postSize: Int
        var friendsSize: Int
        var userDetails: LoggedInUserDetails
        database.collection("users").document(userId).get().addOnSuccessListener { it ->
            userDetails = it.toObject(LoggedInUserDetails::class.java)!!
            database.collection("users").document(userId).collection("Friends").get()
                .addOnSuccessListener { friends ->
                    friendsSize = friends.size()
                    database.collection("users").document(userId).collection("posts").get()
                        .addOnSuccessListener { posts ->
                            postSize = posts.size()
                            val data = FriendsProfileDetails(userDetails, postSize, friendsSize)
                            onComplete(data)
                        }
                }
        }
    }

    fun getFriends(onComplete: (ArrayList<String>) -> Unit) {
        val friendsIds = ArrayList<String>()
        database.collection("users").document(user!!.uid).collection("Friends").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userID = document.getString("friendUserId") ?: "No Friends"
                    friendsIds.add(userID)
                    Log.d("Friends are: ", userID)
                }
                Log.d("friends list are: ", friendsIds.toString())
                onComplete(friendsIds)
            }
    }

    fun getFriendsWithDetails(onComplete: (ArrayList<FriendRequestsUsers>) -> Unit) {
        val friendsList = ArrayList<FriendRequestsUsers>()
        getFriends { friends ->
            if (friends.isEmpty()) {
                onComplete(ArrayList(emptyList()))
            } else {
                for (friend in friends) {
                    database.collection("users").document(friend).get().addOnSuccessListener {
                        val friendDetails = it.toObject(FriendRequestsUsers::class.java)
                        friendsList.add(friendDetails!!)
                        onComplete(friendsList)
                    }
                }
            }
        }
    }

    fun getFriendsOfFriend(userId: String,onComplete: (ArrayList<LoggedInUserDetails>) -> Unit){
        val friendsList = ArrayList<LoggedInUserDetails>()
        database.collection("users").document(userId).collection("Friends").get().addOnSuccessListener {
            documents -> for(document in documents){
                fetchUserDetails(document.id){
                    if(!friendsList.contains(it)){
                        friendsList.add(it)
                    }
                    if(documents.size()==friendsList.size){
                        Log.d("sending friends Bottom Sheet", friendsList.toString())
                        onComplete(friendsList)
                    }
                }

        }
        }
    }

    fun uploadProfileImage(imageUri: File, onComplete: (String?) -> Unit) {
        val storageRef =
            FirebaseStorage.getInstance().reference.child("profile_images/${mAuth.currentUser?.uid}.jpg")
        storageRef.putFile(Uri.fromFile(imageUri))
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                storageRef.downloadUrl
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result.toString()
                    onComplete(downloadUri)
                } else {
                    onComplete(null)
                }
            }
    }


    fun updateUserData(user: HashMap<String, String>, onComplete: (Boolean, String) -> Unit) {
        database.collection("users").document(mAuth.currentUser!!.uid)
            .update(user as Map<String, Any>).addOnSuccessListener {
                onComplete(true, "Data Updated Successfully")
            }
            .addOnFailureListener {
                onComplete(true, it.message.toString())
            }
    }


    fun sendFriendRequest(receiverId: String, onComplete: (Boolean, String) -> Unit) {
        val friendRequest = hashMapOf(
            "senderId" to mAuth.currentUser!!.uid,
            "receiverId" to receiverId,
            "status" to "pending"
        )
        database.collection("FriendRequests")
            .add(friendRequest)
            .addOnSuccessListener {
                onComplete(true, "Friend request sent successfully.")
            }
            .addOnFailureListener { e ->
                onComplete(false, "Error sending friend request: ${e.message}")
            }
    }


    fun fetchAllUsersWithStatus(onComplete: (ArrayList<FriendRequestsUsers>) -> Unit) {
        val userList = ArrayList<FriendRequestsUsers>()
        val currentUserId = mAuth.currentUser?.uid
        val friendRequestsMap = mutableMapOf<String, String>()


        database.collection("FriendRequests")
            .whereEqualTo("receiverId", currentUserId)
            .get()
            .continueWithTask { task ->

                for (document in task.result) {
                    val senderId = document.getString("senderId") ?: continue
                    friendRequestsMap[senderId] = document.getString("status") ?: "Unknown"
                }

                database.collection("FriendRequests")
                    .whereEqualTo("senderId", currentUserId)
                    .get()
            }
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    for (document in task.result) {
                        val receiverId = document.getString("receiverId") ?: continue
                        friendRequestsMap[receiverId] = document
                            .getString("status") ?: "Unknown"
                    }

                    database.collection("users").get().addOnSuccessListener { userDocuments ->
                        for (userDoc in userDocuments) {
                            val userData = userDoc.toObject(FriendRequestsUsers::class.java)
                            val uid = userData.userId

                            if (uid != currentUserId) {
                                userData.status = when {
                                    friendRequestsMap.containsKey(uid) -> friendRequestsMap[uid]
                                        ?: "Unknown"

                                    else -> "Add Friend"
                                }
                                userList.add(userData)
                            }
                        }
                        onComplete(userList)
                    }
                } else {
                    onComplete(userList)
                }
            }
    }


    private fun getSentFriendRequests(onComplete: (Map<String, String>) -> Unit) {
        val sentRequests = mutableMapOf<String, String>()
        database.collection("FriendRequests")
            .whereEqualTo("senderId", mAuth.currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val receiverId = doc.getString("receiverId") ?: continue
                    sentRequests[receiverId] = doc.getString("status").toString()
                }
                Log.d("sent friend requests", sentRequests.toString())
                onComplete(sentRequests)
            }
            .addOnFailureListener {
                onComplete(sentRequests)
            }
    }


//    fun searchUsersWithStatus(query: String, onComplete: (ArrayList<FriendRequestsUsers>) -> Unit) {
//        val searchResults = ArrayList<FriendRequestsUsers>()
//        getSentFriendRequests { sentRequests ->
//            database.collection("users").whereNotEqualTo("userId", mAuth.currentUser!!.uid).get()
//                .addOnSuccessListener { documents ->
//                    for (document in documents) {
//                        val userData = document.toObject(FriendRequestsUsers::class.java)
//                        if (userData.username.contains(query, ignoreCase = true) ||
//                            userData.name.contains(query, ignoreCase = true)) {
//                            userData.status = sentRequests[userData.userId] ?: "Add Friend"
//                            Log.d("Searches Result ${userData.name}", userData.status)
//                            searchResults.add(userData)
//                        }
//                    }
//                    onComplete(searchResults)
//                }
//        }
//    }

//        fun searchUsersWithStatus(query: String, onComplete: (ArrayList<FriendRequestsUsers>) -> Unit) {
//        val searchResults = ArrayList<FriendRequestsUsers>()
//        getSentFriendRequests { sentRequests ->
//            getReceivedFriendRequests {
//                receivedRequests ->      database.collection("users").whereNotEqualTo("userId", mAuth.currentUser!!.uid).get()
//                .addOnSuccessListener { documents ->
//                    for (document in documents) {
//                        val userData = document.toObject(FriendRequestsUsers::class.java)
//                        if (userData.username.contains(query, ignoreCase = true) ||
//                            userData.name.contains(query, ignoreCase = true)) {
//                            if(sentRequests.containsValue(userData.userId)){
//                                userData.status = "pending"
//                            }
//                            userData.status = sentRequests[userData.userId] ?: "Add Friend"
//                            Log.d("Searches Result ${userData.name}", userData.status)
//                            searchResults.add(userData)
//                        }
//                    }
//                    onComplete(searchResults)
//                }
//            }
//        }
//    }

    fun searchUsersWithStatus(query: String, onComplete: (ArrayList<FriendRequestsUsers>) -> Unit) {
        val currentUserId = mAuth.currentUser?.uid ?: return
        val searchResults = ArrayList<FriendRequestsUsers>()

        getReceivedFriendRequests { receivedRequests ->
            getSentFriendRequests { sentRequests ->


                val allFriendRequests = receivedRequests.toMutableMap()
                allFriendRequests.putAll(sentRequests)

                Log.d("All Friends Requests", allFriendRequests.toString())


                database.collection("users")
                    .whereNotEqualTo("userId", currentUserId)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            val userData = document.toObject(FriendRequestsUsers::class.java)
                            if (userData.username.contains(query, ignoreCase = true) ||
                                userData.name.contains(query, ignoreCase = true)
                            ) {

                                Log.d("Search Results for ${userData.username}", userData.status)

                                if (userData.status == "Accepted") {
                                    userData.status = "Accepted"
                                } else {
                                    userData.status = when {
                                        receivedRequests.containsKey(userData.userId) && sentRequests.containsKey(
                                            userData.userId
                                        ) -> "pending"

                                        else -> "Add Friend"
                                    }
                                }
                                searchResults.add(userData)
                            }
                        }
                        onComplete(searchResults)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(
                            "SearchUsersWithStatus",
                            "Error searching users: ${exception.message}"
                        )
                        onComplete(searchResults)
                    }
            }
        }
    }

    private fun getReceivedFriendRequests(onComplete: (Map<String, String>) -> Unit) {
        val receivedRequests = mutableMapOf<String, String>()
        database.collection("FriendRequests")
            .whereEqualTo("receiverId", user!!.uid).whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val senderId = doc.getString("senderId") ?: continue
                    receivedRequests[senderId] = doc.getString("status") ?: "Unknown"
                }
                Log.d("receivedRequests",receivedRequests.toString())
                onComplete(receivedRequests)
            }
            .addOnFailureListener {
                onComplete(emptyMap())
            }
    }


    fun getFriendRequests(onComplete: (List<FriendRequestsUsers>) -> Unit) {
        getReceivedFriendRequests { receivedRequests ->
            if (receivedRequests.isEmpty()) {
                onComplete(emptyList())
                Log.d("received friend requests 2", receivedRequests.toString())
            }

            val userDataList = mutableListOf<FriendRequestsUsers>()
            receivedRequests.forEach { (senderId, status) ->
                database.collection("users").document(senderId).get()
                    .addOnSuccessListener { document ->
                        val userData = document.toObject(FriendRequestsUsers::class.java)
                        userData!!.status = status
                        userDataList.add(userData)

                        if (userDataList.size == receivedRequests.size) {
                            Log.d("received friend requests", userDataList.toString())

                            onComplete(userDataList)
                        }
                    }
            }
        }
    }


    fun acceptFriend(friendUserId: String, onComplete: (Boolean, String) -> Unit) {
        val userData = hashMapOf("friendUserId" to friendUserId)
        val userData2 = hashMapOf("friendUserId" to user!!.uid)


        database.collection("users").document(user.uid).collection("Friends").document(friendUserId)
            .set(userData)
            .addOnSuccessListener {
                database.collection("users").document(friendUserId).collection("Friends")
                    .document(user.uid).set(userData2).addOnCompleteListener {
                        database.collection("FriendRequests").whereEqualTo("receiverId", user.uid)
                            .whereEqualTo("senderId", friendUserId).get()
                            .addOnSuccessListener { documents ->
                                for (document in documents) {
                                    database.collection("FriendRequests").document(document.id)
                                        .update("status", "Accepted")
                                        .addOnSuccessListener {
                                            onComplete(true, "Friend Added")
                                        }
                                        .addOnFailureListener { e ->
                                            onComplete(false, "Update failed: ${e.message}")
                                        }
                                }
                            }
                    }
                    .addOnFailureListener { e ->
                        onComplete(false, "Set failed: ${e.message}")
                    }
            }
    }


    fun logout() {
        mAuth.signOut()
        Firebase.auth.signOut()


    }


}

