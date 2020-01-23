package com.alokomkar.pictur.core

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import com.alokomkar.pictur.cache.CacheRepository
import com.alokomkar.pictur.network.NetworkManager
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class Pictur private constructor(
    context: Context,
    cacheSize: Int
) {

    private val networkManager : NetworkManager by lazy { NetworkManager.getInstance() }
    private val disposable : CompositeDisposable by lazy { CompositeDisposable() }
    private val cacheRepository : CacheRepository by lazy { CacheRepository(context.cacheDir, cacheSize, disposable) }
    private val resources: Resources by lazy { context.resources }

    @Synchronized
    fun loadImage( imageUrl : String, placeHolder : Int = -1, errorPlaceHolder: Int, target: ImageView, progressListener: ((inProgress : Boolean, progress: Int) -> Unit)? ) {

        if( placeHolder != -1 )
            target.setImageResource(placeHolder)
        progressListener?.invoke(true, 0)

        val errorBitmap = BitmapFactory.decodeResource(resources, errorPlaceHolder)

        val memoryCacheObservable = Maybe
            .fromCallable { cacheRepository.get(imageUrl) }
            .onErrorComplete()
            .toObservable()

        val networkObservable = Flowable.defer {
            networkManager
                .loadImage(imageUrl, errorBitmap)
                .doOnNext { bitmap -> cacheRepository.put(imageUrl, bitmap) }
        }.toObservable()

        disposable.add(Observable
            .merge(memoryCacheObservable, networkObservable)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe (
                {
                    if( it != null ) target.setImageBitmap(it)
                    progressListener?.invoke(false, 100)
                },
                {
                    Log.e("ImageError", "Error : ${it.message} : ${it.cause}")
                    it.printStackTrace()
                    progressListener?.invoke(false, 100)
                }
            ))
    }

    fun dispose() {
        disposable.dispose()
    }

    companion object {
        private val instance : Pictur? = null
        @Synchronized
        fun getInstance( context: Context, cacheSize: Int ) : Pictur
                =  instance?.let { instance } ?: kotlin.run {
            Pictur(context, cacheSize)
        }
    }
}