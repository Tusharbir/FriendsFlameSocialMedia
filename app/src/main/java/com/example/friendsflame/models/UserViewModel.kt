package com.example.friendsflame.models

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.friendsflame.repository.UserRepository

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.friendsflame.Comments
import com.example.friendsflame.FriendRequestsUsers
import com.example.friendsflame.FriendsProfileDetails
import com.example.friendsflame.LoggedInUserDetails
import com.example.friendsflame.PostDetails
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random

class UserViewModel : ViewModel() {
    private val repository = UserRepository()

    val status = MutableLiveData<Pair<Boolean, String?>>()

    val usernameStatus = MutableLiveData<Pair<Boolean, String?>>()

    val _loggedInUserDetails = MutableLiveData<LoggedInUserDetails?>()
    val loggedInUserDetails: LiveData<LoggedInUserDetails?> = _loggedInUserDetails

    fun registerUser(username: String, email: String, password: String, name: String) {
        repository.checkUserName(username) { boolean, message ->
            if (boolean) {
                repository.registerUser(
                    username,
                    email,
                    password,
                    name
                ) { success, message, userDetails ->
                    status.postValue(Pair(success, message))
                    _loggedInUserDetails.postValue(userDetails)
                }
            } else {
                usernameStatus.postValue(Pair(false, message))
                status.postValue(Pair(false,message))
            }
        }
    }

//    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
//
//        var fetchedusername = (account.givenName+account.displayName)
//        fetchedusername = fetchedusername.replace(" ", "_").lowercase()
//
//        val random = Random.nextInt(1000,10000).toString()
//        repository.checkUserName(fetchedusername){
//                boolean, message -> if(!boolean){
//                    fetchedusername = account.givenName+account.familyName+random
//                    repository.firebaseAuthWithGoogle(fetchedusername, account){
//                    success, message, user ->
//                status.postValue(Pair(success,message))
//                _loggedInUserDetails.postValue(user)
//            }
//        }
//            else{
//            repository.firebaseAuthWithGoogle(fetchedusername, account){
//                    success, message, user ->
//                status.postValue(Pair(success,message))
//                _loggedInUserDetails.postValue(user)
//            }
//        }
//        }
//    }
//fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
//    viewModelScope.launch {
//        var fetchedUsername = (account.givenName + account.displayName).replace(" ", "_").lowercase()
//        val random = Random.nextInt(1000, 10000).toString()
//
//        checkAndAuthenticateUser(fetchedUsername, account, random)
//    }
//}

//    private suspend fun checkAndAuthenticateUser(username: String, account: GoogleSignInAccount, random: String) {
//        val usernameAvailable = suspendCoroutine<Boolean> { continuation ->
//            repository.checkUserName(username) { available, _ ->
//                continuation.resume(available)
//            }
//        }
//
//        val finalUsername = if (!usernameAvailable) {
//            account.givenName + account.familyName + random
//        } else {
//            username
//        }
//
//        try {
//            repository.firebaseAuthWithGoogle(finalUsername, account) { success, message, user ->
//                status.postValue(Pair(success, message))
//                _loggedInUserDetails.postValue(user)
//            }
//        } catch (e: Exception) {
//            status.postValue(Pair(false, e.message ?: "Error occurred"))
//        }
//    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {

        var fetchedUsername =
            (account.givenName + account.displayName).replace(" ", "_").lowercase()
        val random = Random.nextInt(1000, 10000).toString()


        CoroutineScope(Dispatchers.Main).launch {

            repository.checkUserName(fetchedUsername) { isAvailable, _ ->
                if (!isAvailable) {
                    // If username is not available, modify it
                    fetchedUsername = account.givenName + account.familyName + random
                }

                repository.firebaseAuthWithGoogle(
                    fetchedUsername,
                    account
                ) { success, message, user ->
                    status.postValue(Pair(success, message))
                    _loggedInUserDetails.postValue(user)
                }
            }


        }
    }


