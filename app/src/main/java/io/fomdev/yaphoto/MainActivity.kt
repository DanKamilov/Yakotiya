package io.fomdev.yaphoto

import android.app.Activity
import android.app.Fragment
import android.app.FragmentTransaction
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import io.fomdev.yaphoto.Instruments.Companion.fillImagesUrlsList
import io.fomdev.yaphoto.Instruments.Companion.getFileNameFromString
import io.fomdev.yaphoto.Instruments.Companion.getFileNameFromUri
import io.fomdev.yaphoto.Instruments.Companion.getPathsOfSavedImagesFromSharedPrefs
import io.fomdev.yaphoto.Instruments.Companion.saveImageToThePhoneStorage
import io.fomdev.yaphoto.Instruments.Companion.savePathsOfSavedImagesToSharedPrefs
import io.fomdev.yaphoto.Instruments.Companion.toast
import io.fomdev.yaphoto.Instruments.Companion.verifyStoragePermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.photos_fragment.*
import java.io.File


class MainActivity : AppCompatActivity() {

    private val redBottomBorder by lazy {
        getBorders(
                Color.WHITE, // Background color
                ContextCompat.getColor(applicationContext, R.color.colorAccent), // Border color
                0, // Left border in pixels
                0, // Top border in pixels
                0, // Right border in pixels
                10 // Bottom border in pixels
        )
    }

    private val whiteBottomBorder by lazy {
        getBorders(
                Color.WHITE, // Background color
                ContextCompat.getColor(applicationContext, R.color.colorPrimaryWhite), // Border color
                0, // Left border in pixels
                0, // Top border in pixels
                0, // Right border in pixels
                10 // Bottom border in pixels
        )
    }

    private var photosAdapterFromFragment: RVPhotosAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settingToolbar()

        settingAndCreateSharedPreferences()

        settingFabButton()

        setClickListenersForHeaderButtons()

