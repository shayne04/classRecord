package com.example.itelective3

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.example.itelective3.databinding.ActivityTeacherMainBinding

class TeacherMain : AppCompatActivity() {

    private lateinit var binding : ActivityTeacherMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            replaceFragment(TeacherHomeFragment())
        }

       binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.navTeacherHome -> replaceFragment(TeacherHomeFragment())
                R.id.navTeacherNotif -> replaceFragment(TeacherNotificationFragment())
                R.id.navTeacherProfile -> replaceFragment(TeacherProfileFragment())

                else -> {

                }
            }
             true
        }

    }

    private fun replaceFragment(fragment : Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }
}

