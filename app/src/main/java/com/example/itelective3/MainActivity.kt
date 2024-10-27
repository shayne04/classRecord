package com.example.itelective3

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginButton: Button = findViewById(R.id.login_button)
        val googleButton: ImageView = findViewById(R.id.google_btn)
        val usernameInput: EditText = findViewById(R.id.username_input)
        val passwordInput: EditText = findViewById(R.id.password_input)
        val signUpLink: TextView = findViewById(R.id.signUp_link)

        loginButton.setOnClickListener {
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()

            if (username.isNotBlank() && password.isNotBlank()) {

                val intent = Intent(this, Homepage::class.java)
                startActivity(intent)
                finish()
            } else {

                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show()
            }
        }


        googleButton.setOnClickListener {
            Toast.makeText(this, "Google Login Clicked", Toast.LENGTH_SHORT).show()

        }


        signUpLink.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }
    }
}
