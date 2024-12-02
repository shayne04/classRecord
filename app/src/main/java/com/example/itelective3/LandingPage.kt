package com.example.itelective3

import android.content.Context
import android.os.Bundle
import android.content.Intent
import android.media.audiofx.BassBoost
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LandingPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing_page)

        // Check network availability
        if (!isNetworkAvailable(this)) {
            showNoInternetDialog()
        } else {
            proceedWithSplashScreen()
        }
    }
    private fun proceedWithSplashScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                startActivity(Intent(this, TeacherMain::class.java))
            } else {
                startActivity(Intent(this, Login::class.java))
            }
            finish()
        }, 3000) // Splash screen delay of 3 seconds
    }
    private fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                return when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
        return false
    }

    private fun showNoInternetDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle("Internet Connectivity Required")
        builder.setMessage("Please turn on your Wi-Fi or data to use this app.")
        builder.setNegativeButton("Exit") { _, _ ->
            // Exit the app
            finish()
        }
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.color.white)
        dialog.show()
    }
}