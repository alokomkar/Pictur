package com.alokomkar.pictur.cache

import android.graphics.Bitmap
import com.alokomkar.pictur.cache.disk.DiskCache
import com.alokomkar.pictur.cache.memory.MemoryCache
import io.reactivex.disposables.CompositeDisposable
import java.io.File

class CacheRepository(
    cacheDir: File,
    maxSize: Int,
    disposable: CompositeDisposable
) : ImageCache {

    private val maxCacheSize : Long = 10 * 1024 * 1024
    private val maxCacheObjects = 10
    private val diskCache : ImageCache = DiskCache.getInstance(cacheDir, maxCacheSize, maxCacheObjects, disposable)
    private val memoryCache : ImageCache = MemoryCache(maxSize)

    override fun put(url: String, bitmap: Bitmap) {
        memoryCache.put(url, bitmap)
        diskCache.put(url, bitmap)
    }

    override fun get(url: String): Bitmap?
            = memoryCache.get(url) ?: diskCache.get(url)

    override fun clear() {
        memoryCache.clear()
        diskCache.clear()
    }

}