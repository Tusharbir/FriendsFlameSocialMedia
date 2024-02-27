package com.example.friendsflame.utils

import android.content.Context
import android.widget.Toast
import com.example.friendsflame.LoggedInUserDetails
import com.google.gson.Gson
import com.google.gson.GsonBuilder

object SharedPreferncesUtils {
    val userDetailsFile: String = "UserDetailsFile"
    val userDetailsJson : String = "userDetailsJson"

    val profileImageUrl: String = "profileImageString"
    val profileImageString : String = "profileImageString"

    fun getSharedData(context: Context): LoggedInUserDetails?
    {
        val sharedPreferences = context.getSharedPreferences(userDetailsFile,Context.MODE_PRIVATE)
        val userDetails =sharedPreferences.getString(userDetailsJson,null)
        return if(userDetails != null)
        {
            Gson().fromJson(userDetails, LoggedInUserDetails::class.java)
        }
        else
        {
            null
        }
    }


     fun saveUserDetails(context: Context, userDetails: LoggedInUserDetails)
    {
        val sharedPreferences = context.getSharedPreferences(userDetailsFile, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val userDetailsData =GsonBuilder().create().toJson(userDetails)
        editor.putString(userDetailsJson, userDetailsData )
        editor.apply()
    }

    fun saveProfileImage(context: Context, url: String){
        val sharedPreferences = context.getSharedPreferences(profileImageUrl, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(profileImageString,url)
        editor.apply()
    }

    fun getProfileImage(context: Context):String?
    {
        val sharedPreferences = context.getSharedPreferences(profileImageUrl, Context.MODE_PRIVATE)
        val imageUrlString = sharedPreferences.getString(profileImageString,null)
        return if(imageUrlString!=null){
            imageUrlString
        }
        else
        {
            null
        }
    }


     fun customToast(context: Context, message: String)
    {
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }

     fun clearSharedPreferences(context: Context)
    {
        val sharedPreferences = context.getSharedPreferences(userDetailsFile, Context.MODE_PRIVATE)
        val sharedPreferences2 = context.getSharedPreferences(profileImageUrl, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val editor2 = sharedPreferences2.edit()
        editor2.clear()
        editor2.apply()
        editor.clear()
        editor.apply()
    }





}

