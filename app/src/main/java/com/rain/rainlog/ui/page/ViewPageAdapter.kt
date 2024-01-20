package com.rain.rainlog.ui.page

import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.rain.rainlog.ui.page.bookmark.PageBookmarkFragment
import com.rain.rainlog.ui.page.follow.PageFollowFragment
import com.rain.rainlog.ui.page.post.PagePostFragment

class ViewPageAdapter(fragment: Fragment, val userId: Int) : FragmentStateAdapter(fragment) {
    companion object {

        const val PAGE_POST = 0

        const val PAGE_BOOKMARK = 1

        const val PAGE_FOLLOW = 2

    }

    private val fragments: SparseArray<Fragment> = SparseArray()

    init {
        fragments.put(PAGE_POST, PagePostFragment(userId))
        fragments.put(PAGE_BOOKMARK, PageBookmarkFragment(userId))
        fragments.put(PAGE_FOLLOW, PageFollowFragment(userId))
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        lateinit var fragment: Fragment

        when (position) {
            PAGE_POST -> {
                if (fragments.get(PAGE_POST) == null) {
                    fragments.put(PAGE_POST, PagePostFragment(userId))
                    fragments.get(PAGE_POST)!!
                } else {
                    fragment = fragments.get(PAGE_POST)!!
                }
            }

            PAGE_BOOKMARK -> {
                if (fragments.get(PAGE_BOOKMARK) == null) {
                    fragments.put(PAGE_BOOKMARK, PageBookmarkFragment(userId))
                    fragment = fragments.get(PAGE_BOOKMARK)!!
                } else {
                    fragment = fragments.get(PAGE_BOOKMARK)!!
                }
            }

            PAGE_FOLLOW -> {
                if (fragments.get(PAGE_FOLLOW) == null) {
                    fragments.put(PAGE_FOLLOW, PageFollowFragment(userId))
                    fragment = fragments.get(PAGE_FOLLOW)!!
                } else {
                    fragment = fragments.get(PAGE_FOLLOW)!!
                }
            }

            else -> {
                if (fragments.get(PAGE_POST) == null) {
                    fragments.put(PAGE_POST, PagePostFragment(userId))
                    fragments.get(PAGE_POST)!!
                } else {
                    fragment = fragments.get(PAGE_POST)!!
                }
            }
        }

        return fragment
    }

}