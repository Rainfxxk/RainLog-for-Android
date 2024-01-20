package com.rain.rainlog.ui.home

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rain.rainlog.R
import com.rain.rainlog.data.model.Post
import com.rain.rainlog.databinding.FragmentHomeBinding
import com.rain.rainlog.http.HttpClient
import com.rain.rainlog.ui.ImageDialog
import com.rain.rainlog.ui.main.UserViewModel
import com.rain.rainlog.ui.content.ContentAdapter
import com.rain.rainlog.ui.content.ImageAdapter
import com.rain.rainlog.ui.title.TitleView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    val userViewModel: UserViewModel by activityViewModels()
    lateinit var homeViewModel: HomeViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var title: TitleView
    lateinit var recyclerView: RecyclerView
    lateinit var refresh: SwipeRefreshLayout

    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        title = binding.title
        recyclerView = binding.recyclerView
        refresh = binding.refresh

        userViewModel.loginState.observe(viewLifecycleOwner, Observer {
            if (it) {
                userViewModel.user.value?.avatarPath?.let {
                    activity?.runOnUiThread {
                        title.setAvatarSrc(it)
                    }
                }

                title.setAvatarClickListener(View.OnClickListener {
                    navToPage()
                })

                title.setPublishClickListener(View.OnClickListener {
                    navToPublish()
                })
            }
            else {
                title.setAvatarSrc(R.drawable.login)

                title.setAvatarClickListener(View.OnClickListener {
                    navToLogin()
                })

                title.setPublishClickListener(View.OnClickListener {
                    navToLogin()
                })
            }
        })

        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        val adapter = homeViewModel.contentList.value?.let { ContentAdapter(it, userViewModel, ::callBack) }
        adapter?.setImageClickListener(object : ImageAdapter.ImageClickListener {
            override fun imageClick(name: String, bitmap: Bitmap) {
                val fragmentManager = activity?.supportFragmentManager
                val newFragment = ImageDialog(name, bitmap)
                val transaction = fragmentManager?.beginTransaction()
                transaction?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                transaction
                    ?.add(android.R.id.content, newFragment)
                    ?.addToBackStack(null)
                    ?.commit()

            }
        })
        recyclerView.adapter = adapter

        homeViewModel.pageNum.observe(viewLifecycleOwner, Observer {
            if (it == 0 && refresh.isRefreshing) {
                homeViewModel.clearContentList()
                Log.d("Home", "clear")
            }

            if (homeViewModel.nextPage.value == true) {
                val formBody = FormBody.Builder()
                    .add("pageNum", it.toString())
                    .build()

                HttpClient.send("/post/getPostInfo", formBody, object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val json = response.body?.string()
                        if (json != null) {
                            Log.d("Home", json)

                            val jsonObject = JSONObject(json)
                            val postInfos = jsonObject.getString("postInfos")
                            Log.d("Home", postInfos)
                            val postList = Gson().fromJson<List<Post>>(postInfos, object : TypeToken<List<Post>>() {}.type);

                            if (postList.size < 10) {
                                homeViewModel.setNextPage(false)
                            }

                            for (i in 0 until postList.size) {
                                homeViewModel.contentList.value?.add(postList[i])
                            }
                            activity?.runOnUiThread {
                                if (refresh.isRefreshing) {
                                    refresh.isRefreshing = false
                                }
                                adapter?.notifyDataSetChanged()
                            }
                        }
                    }
                })
            }
        })

        refresh.setColorSchemeColors(requireContext().getColor(R.color.blue))
        refresh.setOnRefreshListener {
            Handler().postDelayed({
                homeViewModel.setNextPage(true)
                homeViewModel.refreshPage()
                Log.d("Home", "refresh")
            }, 1000)
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(-2)) {
                    Log.d("Home", "incPage")
                    homeViewModel.incPage()
                }
            }
        })

        return binding.root
    }

    fun callBack(type: Int, bundle: Bundle?) {
        activity?.runOnUiThread {
            findNavController().navigate(type, bundle)
            return@runOnUiThread
        }
    }

    private fun navToLogin() {
        activity?.runOnUiThread {
            findNavController().navigate(R.id.login)
        }
    }

    private fun navToPage() {
        activity?.runOnUiThread {
            findNavController().navigate(R.id.navigation_notifications, bundleOf("userId" to userViewModel.user.value?.userId))
        }
    }

    private fun navToPublish() {
        activity?.runOnUiThread {
            findNavController().navigate(R.id.navigation_dashboard)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}