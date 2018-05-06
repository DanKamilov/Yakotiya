package io.fomdev.yaphoto

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import android.support.v4.app.ActivityCompat
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * Created by DiKey on 30.04.2018.
 */
class Instruments {

    companion object {
        private val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        fun fillImagesUrlsList(json: UnsplashResultsResponse) {
            if (json.results.size > 0 && (Data.currentAdapter == Data.LIST_FROM_INTERNET)) {
                var i = 0
                while (i < json.results.size) {
                    Data.imagesList.add(
                            CatImage(
                                    json.results[i].id,
                                    json.results[i].urls.raw,
                                    json.results[i].urls.full,
                                    json.results[i].urls.regular,
                                    json.results[i].urls.small,
                                    json.results[i].urls.thumb))

                    Data.cachedLinksOfImages.add(
                            CatImage(
                                    json.results[i].id,
                                    json.results[i].urls.raw,
                                    json.results[i].urls.full,
                                    json.results[i].urls.regular,
                                    json.results[i].urls.small,
                                    json.results[i].urls.thumb))
                    i += 1
                }
            }
        }

        fun loadImg(imageView: ImageView,
                    imageUrl: String,
                    loadingPanel: RelativeLayout) {

            var pathToImage = imageUrl
            if (!imageUrl.contains("unsplash.com"))
                pathToImage = "file://"+imageUrl

            Picasso.get().load(pathToImage).into(imageView, object : Callback {
                override fun onSuccess() {
                    loadingPanel.visibility = View.GONE
                    imageView.visibility = View.VISIBLE
                }

                override fun onError(e: Exception) {
                }
            })
        }

        fun loadImgForFullPhoto(
                imageView: ImageView,
                imageUrl: String,
                loadingPanel: LinearLayout,
                progressBar: ProgressBar,
                netErrorTextView: TextView,
                netHintTextView: TextView,
                shareItem: MenuItem,
                saveItem: MenuItem,
                deleteItem: MenuItem) {

            var pathToImage = imageUrl
            if (!imageUrl.contains("unsplash.com"))
                pathToImage = "file://"+imageUrl

            Picasso.get().load(pathToImage).into(imageView, object : Callback {
                override fun onSuccess() {
                    loadingPanel.visibility = View.GONE
                    imageView.visibility = View.VISIBLE
                    shareItem.setEnabled(true)
                    saveItem.setEnabled(true)
                    deleteItem.setEnabled(true)

                    shareItem.setIcon(R.drawable.ic_share_black_24dp)
                    saveItem.setIcon(R.drawable.ic_save_black_24dp)
                    deleteItem.setIcon(R.drawable.ic_delete_black_24dp)
                }

                override fun onError(e: Exception) {
                    Picasso.get().cancelRequest(imageView)
                    progressBar.visibility = View.GONE
                    netErrorTextView.visibility = View.VISIBLE
                    netHintTextView.visibility = View.VISIBLE
                }
            })
        }

        fun deleteImageFromThePhoneStorage(pathOfSavedImage: String) {

            val file = File(pathOfSavedImage)
            file.delete()

            Data.pathsOfSavedPhotos.remove(pathOfSavedImage)

            Data.editor.remove(Data.APP_PREFERENCES_SAVED_IMAGES)
            Data.editor.apply()
            Data.editor.putStringSet(Data.APP_PREFERENCES_SAVED_IMAGES, Data.pathsOfSavedPhotos)
            Data.editor.apply()

            if (Data.currentAdapter == Data.SAVED_PHOTOS) {
                //Если мы в разделе сохраненных, то обновляем список изображений в адаптере

                Data.imagesList.clear()

                for (pathToSavedImage in Data.pathsOfSavedPhotos) {
                    val fileName = getFileNameFromString(pathToSavedImage)
                    Data.imagesList.add(CatImage(
                            fileName,
                            pathToSavedImage,
                            pathToSavedImage,
                            pathToSavedImage,
                            pathToSavedImage,
                            pathToSavedImage))
                }
            }
        }

        fun getFileNameFromString(path: String): String {
            var cut = path.lastIndexOf('/')

            val fileName = path.substring(cut + 1, path.length - 4)
            if (fileName.contains("%2F")) {
                cut = fileName.lastIndexOf("%2F")
                return fileName.substring(cut + 1)
            }
            return fileName

        }

        fun getFileNameFromUri(contentResolver: ContentResolver, uri: Uri): String {
            var result: String? = null
            if (uri.scheme.equals("content")) {
                val cursor = contentResolver.query(uri, null, null, null, null)
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                } finally {
                    cursor!!.close()
                }
            }
            if (result == null) {
                result = uri.path
                val cut = result!!.lastIndexOf('/')
                if (cut != -1) {
                    result = result.substring(cut + 1)
                }
            }
            return result.substring(0, result.length - 4)
        }

        fun saveImageToThePhoneStorage(finalBitmap: Bitmap, name: String) {
            try {
                val fname = "/$name.jpg"

                Data.pathsOfSavedPhotos.add(Data.PATH_TO_IMAGES.toString() + fname)

                Data.editor.remove(Data.APP_PREFERENCES_SAVED_IMAGES)
                Data.editor.apply()
                Data.editor.putStringSet(Data.APP_PREFERENCES_SAVED_IMAGES, Data.pathsOfSavedPhotos)
                Data.editor.apply()

                Data.PATH_TO_IMAGES.mkdirs()

                val file = File(Data.PATH_TO_IMAGES, fname)
                if (file.exists())
                    file.delete()

                file.createNewFile()
                val out = FileOutputStream(file)
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun rand(from: Int, to: Int): Int {
            return Random().nextInt(to - from) + from
        }

        fun Context.toast(message: CharSequence) =
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        fun verifyStoragePermissions(activity: Activity): Boolean {
            // Check if we have write permission
            val permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        activity,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                )

                return false
            }

            return true
        }
    }
}