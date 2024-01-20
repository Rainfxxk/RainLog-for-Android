package com.rain.rainlog.ui.page.follow

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rain.rainlog.data.model.User
import com.rain.rainlog.databinding.FragmentPageFollowBinding
import com.rain.rainlog.http.HttpClient
import com.rain.rainlog.ui.main.UserViewModel
import com.rain.rainlog.ui.follow.FollowAdapter
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class PageFollowFragment(val userId: Int) : Fragment(){
    private lateinit var _binding: FragmentPageFollowBinding
    val binding get() = _binding
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var pageFollowViewModel: PageFollowViewModel
    lateinit var recyclerView: RecyclerView
    lateinit var noFollow : TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPageFollowBinding.inflate(inflater, container, false)

        pageFollowViewModel = ViewModelProvider(this).get(PageFollowViewModel::class.java)

        recyclerView = binding.recyclerView
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        val adapter = pageFollowViewModel.followList.value?.let { FollowAdapter(it, userId, userViewModel, ::callBack) }
        adapter?.followClickedListener = object : FollowAdapter.FollowClickedListener {
            override fun onFollowClicked(textView: TextView, text: String) {
                activity?.runOnUiThread {
                    textView.text = text
                }
            }
        }
        recyclerView.adapter = adapter

        noFollow = binding.noFollow

        if (pageFollowViewModel.isFollowListEmpty()) {
            val formBody = FormBody.Builder()
                .add("userId", userId.toString())
                .build()

            HttpClient.send("/user/getFollowUser", formBody, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                    val jsonStr = response.body?.string()

                    if (jsonStr != null) {
                        Log.d("follow", jsonStr)
                    }

                    val followUsers = JSONObject(jsonStr).getString("followUsers")
                    val followList = Gson().fromJson<List<User>>(followUsers, object : TypeToken<List<User>>() {}.type);

                    if (followList.isEmpty()) {
                        activity?.runOnUiThread {
                            noFollow.visibility = View.VISIBLE
                        }
                    }
                    else {
                        for (user in followList) {
                            pageFollowViewModel.addUser(user)
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