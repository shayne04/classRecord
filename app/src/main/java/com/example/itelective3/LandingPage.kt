package com.example.itelective3

import android.os.Bundle
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class LandingPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing_page)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@LandingPage, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)

    }
}