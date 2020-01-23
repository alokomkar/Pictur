package com.alokomkar.pictur.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import java.net.HttpURLConnection
import java.net.URL

class NetworkManager private constructor() {

    fun loadImage( imageUrl : String, errorBitmap: Bitmap ) : Flowable<Bitmap>
        = Observable.fromCallable { downloadImage(imageUrl, errorBitmap) }.toFlowable(BackpressureStrategy.LATEST)

    private fun downloadImage(imageUrl: String, errorBitmap: Bitmap): Bitmap {
        try {
            val urlForImage = URL(imageUrl)
            val conn: HttpURLConnection = urlForImage.openConnection() as HttpURLConnection
            val bitmap = BitmapFactory.decodeStream(conn.inputStream)
            conn.disconnect()
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return errorBitmap
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