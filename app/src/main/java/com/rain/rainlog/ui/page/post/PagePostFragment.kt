package com.rain.rainlog.ui.page.post

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
import com.rain.rainlog.databinding.FragmentPagePostBinding
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

class PagePostFragment(val userId: Int) : Fragment(){
    private lateinit var _binding: FragmentPagePostBinding
    val binding get() = _binding

    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var pagePostViewModel: PagePostViewModel

    lateinit var recyclerView: RecyclerView
    lateinit var noPost : TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPagePostBinding.inflate(inflater, container, false)

        pagePostViewModel = ViewModelProvider(this).get(PagePostViewModel::class.java)

        recyclerView = binding.recyclerView
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        val adapter = pagePostViewModel.postList.value?.let { ContentAdapter(it, userViewModel, ::callBack, userId == userViewModel.user.value?.userId) }
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
        adapter?.setDeleteListener(object : ContentAdapter.DeleteListener {
            override fun delete(postId: Int) {
                val formBody = FormBody.Builder()
                    .add("postId", postId.toString())
                    .build()

                HttpClient.send("/post/deletePost", formBody, object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("ContentAdapter", "onFailure: ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        activity?.runOnUiThread {
                            pagePostViewModel.removePost(postId)
                            adapter.notifyDataSetChanged()
                        }
                    }
                })
            }
        })
        recyclerView.adapter = adapter

        noPost = binding.noPost

        if (pagePostViewModel.isPostListEmpty()) {
            val formBody = FormBody.Builder()
                .add("userId", userId.toString())
                .build()

            HttpClient.send("/post/getUserPost", formBody, object : Callback {
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
                            noPost.visibility = View.VISIBLE
                        }
                    }
                    else {
                        for (post in postList) {
                            pagePostViewModel.addPost(post)
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
            this.findNavController().navigate(type, bundle)
            return@runOnUiThread
        }
    }
}