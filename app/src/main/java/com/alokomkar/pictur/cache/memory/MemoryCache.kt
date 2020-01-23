package com.alokomkar.pictur.cache.memory

import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache
import com.alokomkar.pictur.cache.ImageCache
import com.alokomkar.pictur.cache.defaultCacheSize
import com.alokomkar.pictur.cache.maxMemory

@Suppress("PrivatePropertyName")
class MemoryCache(maxSize : Int ) : ImageCache {

    private val cache : LruCache<String, Bitmap>
    private val TAG = MemoryCache::class.java.simpleName

    init {
        val cacheSize : Int
        if (maxSize > maxMemory) {
            cacheSize = defaultCacheSize
            Log.d(TAG,"Provided Max Size of cache is bigger than maximum cache available on system")
        } else {
            cacheSize = maxSize
        }
        cache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, value: Bitmap): Int {
                return (value.rowBytes)*(value.height) / 1024
            }
        }
    }

    override fun put(url: String, bitmap: Bitmap) {
        cache.put(url, bitmap)
    }

    override fun get(url: String): Bitmap? {
        return cache.get(url)
    }

    override fun clear() {
        cache.evictAll()
    }

}