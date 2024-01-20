package com.rain.rainlog.ui.page.bookmark

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rain.rainlog.data.model.Post
import com.rain.rainlog.databinding.FragmentPageBookmarkBinding
import com.rain.rainlog.http.HttpClient
import com.rain.rainlog.ui.ImageDialog
import com.rain.rainlog.ui.main.UserViewModel
import com.rain.rainlog.ui.content.ContentAdapter
import com.rain.rainlog.ui.content.ImageAdapter
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class PageBookmarkFragment(val userId: Int) : Fragment() {
    private lateinit var _binding: FragmentPageBookmarkBinding
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var pageBookmarkViewModel: PageBookmarkViewModel
    lateinit var recyclerView: RecyclerView
    lateinit var noBookmark : TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPageBookmarkBinding.inflate(inflater, container, false)
        pageBookmarkViewModel = ViewModelProvider(this).get(PageBookmarkViewModel::class.java)

        recyclerView = binding.recyclerView
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        val adapter = pageBookmarkViewModel.bookmarkList.value?.let { ContentAdapter(it, userViewModel, ::callBack) }
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

        noBookmark = binding.noBookmark

        if (pageBookmarkViewModel.isBookmarkListEmpty()) {
            val formBody = FormBody.Builder()
                .add("userId", userId.toString())
                .build()

            HttpClient.send("/post/getBookmarkPost", formBody, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                    val jsonStr = response.body?.string()

                    if (jsonStr != null) {
                        Log.d("post", jsonStr)
                    }
                    val postInfos = JSONObject(jsonStr).getString("postInfos")
                    val postList = Gson().fromJson<List<Post>>(postInfos, object : TypeToken<List<Post>>() {}.type);

                    if (postList.isEmpty()) {
                        activity?.runOnUiThread {
                            noBookmark.visibility = View.VISIBLE
                        }
                    }
                    else {
                        for (post in postList) {
                            pageBookmarkViewModel.addPost(post)
                        }
                    }

                    activity?.runOnUiThread {
                        adapter?.notifyDataSetChanged()
                    }
                }
            })
        }

        return binding.root
    }

    fun callBack(type: Int, bundle: Bundle?) {
        activity?.runOnUiThread {
            findNavController().navigate(type, bundle)
            return@runOnUiThread
        }
    }
}