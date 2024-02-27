package com.example.friendsflame.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.friendsflame.LoggedInUserDetails
import com.example.friendsflame.R
import com.example.friendsflame.utils.SharedPreferncesUtils.customToast
import com.example.friendsflame.utils.SharedPreferncesUtils.getSharedData
import com.example.friendsflame.databinding.FragmentChangePasswordBinding
import com.example.friendsflame.models.UserViewModel
import com.google.firebase.auth.FirebaseAuth


class ChangePasswordFragment : Fragment() {

    private lateinit var userData:LoggedInUserDetails
    private val viewModel = UserViewModel()
    private lateinit var binding : FragmentChangePasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        userData = getSharedData(requireContext())!!

        binding = FragmentChangePasswordBinding.inflate(layoutInflater,container,false)

        binding.changePasswordButton.setOnClickListener {
            changePassword()
        }

        binding.forgotPasswordButton.setOnClickListener {
            forgotPassword()
        }

        return binding.root
    }


    fun changePassword()
    {
        val oldPassword = binding.etOldPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmNewPassword = binding.etConfirmPassword.text.toString()

        if(oldPassword.isEmpty()){
            binding.etOldPasswordLayout.setError("Please Enter Password!")
        }
        else if(newPassword.isEmpty()){
            binding.etNewPassword.setError("Please Enter Password!")
        }
        else if(confirmNewPassword.isEmpty()){
            binding.etConfirmPasswordLayout.setError("Please Enter Password!")
        }
        else{
            FirebaseAuth.getInstance().signInWithEmailAndPassword(userData!!.email, oldPassword).addOnSuccessListener {
                if(newPassword.length>=8){
                    if(newPassword==confirmNewPassword){
                        viewModel.changePassword(newPassword)
                        viewModel.status.observe(viewLifecycleOwner){
                            if(it.first){
                                customToast(requireContext(), "Password Updated")
                                binding.etOldPassword.text.clear()
                                binding.etNewPassword.text.clear()
                                binding.etConfirmPassword.text.clear()
                                parentFragmentManager.beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()
                            }
                            else{
                                customToast(requireContext(), it.second.toString())
                            }
                        }
                    }
                    else
                    {
                        binding.etNewPasswordLayout.setError("Passwords Aren't Same!")
                        binding.etConfirmPasswordLayout.setError("Passwords Aren't Same!")
                    }
                }
                else
                {
                    binding.etNewPasswordLayout.setError("Password's Length Should Be Equals or Greater Than 8")
                }
            }
                .addOnFailureListener {
                    binding.etOldPasswordLayout.setError("Incorrect Password")
                }
        }
    }

    fun forgotPassword(){
        viewModel.forgotPassword(userData!!.email)
    }

}