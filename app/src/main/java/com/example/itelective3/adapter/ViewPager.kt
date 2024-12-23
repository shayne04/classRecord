package com.example.itelective3.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.itelective3.AttendanceFragment
import com.example.itelective3.AttendanceReportFragment

class ViewPager(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AttendanceFragment() // First Tab
            1 -> AttendanceReportFragment() // Second Tab
            else -> AttendanceFragment()
        }
    }
}