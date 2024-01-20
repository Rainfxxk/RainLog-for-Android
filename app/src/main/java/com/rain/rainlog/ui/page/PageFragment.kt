package com.rain.rainlog.ui.page

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.rain.rainlog.R
import com.rain.rainlog.databinding.FragmentPageBinding
import com.rain.rainlog.http.HttpConfig
import com.rain.rainlog.ui.main.UserViewModel
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PageFragment : Fragment() {

    private var _binding: FragmentPageBinding? = null
    private val userViewModel : UserViewModel by activityViewModels()
    private val pageViewModel : PageViewModel by activityViewModels()
    lateinit var avatar: CircleImageView
    lateinit var userName: TextView
    lateinit var fanNum: TextView
    lateinit var followNum: TextView
    lateinit var edit: TextView
    lateinit var follow: TextView
    lateinit var personalitySignature: TextView
    lateinit var tabLayout: TabLayout
    lateinit var viewPage: ViewPager2
    lateinit var logout: FloatingActionButton
    var userId: Int = 0


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        userId = arguments?.getInt("userId")!!

        if (userId == null) {
            activity?.runOnUiThread {
                Toast.makeText(activity, "No arguments", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

        _binding = FragmentPageBinding.inflate(inflater, container, false)
        avatar = binding.avatar
        userName = binding.userName
        fanNum = binding.fanNum
        followNum = binding.followNum
        edit = binding.edit
        follow = binding.follow
        personalitySignature = binding.personalitySignature
        tabLayout = binding.tabLayout
        viewPage = binding.viewPage
        logout = binding.logout

        pageViewModel.getUserInfo(userId!!)

        pageViewModel.user.observe(viewLifecycleOwner,  Observer {
            if (it != null) {
                Picasso.get().load(HttpConfig.BASEURL + it.avatarPath).into(avatar)
                userName.setText(it.userName)
                fanNum.setText("粉丝: " + it.fanNum.toString())
                followNum.setText("关注: " + it.followNum.toString())
                personalitySignature.setText(it.personalitySignature)

                if (userViewModel.user.value == null) {
                    edit.setVisibility(View.GONE)
                }
                else {
                    if (userViewModel.user.value?.userId == userId) {
                        follow.setVisibility(View.GONE)
                        edit.setOnClickListener {
                            findNavController().navigate(R.id.profile, bundleOf("user" to userViewModel.user.value))
                        }

                        logout.visibility = View.VISIBLE
                        logout.setOnClickListener {
                            pageViewModel.logout()
                        }
                        pageViewModel.logoutResult.observe(viewLifecycleOwner, Observer {
                            if (it) {
                                userViewModel.loginState.postValue(false)
                                userViewModel.user.postValue(null)
                                activity?.runOnUiThread {
                                    Toast.makeText(activity, "已退出登录", Toast.LENGTH_SHORT).show()
                                    val navController= findNavController()
                                    val startDestination = navController.graph.getStartDestination()
                                    val navOptions = NavOptions.Builder()
                                        .setPopUpTo(startDestination, true)
                                        .build()
                                    navController.navigate(startDestination, null, navOptions)
                                }
                                pageViewModel.setLogoutResult(false)
                            }
                        })
                    }
                    else {
                        edit.setVisibility(View.GONE)
                        if (it.isFollow == true) {
                            follow.setText("取消关注")
                        }
                    }
                }
            }
        })

        pageViewModel.isFollow.observe(viewLifecycleOwner, Observer {
            if (userViewModel.loginState.value == false) {
                follow.setText("关注")
                follow.setOnClickListener {
                    activity?.runOnUiThread {
                        Toast.makeText(activity, "请先登录", Toast.LENGTH_SHORT).show()
                    }
                }

                return@Observer
            }
            if (it!= null) {
                if (it) {
                    follow.setText("取消关注")
                    follow.setOnClickListener {
                        pageViewModel.cancelFollow(userId!!)
                    }
                }
                else {
                    follow.setText("关注")
                    follow.setOnClickListener {
                        pageViewModel.follow(userId!!)
                    }
                }
            }
        })

        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (pageViewModel.user.value?.userId == null) {
            Log.d("user", "null")
        }
        val viewPageAdapter = ViewPageAdapter(this, userId)
        viewPage.adapter = viewPageAdapter
        viewPage.offscreenPageLimit = 2
        viewPage.isUserInputEnabled = false

        TabLayoutMediator(tabLayout, viewPage) { tab, position ->
            when (position) {
                0 -> tab.text = "动态"
                1 -> tab.text = "收藏"
                2 -> tab.text = "关注"
            }
        }.attach()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}