package com.example.itelective3

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.itelective3.databinding.ActivityLoginBinding
import com.example.itelective3.databinding.DialogForgotPasswordBinding
import com.example.itelective3.model.User
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase


class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var progressDialog: ProgressDialog
    private lateinit var dialogForgotPasswordBinding: DialogForgotPasswordBinding
    private lateinit var dialogForgotPassword: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference

        progressDialog = ProgressDialog(this).apply {
            setMessage("Loading...")
            setCancelable(false)
        }

        binding.loginButton.setOnClickListener { btnLogin() }
        binding.signUpButton.setOnClickListener { btnSignUp() }
        binding.forgotPass.setOnClickListener {forgotPassword()}

    }


    private fun btnLogin() {
        val email = binding.usernameInput.text.toString()
        val password = binding.passwordInput.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email or password is empty, please fill in both fields.", Toast.LENGTH_SHORT).show()
                return
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                return
            }

        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {  task ->
            progressDialog.dismiss()
            if (task.isSuccessful) {
                val uid = firebaseAuth.currentUser?.uid ?: ""
                database.child("user").child(uid).get()
                    .addOnSuccessListener { snapshot ->
                        val user = snapshot.getValue(User::class.java)
                        if (user != null) {
                            Toast.makeText(this, "Login successfully!", Toast.LENGTH_SHORT).show()
                            goToHome()
                        } else {
                            Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToHome() {
        val intent = Intent(this, TeacherMain::class.java)
        startActivity(intent)
        finish()
    }

    private fun forgotPassword() {
        dialogForgotPasswordBinding = DialogForgotPasswordBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogForgotPasswordBinding.root)
        builder.setCancelable(false)
        dialogForgotPassword = builder.create()

        dialogForgotPasswordBinding.btnReset.setOnClickListener {
            resetPassword(dialogForgotPasswordBinding.email)
        }
        dialogForgotPasswordBinding.btnCancel.setOnClickListener {
            dialogForgotPassword.dismiss()
        }

        dialogForgotPassword.show()
    }

    private fun resetPassword(input: TextInputEditText) {
        val email = input.text.toString()
        if (email.isEmpty()) {
           Toast.makeText(this, "Please enter an email.",Toast.LENGTH_SHORT).show()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
           Toast.makeText(this, "Please enter a valid email.", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                dialogForgotPassword.dismiss()
                AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Password Reset Email Sent")
                    .setMessage("We've sent a password reset link to your email. Please check your inbox and spam folder.")
                    .setNegativeButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }
    }

    private fun btnSignUp() {
            startActivity(Intent(this, SignUp::class.java))
    }

}









