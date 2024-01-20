package com.rain.rainlog.ui.comment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rain.rainlog.R
import com.rain.rainlog.data.model.Comment
import com.rain.rainlog.databinding.CommentItemBinding
import com.rain.rainlog.http.HttpConfig
import com.rain.rainlog.ui.main.UserViewModel
import com.squareup.picasso.Picasso
import kotlin.reflect.KFunction2

class CommentAdapter(var commentList: MutableList<Comment>, val userViewModel: UserViewModel, val deleteFun: KFunction2<Comment, Int, Unit>) : RecyclerView.Adapter<CommentAdapter.ViewHolder>(){

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val _binding = CommentItemBinding.bind(itemView)
        val binding get() = _binding

        val avatar = binding.avatar
        val userName = binding.userName
        val commentTime = binding.commentTime
        val commentContent = binding.commentContent
        val delete = binding.delete

        fun setInfo(comment: Comment, position: Int) {

            Picasso.get().load(HttpConfig.BASEURL + comment.user.avatarPath).into(avatar)

            userName.text = comment.user.userName

            commentTime.text = "评论时间:" + comment.commentTime

            commentContent.text = comment.commentContent

            userViewModel.loginState.observeForever {
                if (it) {
                    if (comment.userId == userViewModel.user.value?.userId || comment.authorId == userViewModel.user.value?.userId) {
                        delete.visibility = View.VISIBLE

                        delete.setOnClickListener {
                            deleteFun(comment, position)
                        }
                    }
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setInfo(commentList[commentList.size - position - 1], commentList.size - position - 1)
    }

}