package com.example.itelective3

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.example.itelective3.databinding.ActivityHomepageBinding

class Homepage : AppCompatActivity() {

    private lateinit var binding : ActivityHomepageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pag-ilis sa default fragment sa HomeFragment
        replaceFragment(HomeFragment())

        // Pag-setup sa listener para sa bottom navigation
       binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment()) // Kung ang Home icon ang napili, ilisi ang fragment sa HomeFragment
                R.id.nav_notification -> replaceFragment(NotificationFragment()) // Kung ang Notification icon ang napili, ilisi ang fragment sa NotificationFragment
                R.id.nav_profile -> replaceFragment(ProfileFragment()) // Kung ang Profile icon ang napili, ilisi ang fragment sa ProfileFragment

                else -> {

                }
            }
             true
        }

    }
    // Function para sa pag-ilis sa fragment
    private fun replaceFragment(fragment : Fragment) {
        val fragmentManager = supportFragmentManager   // Pagkuha sa fragment manager
        val fragmentTransaction = fragmentManager.beginTransaction() // Pagsugod sa fragment transaction
        fragmentTransaction.replace(R.id.frame_layout,fragment) //  replaces the current fragment with the new one, Ilisdi ang fragment sa frame layout
        fragmentTransaction.commit() // I-commit ang transaction
    }
}

