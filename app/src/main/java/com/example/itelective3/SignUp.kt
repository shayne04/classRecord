package com.example.itelective3

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.itelective3.databinding.ActivitySignUpBinding
import com.example.itelective3.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.FirebaseDatabase

class SignUp : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var database: DatabaseReference
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference

        progressDialog = ProgressDialog(this).apply {
            setMessage("Signing up please wait...")
            setCancelable(false)
        }

        binding.loginLink.setOnClickListener {
            btnLoginOnClickListener()
        }


        binding.signUpBtn.setOnClickListener {
            val fullName = binding.fullNameInput.text.toString().trim()
            val email = binding.enterEmailInput.text.toString().trim()
            val password = binding.enterPasswordInput.text.toString().trim()


            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            } else {

                signUpUser(fullName, email, password)
            }
        }
    }

        private fun signUpUser(fullName: String, email: String, password: String) {
            progressDialog.show()
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressDialog.dismiss()
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: ""
                        val user = User(uid, fullName, email)

                        database.child("user").child(uid).setValue(user)
                            .addOnSuccessListener {
                                Toast.makeText(this,"Sign-up successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, TeacherMain::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to save data: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        showErrorDialog(task.exception?.message ?: "Sign up failed")
                    }
                }
        }

            private fun showErrorDialog(message: String) {
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