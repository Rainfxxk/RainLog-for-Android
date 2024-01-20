package com.rain.rainlog.ui.content

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rain.rainlog.R
import com.rain.rainlog.data.model.Post
import com.rain.rainlog.databinding.ContentItemBinding
import com.rain.rainlog.http.HttpClient
import com.rain.rainlog.http.HttpConfig
import com.rain.rainlog.ui.main.UserViewModel
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Response
import java.io.IOException

class ContentAdapter(val postList: List<Post>, userViewModel: UserViewModel, callBack: (type: Int, bundle: Bundle?)->Unit, canDelete: Boolean = false) : RecyclerView.Adapter<ContentAdapter.ViewHolder>() {
    val userViewModel: UserViewModel = userViewModel
    val callBack: (type: Int, bundle: Bundle?)->Unit = callBack
    val canDelete: Boolean = canDelete

    interface DeleteListener {
        fun delete(postId: Int)
    }

    private lateinit var deleteListener: DeleteListener

    private lateinit var imageClickListener: ImageAdapter.ImageClickListener

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ContentItemBinding
        var avatar: CircleImageView
        var userName: TextView
        var publishTime: TextView
        var content: TextView
        var images: RecyclerView
        var comment: AppCompatImageButton
        var commentNum: TextView
        var bookmark: AppCompatImageButton
        var bookmarkNum: TextView
        var like: AppCompatImageButton
        var likeNum: TextView
        var delete: AppCompatImageButton

        lateinit var post: Post
        lateinit var userViewModel: UserViewModel


        init {
            binding = ContentItemBinding.bind(itemView)
            avatar = binding.avatar
            userName = binding.userName
            publishTime = binding.publishTime
            content = binding.content
            images = binding.images
            comment = binding.comment
            commentNum = binding.commentNum
            bookmark = binding.bookmark
            bookmarkNum = binding.bookmarkNum
            like = binding.like
            likeNum = binding.likeNum
            delete = binding.delete
        }

        fun setInfo(post: Post, userViewModel: UserViewModel) {

            this.post = post
            this.userViewModel = userViewModel

            Picasso.get().load(HttpConfig.BASEURL + post.user.avatarPath).into(avatar)
            avatar.setOnClickListener {
                toPersonalPage(post.userId)
            }

            userName.setText(post.user.userName)
            publishTime.setText(post.publishTime)
            content.setText(post.content)
            commentNum.setText(getNumStr(post.commentNum))
            bookmarkNum.setText(getNumStr(post.bookmarkNum))
            likeNum.setText(getNumStr(post.likeNum))

            post.imagePath?.let {
                val imageList = it.split(";")
                images.layoutManager = GridLayoutManager(itemView.context, 3)
                val adapter = ImageAdapter(imageList)
                adapter.setImageClickListener(imageClickListener)
                adapter.setImagePath(post.userId.toString() + "-" + post.postId + "-")
                images.adapter = adapter
            }

            userViewModel.loginState.observe(itemView.context as LifecycleOwner, Observer {
                if (it) {
                    setBookmarkListener(post.isBookmark)
                    setLikeListener(post.isLike)
                }
                else {
                    noLogin()
                }
            })

            setCommentListener()
            itemView.setOnClickListener {
                callBack(R.id.post, bundleOf("post" to post))
            }

            if (canDelete) {
                delete.visibility = View.VISIBLE
                delete.setOnClickListener {
                    deleteListener.delete(post.postId)
                }
            }
        }

        fun changeButtonColor(appCompatImageButton: AppCompatImageButton, textView: TextView, color: Int) {
            when (color) {
                R.color.blue -> Log.d("ContentAdapter", "blue")
                R.color.deep_grey -> Log.d("ContentAdapter", "deep_grey")
            }

            val drawable = appCompatImageButton.drawable
            drawable.setTint(itemView.context.resources.getColor(color))
            textView.setTextColor(itemView.context.resources.getColor(color))
        }

        private fun toPersonalPage(userId: Int) {
            val bundle = bundleOf("userId" to userId)
            callBack(R.id.navigation_notifications, bundle)
        }

        private fun setCommentListener() {
            comment.setOnClickListener {
                toPostFragment()
            }
            commentNum.setOnClickListener {
                toPostFragment()
            }
        }

