package com.example.itelective3

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.itelective3.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ProfileFragment : Fragment() {

    // Binding variable para sa FragmentProfileBinding, naggamit ug nullable type
    //It's initialized to null because it will be set up later in the onCreateView method.
    private var _binding: FragmentProfileBinding? = null
    // Ang binding variable nga nag-represent sa non-nullable version sa _binding
    private val binding get() = _binding!!

    // Firebase Authentication instance
    private lateinit var auth: FirebaseAuth
    // Database reference para sa Firebase Realtime Database
    private lateinit var databaseReference: DatabaseReference


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // I-inflate ang layout sa Fragment gamit ang FragmentProfileBinding
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Kuhaon ang Firebase Authentication instance
        auth = FirebaseAuth.getInstance()
        // Kuhaon ang user ID sa current user
        val userId = auth.currentUser?.uid
        // I-set ang database reference sa "Users" nga node sa Firebase, gamit ang user ID
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)

        // Load user data
        loadUserInfo()

        // Set logout button
        binding.logoutButton.setOnClickListener {
            auth.signOut()
            // I-navigate ang user pabalik sa LandingPage
            val intent = Intent(activity, LandingPage::class.java)
            // I-set ang flags aron makasiguro nga wala nay lain nga activity sa stack
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        // I-return ang root view sa binding
        return binding.root
    }

    private fun loadUserInfo() {
        // Kuhaon ang data gikan sa database gamit ang database reference
        databaseReference.get().addOnSuccessListener { snapshot ->
            // Kuhaon ang full name ug email gikan sa snapshot
            val fullName = snapshot.child("fullName").value.toString()
            val email = snapshot.child("email").value.toString()
            // I-set ang profile name ug email sa binding
            binding.profileName.text = fullName
            binding.profileEmail.text = email
        }.addOnFailureListener {
            // I-display ang error message kung dili makuha ang user info
            Toast.makeText(activity, "Failed to load user info", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // I-nullify ang binding object aron malikayan ang memory leaks
        _binding = null
    }

}
