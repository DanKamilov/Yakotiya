package io.fomdev.yaphoto

import android.content.SharedPreferences
import android.os.Environment
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by DiKey on 27.04.2018.
 */
class Data {
    companion object {
        var LIST_FROM_INTERNET = 0
        var SAVED_PHOTOS = 1
        var mCompositeDisposable: CompositeDisposable = CompositeDisposable()
        val photosRepository = PhotosRepository()
        var imagesList: ArrayList<CatImage> = ArrayList()
        var cachedLinksOfImages: ArrayList<CatImage> = ArrayList()
        var screenWidth: Int = 0
        var currentPage: Int = 1
        var pathsOfSavedPhotos: HashSet<String> = HashSet()
        var currentAdapter: Int = LIST_FROM_INTERNET

        val editor by lazy {
            Data.infoAboutUser!!.edit()
        }
        var infoAboutUser: SharedPreferences? = null

        val APP_PREFERENCES = "usersettings"
        val APP_PREFERENCES_SAVED_IMAGES = "saved_images"

        val PATH_TO_IMAGES = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES)
    }

}
