package com.alokomkar.pictur

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.alokomkar.pictur.core.Pictur
import kotlinx.android.synthetic.main.activity_main.*

class DemoActivity : AppCompatActivity() {

    private val pictur : Pictur by lazy { Pictur.getInstance(this, 10 * 1024 * 1024) }
    private val imageArray = ArrayList<String>().apply {
        add("https://cdn.pixabay.com/photo/2019/11/08/11/56/cat-4611189__340.jpg")
        add("https://i.picsum.photos/id/100/367/267.jpg")
        add("https://i.picsum.photos/id/1000/367/267.jpg")
        add("https://picsum.photos/id/1001/367/267")
        add("https://picsum.photos/id/1002/367/267")
        add("https://picsum.photos/id/1003/367/267")
        add("https://picsum.photos/id/1004/367/267")
        add("https://picsum.photos/id/1005/367/267")
        add("https://picsum.photos/id/1006/367/267")
        add("https://picsum.photos/id/1008/367/267")
        add("https://picsum.photos/id/1009/367/267")
        add("https://picsum.photos/id/1010/367/267")
        add("https://picsum.photos/id/1011/367/267")
        add("https://picsum.photos/id/1012/367/267")
        add("https://picsum.photos/id/1013/367/267")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var currentIndex = 0
        loadImage(imageArray[currentIndex])
        btnNext.setOnClickListener {
            loadImage(imageArray[++currentIndex % imageArray.size])
        }

    }

    private fun loadImage(imageUrl: String) {
        pictur.loadImage(imageUrl, R.mipmap.ic_launcher_round, R.drawable.ic_error, ivCatPic) { inProgress, _ ->
            pbImageLoading.visibility = if( inProgress ) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pictur.dispose()
    }
}