fun handleFacebookAccessToken(token: AccessToken) {

    val request = GraphRequest.newMeRequest(token) { jsonObject, _ ->
        try {
            val firstName = jsonObject?.getString("first_name")
            val lastName = jsonObject?.getString("last_name")
            val fullName = "$firstName $lastName"
            var email = jsonObject?.getString("email")
            var profileUrl =
                jsonObject?.getJSONObject("picture")?.getJSONObject("data")?.getString("url")

            if (email.isNullOrBlank()) {
                email = "No Email Obtained"
            }

            if (profileUrl.isNullOrBlank()) {
                profileUrl = ""
            }

            var fetchedusername = firstName + lastName
            fetchedusername = fetchedusername.replace(" ", "_").lowercase()

            val random = Random.nextInt(1000, 10000).toString()

            repository.checkUserName(fetchedusername) { boolean, message ->
                if (!boolean) {
                    fetchedusername = firstName + lastName + random
                    repository.firebaseAuthWithFacebook(
                        fetchedusername,
                        fullName,
                        email,
                        profileUrl,
                        token
                    ) { success, message, user ->
                        status.postValue(Pair(success, message))
                        _loggedInUserDetails.postValue(user)
                    }
                } else {
                    repository.firebaseAuthWithFacebook(
                        fetchedusername,
                        fullName,
                        email,
                        profileUrl,
                        token
                    ) { success, message, user ->
                        status.postValue(Pair(success, message))
                        _loggedInUserDetails.postValue(user)
                    }
                }
            }
        } catch (e: JSONException) {
            status.postValue(Pair(false, "unexpected fetching error occurred"))
        }
    }

    val parameters = Bundle()
    parameters.putString("fields", "first_name,last_name,email,picture.type(large)")
    request.parameters = parameters
    request.executeAsync()
}


fun checkUsername(username: String) {
    repository.checkUserName(username) { boolean, message ->
        usernameStatus.postValue(Pair(boolean, message))
    }
}

fun loginUser(username: String, password: String) {
    repository.loginUser(username, password) { success, message, userDetails ->
        Log.d("userdetailsin login", userDetails.toString())
        status.postValue(Pair(success, message))
        _loggedInUserDetails.postValue(userDetails)
    }
}

    fun totalFriends( onComplete: (Int) -> Unit){
        repository.getFriends { friendsList ->
            onComplete(friendsList.size)
        }
    }



fun changePassword(password: String) {
    repository.changePassword(password) { b, s ->
        status.postValue(Pair(b, s))
    }
}


fun forgotPassword(email: String) {
    repository.forgotPassword(email)
}

val postStatus = MutableLiveData<Pair<Boolean, String>>()
fun uploadPost(imageUri: Uri, captionText: String) {
    repository.uploadPost(imageUri, captionText) { success, message ->
        postStatus.postValue(Pair(success, message))
    }
}

val _fetchedPosts = MutableLiveData<ArrayList<PostDetails>>()
val posts: LiveData<ArrayList<PostDetails>> = _fetchedPosts

fun fetchPosts() {
    repository.fetchPosts { post, error ->
        _fetchedPosts.postValue(post)
    }
}

val _fetchedPostsFriends = MutableLiveData<ArrayList<PostDetails>>()
val postsFriends: LiveData<ArrayList<PostDetails>> = _fetchedPostsFriends

fun fetchPostsFriends() {
    repository.fetchPostsOfFriends { post, error ->
        _fetchedPostsFriends.postValue(post)
    }

}

    fun getFriendsOfFriend(userId: String, onComplete: (ArrayList<LoggedInUserDetails>) -> Unit){
        repository.getFriendsOfFriend(userId){
            onComplete(it)
        }
    }

fun logout() {
    repository.logout()
}


val _profileImage = MutableLiveData<String>()
val profileImageString: LiveData<String> = _profileImage

fun uploadProfileImage(image: File) {
    repository.uploadProfileImage(image) {
        _profileImage.postValue(it)
    }
}

fun updateProfileData(
    email: String,
    username: String,
    userId: String,
    name: String,
    profileUrl: String
) {
    Log.d("whether data is updated", "Inside funcition called")
    val userData = hashMapOf(
        "username" to username,
        "email" to email,
        "name" to name,
        "userId" to userId,
        "profileUrl" to profileUrl
    )
    repository.updateUserData(userData) { b, s ->
        status.postValue(Pair(b, s))
    }

}


val _fetchedUsers = MutableLiveData<ArrayList<FriendRequestsUsers>>()
val users: LiveData<ArrayList<FriendRequestsUsers>> = _fetchedUsers

fun fetchAllUsersWithStatus() {
    repository.fetchAllUsersWithStatus { usersList ->
        _fetchedUsers.postValue(usersList)
    }
}