        fun noLogin() {
            bookmark.setOnClickListener {
                toLoginFragment()
            }
            bookmarkNum.setOnClickListener {
                toLoginFragment()
            }
            like.setOnClickListener {
                toLoginFragment()
            }
            likeNum.setOnClickListener {
                toLoginFragment()
            }
        }

        @SuppressLint("ResourceType")
        fun toLoginFragment() {
            callBack(R.id.login, null)
        }

        @SuppressLint("ResourceType")
        fun toPostFragment() {
            callBack(R.id.post, bundleOf("post" to  post))
        }

        fun setCommentListener (postId: Int) {
            if (userViewModel.loginState.value == true) {
                comment.setOnClickListener {
                    toPostFragment()
                }
            }
        }

        fun setLikeListener(isLike: Boolean) {
            if (isLike) {
                changeButtonColor(like, likeNum, R.color.blue)

                like.apply {
                    setOnClickListener {
                        cancelLike()
                    }
                }

                likeNum.apply {
                    setOnClickListener {
                        cancelLike()
                    }
                }
            }
            else {
                changeButtonColor(like, likeNum, R.color.deep_grey)

                like.apply {
                    setOnClickListener {
                        like()
                    }
                }

                likeNum.apply {
                    setOnClickListener {
                        like()
                    }
                }
            }
        }

        fun like() {
            post.likeNum++
            likeNum.setText(getNumStr(post.likeNum))
            post.isLike = true

            setLikeListener(post.isLike)

            var formBody = FormBody.Builder()
                .add("postId", post.postId.toString())
                .add("authorId", post.userId.toString())
                .build()
            HttpClient.send("/like/likePost", formBody, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                }
            })
        }

        fun cancelLike() {
            post.likeNum--
            likeNum.setText(getNumStr(post.likeNum))
            post.isLike = false

            setLikeListener(post.isLike)

            var formBody = FormBody.Builder()
                .add("postId", post.postId.toString())
                .add("authorId", post.userId.toString())
                .build()
            HttpClient.send("/like/cancelLikePost", formBody, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                }
            })
        }

        fun setBookmarkListener(isBookmark: Boolean) {
            if (isBookmark) {
                changeButtonColor(bookmark, bookmarkNum, R.color.blue)

                bookmark.apply {
                    setOnClickListener {
                        cancelBookmark()
                    }
                }

                bookmarkNum.apply {
                    setOnClickListener {
                        cancelBookmark()
                    }
                }
            }
            else {
                changeButtonColor(bookmark, bookmarkNum, R.color.deep_grey)

                bookmark.apply {
                    setOnClickListener {
                        bookmark()
                    }
                }

                bookmarkNum.apply {
                    setOnClickListener {
                        bookmark()
                    }
                }
            }
        }

        fun bookmark() {
            post.bookmarkNum++
            bookmarkNum.setText(getNumStr(post.bookmarkNum))
            post.isBookmark = true

            setBookmarkListener(post.isBookmark)

            var formBody = FormBody.Builder()
                .add("postId", post.postId.toString())
                .add("authorId", post.userId.toString())
                .build()
            HttpClient.send("/bookmark/bookmarkPost", formBody, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                }
            })
        }

        fun cancelBookmark() {
            post.bookmarkNum--
            bookmarkNum.setText(getNumStr(post.bookmarkNum))
            post.isBookmark = false

            setBookmarkListener(post.isBookmark)

            var formBody = FormBody.Builder()
                .add("postId", post.postId.toString())
                .add("authorId", post.userId.toString())
                .build()
            HttpClient.send("/bookmark/cancelBookmarkPost", formBody, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                }
            })
        }

        fun getNumStr(num: Int): String {
            if (num > 10000) {
                return (num / 10000).toString() + "w"
            }
            else if (num > 1000) {
                return (num / 1000).toString() + "k"
            }

            return num.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.content_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContentAdapter.ViewHolder, position: Int) {
        holder.setInfo(postList[position], userViewModel)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    fun setDeleteListener(deleteListener: DeleteListener) {
        this.deleteListener = deleteListener
    }

    fun setImageClickListener(imageClickListener: ImageAdapter.ImageClickListener) {
        this.imageClickListener = imageClickListener
    }
}