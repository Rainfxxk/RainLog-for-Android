package com.rain.rainlog.ui.content.post
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rain.rainlog.R
import com.rain.rainlog.data.model.Comment
import com.rain.rainlog.data.model.Post
import com.rain.rainlog.data.model.User
import com.rain.rainlog.databinding.FragmentPostBinding
import com.rain.rainlog.http.HttpClient
import com.rain.rainlog.http.HttpConfig
import com.rain.rainlog.ui.ImageDialog
import com.rain.rainlog.ui.main.UserViewModel
import com.rain.rainlog.ui.comment.CommentAdapter
import com.rain.rainlog.ui.comment.CommentDialog
import com.rain.rainlog.ui.content.ImageAdapter
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class PostFragment : Fragment(){
    private var _binding: FragmentPostBinding? = null
    private val binding get() = _binding!!

    val userViewModel: UserViewModel by activityViewModels()
    lateinit var postViewModel: PostViewModel

    lateinit var avatar: CircleImageView
    lateinit var userName: TextView
    lateinit var publishTime: TextView
    lateinit var content: TextView
    lateinit var images: RecyclerView
    lateinit var comment: AppCompatImageButton
    lateinit var commentNum: TextView
    lateinit var bookmark: AppCompatImageButton
    lateinit var bookmarkNum: TextView
    lateinit var like: AppCompatImageButton
    lateinit var likeNum: TextView
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: CommentAdapter

    lateinit var post: Post

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostBinding.inflate(inflater, container, false)

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
        recyclerView = binding.recyclerView

        postViewModel = ViewModelProvider(this).get(PostViewModel::class.java)

        fun delete(comment: Comment, position: Int) {
            val formBody = FormBody.Builder()
                .add("commentId", comment.commentId.toString())
                .add("postId", comment.topicId.toString())
                .build()

            HttpClient.send("/comment/deletePostComment", formBody, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                    postViewModel.removeComment(position)
                    activity?.runOnUiThread {
                        adapter?.notifyDataSetChanged()
                    }
                }
            })
        }

        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        adapter = postViewModel.commentList.value?.let { CommentAdapter(it, userViewModel, ::delete) }!!
        recyclerView.adapter = adapter

        postViewModel.post.observe(viewLifecycleOwner, Observer {
            if (postViewModel.ifCommentListEmpty()) {
                val formBody = FormBody.Builder()
                    .add("postId", it.postId.toString())
                    .build()

                HttpClient.send("/comment/getPostComment", formBody, object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val json = response.body?.string()
                        val jsonObject = JSONObject(json)

                        val commentList = Gson().fromJson<List<Comment>>(jsonObject.getString("comments"), object : TypeToken<List<Comment>>() {}.type)
                        post.commentNum = commentList.size

                        for (comment in commentList) {
                            postViewModel.addComment(comment)
                        }

                        activity?.runOnUiThread {
                            commentNum.text = getNumStr(post.commentNum)
                            adapter?.notifyDataSetChanged()
                        }
                    }
                })
            }
        })

        post = arguments?.getSerializable("post", Post::class.java) as Post
        postViewModel.setPost(post)

        if (post!= null) {
            Picasso.get().load(HttpConfig.BASEURL + post.user.avatarPath).into(avatar)
            avatar.setOnClickListener {
                val bundle = bundleOf("userId" to post.userId)
                findNavController().navigate(R.id.navigation_notifications, bundle)
            }

            userName.setText(post.user.userName)
            publishTime.setText(post.publishTime)
            content.setText(post.content)
            commentNum.setText(getNumStr(post.commentNum))
            bookmarkNum.setText(getNumStr(post.bookmarkNum))
            likeNum.setText(getNumStr(post.likeNum))

            post.imagePath?.let {
                val imageList = it.split(";")
                images.layoutManager = GridLayoutManager(context, 3)
                val adapter = ImageAdapter(imageList)
                adapter.setImageClickListener(object : ImageAdapter.ImageClickListener {
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
                adapter.setImagePath(post.userId.toString() + "-" + post.postId + "-")
                images.adapter = adapter
            }

            userViewModel.loginState.observe(viewLifecycleOwner, Observer {
                if (it) {
                    setCommentListener()
                    setBookmarkListener(post.isBookmark)
                    setLikeListener(post.isLike)
                }
                else {
                    Log.d("ContentAdapter", "not login")
                    noLogin()
                }
            })
        }

        return binding.root
    }

    private fun callBack(type: Int, bundle: Bundle?) {
        findNavController().navigate(type, bundle)
    }

    private fun goPersonalPage(userId: String) {
        val bundle = bundleOf("userId" to userId)
        callBack(R.id.navigation_notifications, bundle)
    }

    private fun setCommentListener() {
        comment.setOnClickListener {
            showCommentDialog()
        }
        commentNum.setOnClickListener {
            showCommentDialog()
        }
    }

    fun showCommentDialog() {
        val fragmentManager = activity?.supportFragmentManager
        val newFragment = CommentDialog(object : CommentDialog.CommentDialogListener {
            override fun onComment(comment: String) {
                val formBody = FormBody.Builder()
                    .add("commentContent", comment)
                    .add("postId", post.postId.toString())
                    .add("authorId", post.userId.toString())
                    .build()

                HttpClient.send("/comment/commentPost", formBody, object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val json = response.body?.string()
                        val jsonObject = JSONObject(json)
                        val comment = Gson().fromJson<Comment>(jsonObject.getString("commentInfo"), Comment::class.java)
                        val user = Gson().fromJson<User>(jsonObject.getString("userInfo"), User::class.java)
                        comment.user = user
                        postViewModel.addComment(comment)
                        post.commentNum++
                        activity?.runOnUiThread {
                            commentNum.text = getNumStr(post.commentNum)
                            adapter.notifyDataSetChanged()
                        }
                    }
                })
            }

        })
        // The device is smaller, so show the fragment fullscreen
        val transaction = fragmentManager?.beginTransaction()
        // For a little polish, specify a transition animation
        transaction?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity
        transaction
                ?.add(android.R.id.content, newFragment)
                ?.addToBackStack(null)
                ?.commit()
    }

    fun changeButtonColor(appCompatImageButton: AppCompatImageButton, textView: TextView, color: Int) {
        when (color) {
            R.color.blue -> Log.d("ContentAdapter", "blue")
            R.color.deep_grey -> Log.d("ContentAdapter", "deep_grey")
        }

        val drawable = appCompatImageButton.drawable
        drawable.setTint(requireContext().resources.getColor(color))
        textView.setTextColor(requireContext().resources.getColor(color))
    }

    fun noLogin() {
        comment.setOnClickListener {
            toLoginFragment()
        }
        commentNum.setOnClickListener {
            toLoginFragment()
        }
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
        callBack(R.id.post, null)
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
        post.isLike = true
        like.apply {
            setColorFilter(R.color.blue)
            setOnClickListener {
                cancelLike()
            }
        }

        likeNum.apply {
            setText(getNumStr(post.likeNum))
            setTextColor(resources.getColor(R.color.blue))
            setOnClickListener {
                cancelLike()
            }
        }


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
        post.isLike = false

        like.apply {
            setColorFilter(R.color.grey)
            setOnClickListener {
                like()
            }
        }

        likeNum.apply {
            setText(getNumStr(post.likeNum))
            setTextColor(resources.getColor(R.color.grey))
            setOnClickListener {
                like()
            }
        }

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