        val fragment: Fragment
        fragment = MainActivityFragment()
        photosAdapterFromFragment = fragment.getAdapterOfRV()
        val ft = fragmentManager.beginTransaction()
        ft.replace(R.id.content_frame, fragment, "visible_fragment")
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.commit()
    }

    private fun getBorders(bgColor: Int, borderColor: Int,
            //настраивает нижнюю границу для кнопки хедера
                           left: Int, top: Int, right: Int, bottom: Int): LayerDrawable {
        val borderColorDrawable = ColorDrawable(borderColor)
        val backgroundColorDrawable = ColorDrawable(bgColor)

        val drawables = arrayOf<Drawable>(borderColorDrawable, backgroundColorDrawable)

        val layerDrawable = LayerDrawable(drawables)

        layerDrawable.setLayerInset(
                1,
                left,
                top,
                right,
                bottom
        )

        return layerDrawable
    }

    private fun settingToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    private fun settingFabButton() {
        fab.setOnClickListener { view ->
            if (verifyStoragePermissions(this)) {
                view.isEnabled = false
                val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 1)
            }
        }
    }

    private fun setClickListenersForHeaderButtons() {
        buttonAll.background = redBottomBorder

        buttonAll.setOnClickListener {

            Data.currentAdapter = Data.LIST_FROM_INTERNET

            hideFabButton()

            setRedUnderlineForAllButton()

            if (Data.cachedLinksOfImages.size < 30) {
                /*Если хотя бы 30 путей для загрузки картинок не закешированы в массиве, то
                даем асинхронный запрос на получение путей для загрузки картинок*/
                showLoadingPanel()

                Data.imagesList.clear()

                loadLinksOfImages()

            } else {
                Data.imagesList.clear()
                /*Если уже успело загрузиться 30 или более путей для выкачки
                (то не даем запрос на получение путей для загруки, а получаем пути из кешМассива */
                Data.currentPage = Data.cachedLinksOfImages.size/30
                /*вычисляем текущую страницу подгрузки(по 30 на странице)*/

                for (catImageUrl in Data.cachedLinksOfImages)
                    Data.imagesList.add(catImageUrl)

                photosAdapterFromFragment!!.notifyItemRangeChanged(0, Data.imagesList.size)
                photosAdapterFromFragment!!.notifyDataSetChanged()
            }
        }

        buttonSaved.setOnClickListener {

            stopLoadImagesFromInternet()

            Data.currentAdapter = Data.SAVED_PHOTOS

            Data.imagesList.clear()

            showRecyclerView()

            showFabButton()

            setRedUnderlineForSavedButton()

            Data.imagesList.clear()
            //получаем пути сохр.картинок из SharedPrefs
            if (Data.infoAboutUser!!.contains("Array_size")) {

                getPathsOfSavedImagesFromSharedPrefs()

                for (pathToSavedImage in Data.pathsOfSavedPhotos) {
                    //чистим массив сохраненных картинок,
                    // от картинок, которые были удалены пользователем из галлереи вручную
                    val file = File(pathToSavedImage)
                    if (!file.exists())
                        Data.pathsOfSavedPhotos.remove(pathToSavedImage)
                }

                savePathsOfSavedImagesToSharedPrefs()

                for (pathToSavedImage in Data.pathsOfSavedPhotos) {//заполняем массив для адаптера
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

            photosAdapterFromFragment!!.notifyItemRangeChanged(0, Data.imagesList.size)
            photosAdapterFromFragment!!.notifyDataSetChanged()
        }
    }

    private fun loadLinksOfImages(){
        Data.mCompositeDisposable.add(Data.photosRepository.getPhotosObservableByPage(Data.currentPage
        )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { json ->
                            fillImagesUrlsList(json)
                        },
                        { error ->
                            error.printStackTrace()
                            showNetError()
                        },
                        {
                            showRecyclerView()
                            photosAdapterFromFragment!!.notifyItemRangeChanged(0, Data.imagesList.size)
                            photosAdapterFromFragment!!.notifyDataSetChanged()
                        }))
    }

    private fun stopLoadImagesFromInternet() = Data.mCompositeDisposable.clear()

    private fun showRecyclerView() {
        recyclerView.visibility = View.VISIBLE
        loadingPanelForRV.visibility = View.GONE
    }

    private fun showLoadingPanel(){
        recyclerView.visibility = View.GONE
        loadingPanelForRV.visibility = View.VISIBLE
    }

    private fun showNetError(){
        progressBarOfMainLoading.visibility = View.GONE
        netErrorTextView.visibility = View.VISIBLE
        netHintTextView.visibility = View.VISIBLE
    }

    private fun showFabButton() {
        fab.visibility = View.VISIBLE
    }

    private fun hideFabButton() {
        fab.visibility = View.GONE
    }

    private fun setRedUnderlineForAllButton(){
        buttonAll.background = redBottomBorder
        buttonSaved.background = whiteBottomBorder
    }

    private fun setRedUnderlineForSavedButton(){
        buttonSaved.background = redBottomBorder
        buttonAll.background = whiteBottomBorder
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        fab.isEnabled = true

        when (requestCode) {
            1 -> if (resultCode == Activity.RESULT_OK) {
                val uri = data!!.data

                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

                val fileName = getFileNameFromUri(this.contentResolver,uri)

                val pathToImage = "${Data.PATH_TO_IMAGES}/$fileName.jpg"

                if (!Data.pathsOfSavedPhotos.contains(pathToImage)) {
                    Data.imagesList.add(0,
                            CatImage(
                                    fileName,
                                    pathToImage,
                                    pathToImage,
                                    pathToImage,
                                    pathToImage,
                                    pathToImage))
                    photosAdapterFromFragment!!.notifyItemRangeChanged(0, Data.imagesList.size)
                    photosAdapterFromFragment!!.notifyDataSetChanged()
                }

                saveImageToThePhoneStorage(bitmap, fileName)
                toast(resources.getString(R.string.cat_saved))
            }
            else -> {
            }
        }
    }

    private fun settingAndCreateSharedPreferences() {
        if (Data.infoAboutUser == null)
            Data.infoAboutUser = getSharedPreferences(Data.APP_PREFERENCES, Context.MODE_PRIVATE)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_dev -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/DanKamilov"))
                startActivity(browserIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
