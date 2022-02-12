package com.example.finalprojectsocket.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.finalprojectsocket.fragment.Tab1Fragment
import com.example.finalprojectsocket.fragment.Tab2Fragment

class TabPageAdapter(fm:FragmentManager,private var tabCount:Int):FragmentPagerAdapter(fm){
    override fun getItem(position: Int): Fragment {
        when(position){
            0 -> return Tab1Fragment()
            1 -> return Tab2Fragment()
            else ->return Tab1Fragment()


        }
    }

    override fun getCount(): Int {
        return tabCount
    }

}