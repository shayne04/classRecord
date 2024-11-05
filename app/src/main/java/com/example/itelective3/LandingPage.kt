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

        // Use ug handler para naay 3 seconds na delay before mag transition sa Login
        Handler(Looper.getMainLooper()).postDelayed({
            //intent to start sa login
            val intent = Intent(this@LandingPage, Login::class.java)
            startActivity(intent)
            finish()
        }, 3000)

    }
}