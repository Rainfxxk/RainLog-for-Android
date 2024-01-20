package com.rain.rainlog.ui.follow

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.rain.rainlog.R
import com.rain.rainlog.data.model.User
import com.rain.rainlog.databinding.FollowItemBinding
import com.rain.rainlog.http.HttpClient
import com.rain.rainlog.http.HttpConfig
import com.rain.rainlog.ui.main.UserViewModel
import com.squareup.picasso.Picasso
import okhttp3.Callback
import okhttp3.FormBody
import org.json.JSONObject

class FollowAdapter(val followList: List<User>, val userId: Int, userViewModel: UserViewModel, callBack: (type: Int, bundle: Bundle?) -> Unit) : Adapter<FollowAdapter.ViewHolder>() {

    val userViewModel: UserViewModel = userViewModel
    val callBack: (type: Int, bundle: Bundle?)->Unit = callBack

    interface FollowClickedListener {
        fun onFollowClicked(textView: TextView, text: String)
    }

    lateinit var followClickedListener: FollowClickedListener

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: FollowItemBinding = FollowItemBinding.bind(itemView)
        val avatar = binding.avatar
        val username = binding.username
        val follow = binding.follow

        lateinit var user : User

        fun setEvent(user : User) {
            this.user = user

            Picasso.get().load(HttpConfig.BASEURL + user.avatarPath).into(avatar)
            avatar.setOnClickListener {
                val bundle = Bundle()
                bundle.putInt("userId", user.userId)
                callBack(R.id.navigation_notifications, bundle)
            }

            username.setText(user.userName)

            userViewModel.loginState.observe(itemView.context as LifecycleOwner, Observer {
                if (it) {
                    if (userId == userViewModel.user.value?.userId) {
                        follow.setText("取消关注")
                        follow.setOnClickListener {
                            cancelFollow()
                        }
                    }
                    else {
                        if (user.isFollow == true) {
                            follow.setText("已关注")
                            Log.d("follow", "follow: " + user.isFollow.toString())
                        }
                        else {
                            Log.d("follow", "follow: " + user.isFollow.toString())
                            follow.setText("关注")
                            follow.setOnClickListener {
                                follow()
                            }
                        }
                    }
                }
                else {
                    follow.setOnClickListener {
                        Toast.makeText(itemView.context, "请先登录", Toast.LENGTH_SHORT).show()
                    }
                }
            })

        }

        fun follow() {
            val formBody = FormBody.Builder()
                .add("toId", user.userId.toString())
               .build()

            HttpClient.send("/follow/followUser", formBody, object : Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    if (response.isSuccessful) {
                        val json = response.body?.string()

                        val jsonObject = JSONObject(json)
                        if (jsonObject.getString("followResult") == "noLogin") {
                            callBack(R.id.login, null)
                        }

                        if (jsonObject.getBoolean("followResult")) {

                            if (user.userId == userViewModel.user.value?.userId) {
                                followClickedListener.onFollowClicked(follow, "取消关注")
                                follow.setOnClickListener {
                                    cancelFollow()
                                }
                            }
                            else {
                                followClickedListener.onFollowClicked(follow, "已关注")
                            }

                            follow.setOnClickListener {
                                cancelFollow()
                            }
                        }
                    }
                }
            })
        }

        fun cancelFollow() {
            val formBody = FormBody.Builder()
               .add("toId", user.userId.toString())
              .build()

            HttpClient.send("/follow/cancelFollowUser", formBody, object : Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val json = response.body?.string()

                    val jsonObject = JSONObject(json)
                    if (jsonObject.getString("followResult") == "noLogin") {
                        callBack(R.id.login, null)
                    }

                    if (jsonObject.getBoolean("followResult")) {
                        followClickedListener.onFollowClicked(follow, "关注")
                        follow.setOnClickListener {
                            follow()
                        }
                    }
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.follow_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: FollowAdapter.ViewHolder, position: Int) {
        holder.setEvent(followList[position])
    }

    override fun getItemCount(): Int {
        return followList.size
    }
}