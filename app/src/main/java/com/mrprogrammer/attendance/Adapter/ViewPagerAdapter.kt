package com.mrprogrammer.attendance.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


class ViewPagerAdapter(fm: FragmentManager, private val fragments: Array<Fragment>) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        // Set tab titles if needed
        return when (position) {
            0 -> "Dashboard"
            1 -> "Attendance "
            2 -> "Leave Apply"
            else -> ""
        }
    }
}
