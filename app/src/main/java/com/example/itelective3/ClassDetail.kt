package com.example.itelective3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.itelective3.adapter.ViewPager
import com.example.itelective3.databinding.ActivityClassDetailBinding
import com.google.android.material.tabs.TabLayoutMediator

class ClassDetail : AppCompatActivity() {
    private lateinit var binding: ActivityClassDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
        val viewPagerAdapter = ViewPager(this)
        binding.viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Take Attendance"
                1 -> "Attendance Report"
                else -> null
            }
        }.attach()
    }
}


