package com.example.friendsflame

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.example.friendsflame.utils.SharedPreferncesUtils.customToast
import com.example.friendsflame.utils.SharedPreferncesUtils.saveUserDetails
import com.example.friendsflame.databinding.ActivityMainBinding
import com.example.friendsflame.models.UserViewModel
import com.example.friendsflame.utils.LoadingUtils
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    private  var viewModel = UserViewModel()

    private lateinit var googleSignIn: GoogleSignInClient

    private lateinit var callbackManager: CallbackManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginClick.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java ))
            finish()
        }

        callbackManager = CallbackManager.Factory.create()

//        binding.fbLoginButton.setReadPermissions("email", "public_profile")
//        binding.fbLoginButton.registerCallback(
//            callbackManager,
//            object : FacebookCallback<LoginResult> {
//                override fun onSuccess(loginResult: LoginResult) {
//                    LoadingUtils.showDialog(this@MainActivity,false)
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
                LoadingUtils.showDialog(this@MainActivity,false)
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

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail().build()

            googleSignIn = GoogleSignIn.getClient(applicationContext,gso)


            binding.googlesign.setOnClickListener {
                googleSignIn()
            }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }


    fun googleSignIn(){
        val signInIntent = googleSignIn.signInIntent
        Log.d("JARVIS", "signinintent $signInIntent")
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if(result.resultCode== Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            Log.d("JARVIS", "sign in worked, ${task.result}")
            handleSignInResult(task)
    }
        else{
            Log.d("JARVIS", "result unssuccess, ${result.resultCode}")
        }
    }


    fun handleSignInResult(completedTask: Task<GoogleSignInAccount>){
        try{
            val account = completedTask.getResult(ApiException::class.java)
            LoadingUtils.showDialog(this@MainActivity,false)
            CoroutineScope(Dispatchers.IO).launch {
                viewModel. firebaseAuthWithGoogle(account)
                runOnUiThread {
                    getStatus()
                }
            }
        }
        catch (exception: ApiException){
            Log.d("exception", exception.toString())
        }

    }



    fun signUpClick(view: View) {

        val username = binding.eTUsername.text.toString()
        val password = binding.eTPassword.text.toString()
        val name = binding.name.text.toString()
        val email = binding.email.text.toString()

        if(username.isEmpty())
        {
            customToast(this, "Please Enter Username!" )
        }
        else if(password.isEmpty())
        {
            customToast(this, "Please Enter Password!" )
        }
        else if(name.isEmpty())
        {
            customToast(this, "Please Enter Name!" )
        }
        else if(email.isEmpty())
        {
            customToast(this, "Please Enter Email!" )
        }
        else{
            LoadingUtils.showDialog(this@MainActivity,false)
            CoroutineScope(Dispatchers.IO).launch {
            viewModel.registerUser(username, email, password, name)
                runOnUiThread {
                    getStatus()
                }
            }
        }
    }

    fun getStatus()
    {  Handler(mainLooper).postDelayed({
        viewModel.status.observe(this){ it ->
            if(it.first)
            {
                customToast(this,"Registration Successful")
                viewModel.loggedInUserDetails.observe(this){details->
                    customToast(this,"Login Successful")
                    if (details != null) {
                        saveUserDetails(this,LoggedInUserDetails(details.username,details.name,details.email,details.userId,details.profileUrl,true))
                        LoadingUtils.hideDialog()
                        startActivity(Intent(this, FragmentsRootActivity::class.java))
                        finish()
                    }
                }
            }
            else
            {
                if(it.second.toString() == "email already exists"){
                    binding.emailTextInput.setError(it.second)
                }
                else
                {
                    binding.etUsernameTextInput.setError("Username Already Exists")
                }
            }
            LoadingUtils.hideDialog()
        }
    },3000)
    }





}