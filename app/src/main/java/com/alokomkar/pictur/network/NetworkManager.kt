package com.alokomkar.pictur.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.reactivex.Flowable
import java.net.HttpURLConnection
import java.net.URL

class NetworkManager private constructor() {

    fun loadImage( imageUrl : String) : Flowable<Bitmap>
        = Flowable.fromCallable { downloadImage(imageUrl) }

    private fun downloadImage(imageUrl: String): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val urlForImage = URL(imageUrl)
            val conn: HttpURLConnection = urlForImage.openConnection() as HttpURLConnection
            bitmap = BitmapFactory.decodeStream(conn.inputStream)
            conn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    companion object {
        private val instance : NetworkManager? = null
        @Synchronized
        fun getInstance() : NetworkManager
                =  instance?.let { instance } ?: kotlin.run {
            NetworkManager()
        }
    }


}