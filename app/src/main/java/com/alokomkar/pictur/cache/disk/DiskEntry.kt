package com.alokomkar.pictur.cache.disk

data class DiskEntry(
    val fileEntry: String,
    val fileAccessTimeStamp: Long
) : Comparable<DiskEntry> {

    override fun compareTo(other: DiskEntry): Int {
        return (fileAccessTimeStamp - other.fileAccessTimeStamp).toInt()
    }

}