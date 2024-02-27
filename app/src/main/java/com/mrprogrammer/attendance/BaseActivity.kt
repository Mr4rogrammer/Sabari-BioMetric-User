package com.mrprogrammer.attendance

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mrprogrammer.attendance.Adapter.ViewPagerAdapter
import com.mrprogrammer.attendance.Fragment.Dashboard
import com.mrprogrammer.attendance.Fragment.Leave
import com.mrprogrammer.attendance.Fragment.MapFragment
import com.mrprogrammer.mrshop.ObjectHolder.ObjectHolder
import info.mrprogrammer.admin_bio.Model.AttdanceModel


class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)



        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager>(R.id.viewPager)

        val fragments: Array<Fragment> = arrayOf<Fragment>(Dashboard(), MapFragment(), Leave())
        val adapter = ViewPagerAdapter(supportFragmentManager, fragments)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }




}