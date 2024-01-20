package com.rain.rainlog.ui.publish

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.rain.rainlog.databinding.FragmentPublishBinding
import com.rain.rainlog.ui.login.afterTextChanged
import com.rain.rainlog.ui.main.UserViewModel
import com.rain.rainlog.ui.home.HomeViewModel

class PublishFragment : Fragment() {

    private var _binding: FragmentPublishBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var publishViewModel: PublishViewModel
    lateinit var userViewModel: UserViewModel
    val homeViewModel: HomeViewModel by activityViewModels()

    companion object {
        val fromAlbum = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        publishViewModel =
            ViewModelProvider(this).get(PublishViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        _binding = FragmentPublishBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val publish = binding.publish
        val edit = binding.edit
        val selector = binding.selector
        val recyclerView = binding.iamges
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        val adapter = SelectedImageItemAdapter(publishViewModel.images.value!!)
        adapter.setCloseListener(object : SelectedImageItemAdapter.OnCloseListener {
            override fun onClose(position: Int) {
                publishViewModel.removeBitMap(position)
            }
        })
        recyclerView.adapter = adapter

        Log.d("UserViewModel", userViewModel.loginState.value.toString())

        refrush()

        selector.setOnClickListener {
            if (publishViewModel.imgNum.value == 9) {
                Toast.makeText(context, "最多只能上传9张图片", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            // 指定只显示图片
            intent.type = "image/*"
            startActivityForResult(intent, fromAlbum)
            Log.d("ProfileFragment", "avatarOnClick")
        }

        publishViewModel.imgNum.observe(viewLifecycleOwner, Observer {
            activity?.runOnUiThread {
                adapter.notifyDataSetChanged()
            }
        })

        publishViewModel.publishResult.observe(viewLifecycleOwner, Observer {
            val result = it?: return@Observer

            if (result) {
                Toast.makeText(context, "发布成功", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(context, "发布失败", Toast.LENGTH_SHORT).show()
            }
        })

        edit.afterTextChanged {
            publishViewModel.setText(edit.text.toString())
        }

        publish.setOnClickListener {
            val content = edit.text.toString()

            if (content.isEmpty() && content.isBlank() && publishViewModel.isImagesEmpty()) {
                Toast.makeText(context, "内容不能为空", Toast.LENGTH_SHORT).show()
            }
            else {
                publishViewModel.publish(content)
            }
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    private fun refrush() {
        binding.edit.setText(publishViewModel.text.value)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            fromAlbum -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    data.data?.let { uri ->
                        // 将选择的图片显示
                        val bitmap = getBitmapFromUri(uri)
                        bitmap?.let { publishViewModel.addBitMap(it) }
                    }
                }
            }
        }
    }

    fun getBitmapFromUri(uri: Uri) = requireActivity().contentResolver
        .openFileDescriptor(uri, "r")?.use {
            BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
        }


}