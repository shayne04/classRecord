package com.example.itelective3

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.itelective3.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.FirebaseDatabase

class SignUp : AppCompatActivity() {

    // FirebaseAuth object para sa authentication
    private lateinit var auth: FirebaseAuth
    // Binding object para sa layout sa SignUp activity
    private lateinit var binding: ActivitySignUpBinding
    // ProgressDialog para sa loading indication sa sign-up process
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Nag-inflate sa layout sa SignUp activity
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        // Pag-setup sa ProgressDialog
        progressDialog = ProgressDialog(this).apply {
            setMessage("Signing up...")
            setCancelable(false) // Dili ma-cancel ang dialog
        }

        // Listener para sa "Login" link
        binding.loginLink.setOnClickListener {
            btnLoginOnClickListener()
        }


        // Listener para sa "Sign Up" button
        binding.signUpBtn.setOnClickListener {
            val fullName = binding.fullNameInput.text.toString().trim()
            val email = binding.enterEmailInput.text.toString().trim()
            val password = binding.enterPasswordInput.text.toString().trim()

            // I-check kung naa bay empty fields
            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Tawag sa signUpUser function kung complete ang info
                signUpUser(fullName, email, password)
            }
        }
    }

        private fun signUpUser(fullName: String, email: String, password: String) {
            progressDialog.show()
            // Pag-create sa user using ang email ug password
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressDialog.dismiss()
                    if (task.isSuccessful) {
                        // Pagkuha sa user ID kung successful ang sign-up
                        val userId = auth.currentUser?.uid ?: ""
                        // Pag-save sa user sa database
                        saveUserToDatabase(userId, fullName, email)
                    } else {
                        showErrorDialog(task.exception?.message ?: "Sign up failed")
                    }
                }
        }

        private fun saveUserToDatabase(userId: String, fullName: String, email: String) {
            // Pagkuha sa reference sa "Users" node sa Firebase Realtime Database gamit ang user ID
            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
            // Pag-setup sa user information sa porma sa map
            val userInfo = mapOf(
                "fullName" to fullName,
                "email" to email
            )
            // Pag-save sa user info sa database
            userRef.setValue(userInfo)
                .addOnCompleteListener { task ->
                    // I-check kung successful ang pag-save sa data
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show()
                        // Navigate to LoginActivity after successful sign-up
                        val intent = Intent(this, Login::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        showErrorDialog(task.exception?.message ?: "Error saving user info")
                    }
                }
        }
            private fun showErrorDialog(message: String) {
               // Pag-create ug AlertDialog para sa error messages
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show()
            }

        private fun btnLoginOnClickListener() {
            startActivity(Intent(this, Login::class.java))
        }

}