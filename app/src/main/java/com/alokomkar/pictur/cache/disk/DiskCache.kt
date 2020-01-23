package com.alokomkar.pictur.cache.disk

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.alokomkar.pictur.cache.ImageCache
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.*
import java.net.URLEncoder
import java.util.*

/**
 * Persists Bitmaps in files in the cache directory (See [Context.getCacheDir]).
 */
@Suppress("PrivatePropertyName")
class DiskCache private constructor(private val cacheDirectory: File,
                                    private val maxSize: Long,
                                    private val maxFilesCount: Int,
                                    disposable: CompositeDisposable) : ImageCache {

    private var currentCacheSize: Long = 0L
    private var currentCacheFilesCount: Int = 0

    private val lruEntries: LinkedHashMap<String, Long> =
        LinkedHashMap(
            maxFilesCount,
            0.75f,
            true
        )

    init {
        disposable.add(Single.fromCallable { loadLRUEntriesFromCacheDirectory() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe())
    }

    private fun loadLRUEntriesFromCacheDirectory() {
        lruEntries.clear()
        currentCacheFilesCount = cacheDirectory.listFiles()?.size ?: 0
        currentCacheSize = getFolderSize(cacheDirectory)

        val entries : MutableList<DiskEntry> = mutableListOf()
        cacheDirectory.listFiles { file, _ ->
            entries.add(DiskEntry(file.absolutePath, file.lastModified()))
        }
        //Sort by descending order of timestamp
        entries.sortDescending()
        entries.forEach {
            lruEntries[it.fileEntry] = it.fileAccessTimeStamp
        }
    }

    private fun getFolderSize(folder: File): Long {
        var length: Long = 0
        val dirFiles = folder.listFiles()
        dirFiles?.let { files ->
            val count = files.size
            for (i in 0 until count) {
                length += if (files[i].isFile) {
                    files[i].length()
                } else {
                    getFolderSize(files[i])
                }
            }
        }
        return length
    }

    override fun put(url: String, bitmap: Bitmap) {
        encodeKey(url)?.let { cacheFileName ->
            val cacheFile = File(cacheDirectory, cacheFileName)
            try {
                val fileOutputStream = FileOutputStream(cacheFile)
                saveBitmapToFile(cacheFile, bitmap, fileOutputStream)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun get(url: String): Bitmap? {
        synchronized(cacheDirectory) {
            val cacheFileName = encodeKey(url)
            val foundCacheFiles =
                cacheDirectory.listFiles { _: File?, filename: String -> filename == cacheFileName }
            return if (foundCacheFiles == null || foundCacheFiles.isEmpty()) { // No cached object found for this key
                null
            } else readBitmapFromFile(foundCacheFiles[0])
            // Read and return its contents
        }
    }

    override fun clear() {
        synchronized(cacheDirectory) {
            val cachedFiles = cacheDirectory.listFiles()
            if (cachedFiles != null) {
                for (cacheFile in cachedFiles) {
                    cacheFile.delete()
                }
            }
            cacheDirectory.delete()
            lruEntries.clear()
            currentCacheSize = 0
            currentCacheFilesCount = 0
        }
    }
    // ======== UTILITY ======== //
    /**
     * Escapes characters in a key (which may be a Url) so that it can be
     * safely used as a File name.
     *
     * This is required because otherwise keys having "\\" may be considered
     * as directory path separators.
     */
    private fun encodeKey(toEncodeString: String?): String? {
        try {
            return URLEncoder.encode(toEncodeString, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return null
    }

    private fun readBitmapFromFile(foundCacheFile: File): Bitmap? {
        try {
            val fileInputStream = FileInputStream(foundCacheFile)
            return BitmapFactory.decodeStream(fileInputStream)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(IOException::class)
    private fun saveBitmapToFile(
        file: File,
        bitmapToSave: Bitmap?,
        fileOutputStream: FileOutputStream
    ) {
        fileOutputStream.use { outputStream ->
            bitmapToSave?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            lruEntries[file.absolutePath] = System.currentTimeMillis()
            currentCacheFilesCount = lruEntries.size
            currentCacheSize += file.length()
            trimLRU()
            true
        }
    }

    private fun trimLRU() {

        while( lruEntries.size > maxFilesCount || currentCacheSize > maxSize ) {
            val lastEntryIterable = lruEntries.entries.last()
            lruEntries.remove(lastEntryIterable.key)
            currentCacheSize -= lastEntryIterable.value
            currentCacheFilesCount--
            val oldestFile = File(lastEntryIterable.key)
            oldestFile.delete()
        }
    }

    companion object {

        @Volatile
        private var instance: DiskCache? = null

        @Synchronized
        fun getInstance(cacheDirectory: File, maxSize: Long, maxFilesCount: Int, disposable: CompositeDisposable): DiskCache {
            return instance?.let { instance }
                ?: run {
                    return DiskCache(cacheDirectory, maxSize, maxFilesCount, disposable)
                }
        }
    }

}