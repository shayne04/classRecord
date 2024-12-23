package com.example.itelective3

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
    private val pickImageRequest = 1001

    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        _binding = FragmentTeacherProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.changeProfilePictureButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, pickImageRequest)
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        progressDialog = ProgressDialog(activity).apply {
            setMessage("Logging out...")
            setCancelable(false)
        }
        loadUserInfo()

        binding.logoutButton.setOnClickListener {
            showLogoutDialog()
        }

        binding.changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    private fun showChangePasswordDialog() {
        val input = android.widget.EditText(requireContext()).apply {
            hint = "Enter new password"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setMessage("Please enter your new password:")
            .setView(input)
            .setPositiveButton("Submit") { _, _ ->
                val newPassword = input.text.toString()
                if (newPassword.isNotEmpty() && newPassword.length >= 6) {
                    changePassword(newPassword)
                } else {
                    Toast.makeText(requireContext(), "Password must be at least 6 characters",Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun changePassword(newPassword: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            currentUser.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) { Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to change password: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            Toast.makeText(requireContext(), "No user logged in", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                progressDialog.show()
                performLogout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performLogout() {
        auth.signOut()
        Handler(Looper.getMainLooper()).postDelayed({
            progressDialog.dismiss()
            val intent = Intent(activity, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }, 2000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == pickImageRequest) {
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                binding.profilePicture.setImageURI(selectedImageUri)
                binding.profilePicture.scaleType = ImageView.ScaleType.CENTER_CROP
            }
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
