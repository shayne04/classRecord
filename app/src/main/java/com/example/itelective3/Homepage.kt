package com.example.itelective3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Homepage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        val backButton: Button = findViewById(R.id.backButton)

        backButton.setOnClickListener {

            val intent = Intent(this, LandingPage::class.java)
            startActivity(intent)
            finish()
        }

        }
    }