val friendsRequestSendStatus = MutableLiveData<Pair<Boolean, String>>()
fun sendFriendRequest(toUserId: String) {
    repository.sendFriendRequest(toUserId) { friendsRequestSendingStatus, message ->
        friendsRequestSendStatus.postValue(Pair(friendsRequestSendingStatus, message))
    }
}

private val _searchResults = MutableLiveData<ArrayList<FriendRequestsUsers>>()
val searchResults: LiveData<ArrayList<FriendRequestsUsers>> = _searchResults

fun searchUsers(query: String) {
    repository.searchUsersWithStatus(query) { results ->
        _searchResults.postValue(results)
    }
}


private val _friendRequestDetails = MutableLiveData<List<FriendRequestsUsers>>()
val friendRequestDetails: LiveData<List<FriendRequestsUsers>> = _friendRequestDetails

fun getFriendRequests() {
    repository.getFriendRequests {
        _friendRequestDetails.postValue(it)
    }
}

fun acceptFriend(friendUserId: String) {
    repository.acceptFriend(friendUserId) { b, s ->
        status.postValue(Pair(b, s))
    }
}


val _friends = MutableLiveData<ArrayList<FriendRequestsUsers>>()
val friends: LiveData<ArrayList<FriendRequestsUsers>> = _friends

fun getFriends() {
    repository.getFriendsWithDetails {
        _friends.postValue(it)
    }
}


val _friendsUserProfile = MutableLiveData<FriendsProfileDetails>()
val friendsUserProfile: LiveData<FriendsProfileDetails> = _friendsUserProfile

fun fetchUserWholeDetails(userId: String) {
    repository.fetchUsersWholeDetails(userId) {
        _friendsUserProfile.postValue(it)
    }
}

val _friendsPosts = MutableLiveData<ArrayList<PostDetails>>()
val friendsPosts: LiveData<ArrayList<PostDetails>> = _friendsPosts

fun fetchPostsByUserId(userId: String) {
    repository.fetchPostByUserId(userId) {
        _friendsPosts.postValue(it)
    }
}


fun fetchPostByPostId(userId: String, postId: String, onComplete: (PostDetails) -> Unit) {

    repository.fetchPostByPostId(userId, postId) {
        onComplete(it)
    }
}


fun toggleLikeFunctionality(userId: String, postId: String) {
    repository.toggleLikeFunctionality(userId, postId)
}


val _totalLikes = MutableLiveData<String>()
val totalLikes: MutableLiveData<String> = _totalLikes
fun getTotalLikes(userId: String, postId: String, onComplete: (Int) -> Unit) {
    repository.fetchUsersLiked(userId, postId) {
        onComplete(it.size)
    }
}

fun savePost(userId: String, postId: String) {
    repository.savePost(userId, postId)
}


val _savedPosts = MutableLiveData<ArrayList<PostDetails>>()
val savedPosts: MutableLiveData<ArrayList<PostDetails>> = _savedPosts
fun fetchSavedPosts() {
    repository.fetchSavedPostsByPostIds {
        _savedPosts.postValue(it)
    }
}


fun postComment(userId: String, postId: String, comment: String, onComplete: (String) -> Unit) {
    repository.postComment(userId, postId, comment) { boolean, message ->
        onComplete(message)
    }
}


private val _comments = MutableLiveData<ArrayList<Comments>>()
val comments: LiveData<ArrayList<Comments>> = _comments
fun fetchComments(postDetails: PostDetails) {
    repository.fetchComments(postDetails) {
        _comments.postValue(it)
    }
}

fun updateComment(userId: String, postId: String, comment: String, commentId: String) {
    repository.updateComment(userId, postId, comment, commentId)
}

fun deleteComment(userId: String, postId: String, commentId: String) {
    repository.deleteComment(userId, postId, commentId)
}

    fun getLikesList(userId: String, postId: String, onComplete: (ArrayList<LoggedInUserDetails>) -> Unit) {
        val usersList = ArrayList<LoggedInUserDetails>()
        repository.fetchUsersLiked(userId,postId){
            ids -> for(id in ids){
                repository.fetchUserDetails(id){
                    if(!usersList.contains(it)){
                        usersList.add(it)
                    }
                    if(ids.size==usersList.size)
                    {
                        onComplete(usersList)
                    }
                }

        }
        }

    }


}