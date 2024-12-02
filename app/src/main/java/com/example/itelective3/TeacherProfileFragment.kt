package com.example.itelective3

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.itelective3.databinding.FragmentTeacherProfileBinding
import com.example.itelective3.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class TeacherProfileFragment : Fragment() {

    private var _binding: FragmentTeacherProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var progressDialog: ProgressDialog

    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        _binding = FragmentTeacherProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        progressDialog = ProgressDialog(activity).apply {
            setMessage("Logging out...")
            setCancelable(false)
        }
        loadUserInfo()

        binding.logoutButton.setOnClickListener {
            progressDialog.show()
            auth.signOut()

            Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(activity, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
                progressDialog.dismiss()
            }, 2000)
        }

    }

    private fun loadUserInfo() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
        database.child("user").child(uid).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    binding.profileName.text = user.fullName
                    binding.profileEmail.text = user.email
                } else {
                    Toast.makeText(activity, "Failed to load user info", Toast.LENGTH_SHORT).show()
                }
            }
        .addOnFailureListener {
            Toast.makeText(activity, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
        } else {
            Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
