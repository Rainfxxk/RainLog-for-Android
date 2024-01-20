package com.rain.rainlog.ui.comment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.rain.rainlog.R
import com.rain.rainlog.databinding.DialogCommentBinding

class CommentDialog(lisenter: CommentDialogListener) : DialogFragment(){

    private lateinit var _binding:DialogCommentBinding
    val binding get() = _binding

    lateinit var mark: View
    lateinit var commentInput: EditText
    lateinit var comment: Button

    private val lisenter: CommentDialogListener = lisenter

    interface CommentDialogListener {
        fun onComment(comment: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogCommentBinding.inflate(inflater, container, false)

        mark = binding.mark
        commentInput = binding.commentInput
        comment = binding.comment

        mark.setOnClickListener {
            dismissNow()
        }

        comment.setOnClickListener {
            lisenter.onComment(commentInput.text.toString())
            this.dismissNow()
        }

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;
            builder.setView(inflater.inflate(R.layout.dialog_comment, null))
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}