package io.dailyunix

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.lang.IllegalArgumentException

class ContentFragmentStateAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> {
                TldrFragment()
            }
            1 -> {
                ManFragment()
            }
            else -> {
                throw IllegalArgumentException("Only two positions. No position: $position")
            }
        }

        // TODO (P1): Pass in the content via the Bundle:
//            fragment.arguments = Bundle().apply {
//                // Our object is just an integer :-P
//                putInt(ARG_OBJECT, position + 1)
//            }
//            return fragment
    }

}