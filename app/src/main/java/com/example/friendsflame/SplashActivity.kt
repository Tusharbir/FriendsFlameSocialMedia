package com.example.friendsflame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.example.friendsflame.utils.SharedPreferncesUtils.getSharedData

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(mainLooper).postDelayed({
            val userDetails = getSharedData(this)
            Log.d("user data", userDetails.toString())
            if(userDetails != null){
                if(userDetails.login)
                {
                    startActivity(Intent(this, FragmentsRootActivity::class.java))
                    finish()
                }
            }
            else{
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        },
            2000)

    }
}