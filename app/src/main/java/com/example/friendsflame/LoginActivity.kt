package com.example.friendsflame

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.friendsflame.databinding.ActivityLoginBinding
import com.example.friendsflame.models.UserViewModel
import com.example.friendsflame.utils.LoadingUtils
import com.example.friendsflame.utils.SharedPreferncesUtils.customToast
import com.example.friendsflame.utils.SharedPreferncesUtils.saveUserDetails
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.CallbackManager.Factory.create
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Arrays


class LoginActivity : AppCompatActivity() {

    private val viewModel = UserViewModel()
    private lateinit var binding : ActivityLoginBinding
    private lateinit var googleSignIn: GoogleSignInClient

    private lateinit var callbackManager:CallbackManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        callbackManager = CallbackManager.Factory.create();

        callbackManager = create()

//        binding.fbLoginButton.setReadPermissions("email", "public_profile")
//        binding.fbLoginButton.registerCallback(
//            callbackManager,
//            object : FacebookCallback<LoginResult> {
//                override fun onSuccess(loginResult: LoginResult) {
//                    LoadingUtils.showDialog(this@LoginActivity,false)
//                    CoroutineScope(Dispatchers.IO).launch {
//                        viewModel.handleFacebookAccessToken(loginResult.accessToken)
//                        runOnUiThread {
//                            getStatus()
//                        }
//                    }
//                }
//
//                override fun onCancel() {
//                    Log.d("facebook:onCancel","cancelled")
//                }
//
//                override fun onError(error: FacebookException) {
//                    Log.d("facebook:onError", error.toString())
//                }
//            },
//        )

        binding.fbLoginButton.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(
                this,
                listOf("email", "public_profile")
            )
        }

        // Register callback for the Facebook login
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                LoadingUtils.showDialog(this@LoginActivity,false)
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.handleFacebookAccessToken(result.accessToken)
                    runOnUiThread {
                        getStatus()
                    }
                }
            }

            override fun onCancel() {
                // Handle Facebook login cancel
            }

            override fun onError(error: FacebookException) {
                // Handle Facebook login error
            }
        })


        binding.registerClickButton.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail().build()


        googleSignIn = GoogleSignIn.getClient(applicationContext,gso)


        binding.googleSignIN.setOnClickListener {
            googleSignIn()
        }



        binding.resetPassword.setOnClickListener {
            forgotPassword()
        }

        setContentView(binding.root)
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }



    fun googleSignIn(){
        val signInIntent = googleSignIn.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if(result.resultCode== Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }
    }


    fun handleSignInResult(completedTask: Task<GoogleSignInAccount>){
        try{
            val account = completedTask.getResult(ApiException::class.java)
            LoadingUtils.showDialog(this@LoginActivity,false)
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.firebaseAuthWithGoogle(account)
                runOnUiThread {
                    getStatus()
                }
            }
        }
        catch (exception: ApiException){
            Log.d("exception", exception.toString())
        }

    }

    fun loginFunctionality(view: View)
    {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.password.text.toString().trim()

        if(username.isEmpty())
        {
            customToast(this, "Please Enter Username!")
        }
        else if(password.isEmpty())
        {
            customToast(this, "Please Enter Password")
        }
        else{
            LoadingUtils.showDialog(this@LoginActivity,false)
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.loginUser(username, password)
                runOnUiThread {
                    getStatus()
                }
            }
        }
    }

    private fun getStatus()
    {
        Handler(mainLooper).postDelayed({
            viewModel.status.observe(this){
                if(it.first)
                {
                    viewModel.loggedInUserDetails.observe(this){ user->
                        customToast(this,"Login Successful")
                        if (user != null) {
                            saveUserDetails(this,LoggedInUserDetails(user.username,user.name,user.email,user.userId,user.profileUrl,true))
                            startActivity(Intent(this, FragmentsRootActivity::class.java))
                            finish()
                        }
                    }
                }
                else
                {
                    customToast(this,it.second.toString())
                }
                LoadingUtils.hideDialog()
            }
        },3000)
    }


    private fun forgotPassword(){
        if(binding.etUsername.text.toString().contains("@"))
        {
            viewModel.forgotPassword(binding.etUsername.text.toString())
        }
        else{
            customToast(this, "Please Enter Email Id")
        }
    }
}