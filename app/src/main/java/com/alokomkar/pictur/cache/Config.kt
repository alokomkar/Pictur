package com.alokomkar.pictur.cache

val maxMemory = Runtime.getRuntime().maxMemory() / 1024
val defaultCacheSize = (maxMemory / 4).toInt()