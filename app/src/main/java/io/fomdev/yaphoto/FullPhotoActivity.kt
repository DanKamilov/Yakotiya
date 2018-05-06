package io.fomdev.yaphoto

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import io.fomdev.yaphoto.Instruments.Companion.deleteImageFromThePhoneStorage
import io.fomdev.yaphoto.Instruments.Companion.loadImgForFullPhoto
import io.fomdev.yaphoto.Instruments.Companion.rand
import io.fomdev.yaphoto.Instruments.Companion.saveImageToThePhoneStorage
import io.fomdev.yaphoto.Instruments.Companion.toast
import io.fomdev.yaphoto.Instruments.Companion.verifyStoragePermissions
import kotlinx.android.synthetic.main.activity_full_photo.*
import kotlinx.android.synthetic.main.content_full_photo.*
import java.io.File


class FullPhotoActivity : AppCompatActivity() {

    private val namesOfCats = arrayOf("Айсберг", "Аскольд", "Астерикс", "Аякс", "Артур", "Асклепий", "Арес", "Атос", "Арамис", "Август", "Альбус", "Алмаз,Баксик", "Бакстер", "Брюс", "Базальт", "Базилио", "Базилик", "Боско", "Бостон", "Батон", "Бисквит", "Бисер", "Боцман", "Васаби", "Винсент", "Валдис", "Вирус", "Ватсон", "Висмут", "Вермут", "Витас", "Витязь", "Влас", "Василек", "Вольтер", "Густав", "Гелиос", "Гефест", "Гусар", "Гизмо", "Гусляр", "Гораций", "Ганс", "Грызя", "Гиннесс", "Гуффи", "Голиаф", "Джакс", "Декстер", "Диксон", "Деймос", "Даллас", "Декарт", "Джерри", "Джексон", "Джедай", "Дениска", "Джойстик", "Дорофей", "Есаул", "Есенин", "Евсей", "Енисей", "Елисей", "Ерофей", "Ёжик", "Евстахий", "Елизар", "Евфрат", "Евклид", "Енох", "Жозеф", "Жустьен", "Жгутик", "Живчик", "Жулик", "Жусик", "Жорик", "Жужик", "Жако", "Жофрей", "Жорж", "Жикар", "Зевс", "Зодиак", "Звездочет", "Зигмунд", "Задира", "Забияка", "Завулон", "Зенит", "Зефир", "Зорро", "Зидан", "Захар", "Изюмчик", "Иртыш", "Иствуд", "Изумруд", "Иосиф", "Искандер", "Ипполит", "Икар", "Ивасик", "Ивашка", "Йогурт", "Йосик", "Кекс", "Кузьма", "Круассан", "Карабас", "Космос", "Конфуций", "Кёртис", "Коржик", "Косинус", "Колбасыч", "Каспер", "Кастор", "Ластик", "Лизун", "Лютик", "Лоскутик", "Люциус", "Лакмус", "Ляпис", "Люсьен", "Ланселот", "Лунатик", "Лоуренс", "Лисенок", "Мэйсон", "Маркиз", "Мультик", "Матроскин", "Марсианин", "Мушкетер", "Максимус", "Моцарт", "Муфаса", "Мяут", "Морсик", "Маркус", "Нарцисс", "Норрис", "Несквик", "Нафталин", "Нейтрон", "Нельсон", "Наперсток", "Николас", "Ниндзя", "Нильс", "Никсон", "Нафаня", "Осинка", "Орнетта", "Олфи", "Оникса", "Орнесса", "Октавия", "Орхидея", "Оливка", "Офелия", "Одалиска", "Оклахома", "Оскомина", "Персик", "Паштет", "Пиксель", "Пирожок", "Пуфик", "Плюсик", "Потягуш", "Позитив", "Паскаль", "Пафнутий", "Патриций", "Портос", "Рататуй", "Русик", "Ролекс", "Руфус", "Рафинад", "Рамзик", "Рамзес", "Расстегай", "Ришелье", "Рузвельт", "Рассел", "Ритц", "Рулет", "Светофор", "Северус", "Синдбад", "Спартак", "Степашка", "Сталкер", "Сенатор", "Салазар", "Серафим", "Сименс", "Сапфир", "Султан", "Тайсон", "Тайфун", "Тарзан", "Тигриус", "Тобиас", "Томас", "Таймс", "Тиграсик", "Трофей", "Тимофей", "Тристан", "Твиксик", "Уксус", "Уинстон", "Урчун", "Уголек", "Утюжок", "Укроп", "Уэсли", "Умка", "Урфин", "Улисс", "Уиллис", "Ульрик", "Фантик", "Феликс", "Филлипс", "Фыркун", "Флоризель", "Фауст", "Франтик", "Форест", "Фуксик", "Фикус", "Фантомас", "Флинстоун", "Хьюстон", "Хоттабыч", "Харитон", "Харрис", "Хрумстик", "Хрустик", "Хаус", "Хельсинг", "Хичкок", "Хакер", "Херес", "Холмс", "Цитрус", "Цент", "Цукат", "Цельсий", "Цезарь", "Центурион", "Цербер", "Цитрон", "Циник", "Цыган", "Цицерон", "Царапыч", "Чешир", "Чертенок", "Черныш", "Чебурек", "Черномор", "Чезарро", "Чаплин", "Чарльз", "Черчилль", "Чингисхан", "Чупа-Чупс", "Чупик", "Шалфей", "Шарфик", "Шерхан", "Шнырик", "Шуршик", "Шостакович", "Шуруп", "Шумахер", "Шашлык", "Шериф", "Шкет", "Шпунтик", "Эдик", "Эдвард", "Эвклид", "Элвис", "Эйнштейн", "Эдисон", "Эльбрус", "Энтони", "Эскулап", "Эскимос", "Эдельвейс", "Эрнесто", "Юджин", "Юпитер", "Юникс", "Ювентус", "Юстас", "Юлиус", "Юрасик", "Юлик", "Ютуб", "Юзер", "Юнец", "Юкос", "Яндекс", "Ярослав", "Ясон", "Ягуар", "Яхонт", "Янтарь", "Яшка", "Ярило", "Ярофей", "Яромир", "Янчик", "Ясик")

    private var pathOfSavedImage: String = ""

    private var shareMenuItem: MenuItem? = null
    private var saveMenuItem: MenuItem? = null
    private var deleteMenuItem: MenuItem? = null

    private var currentImagePosition = 0
    private var urlOfImage = ""
    private var idOfImage = ""
    private var randomIndexOfCatsName = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_photo)

        settingToolbar()

        readIntentData()

        setTouchListeners()
    }

    private fun settingToolbar(){
        randomIndexOfCatsName = rand(0, namesOfCats.size - 1)

        toolbarFullPhoto.title = "${resources.getString(R.string.cat)} ${namesOfCats[randomIndexOfCatsName]}"

        setSupportActionBar(toolbarFullPhoto)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }

    private fun readIntentData(){
        currentImagePosition = this.intent.getIntExtra("position", 0)

        if (this.intent.getStringExtra("urlOfImage") != null)
            urlOfImage = this.intent.getStringExtra("urlOfImage")

        if (this.intent.getStringExtra("idOfImage") != null)
            idOfImage = this.intent.getStringExtra("idOfImage")
    }

    private fun setTouchListeners()
    {
        loadingPanelForFullImage.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeBottom() {
                super.onSwipeBottom()

                showProgressBar()

                loadImgForFullPhoto(
                        fullPhotoImageView,
                        urlOfImage,
                        loadingPanelForFullImage,
                        progressBarOfFullPhotoLoading,
                        netErrorTextView,
                        netHintTextView,
                        shareMenuItem!!,
                        saveMenuItem!!,
                        deleteMenuItem!!)
            }
        })

        fullPhotoImageView.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeTop() {
                onBackPressed()
                finish()
            }

            override fun onSwipeLeft() {
                if (currentImagePosition < Data.imagesList.size - 1) {

                    showProgressBar()

                    reSettingShareDeleteSaveButtons()

                    showLoadingPanel()

                    currentImagePosition += 1
                    urlOfImage = Data.imagesList[currentImagePosition].regular
                    idOfImage = Data.imagesList[currentImagePosition].id

                    checkIfImageAlreadySaved()

                    randomIndexOfCatsName = rand(0, namesOfCats.size - 1)
                    toolbarFullPhoto.title = "${resources.getString(R.string.cat)} ${namesOfCats[randomIndexOfCatsName]}"

                    loadImgForFullPhoto(
                            fullPhotoImageView,
                            urlOfImage,
                            loadingPanelForFullImage,
                            progressBarOfFullPhotoLoading,
                            netErrorTextView,
                            netHintTextView,
                            shareMenuItem!!,
                            saveMenuItem!!,
                            deleteMenuItem!!)
                }
            }

            override fun onSwipeRight() {
                if (currentImagePosition > 0) {

                    showProgressBar()

                    reSettingShareDeleteSaveButtons()

                    showLoadingPanel()

                    currentImagePosition -= 1
                    urlOfImage = Data.imagesList[currentImagePosition].regular
                    idOfImage = Data.imagesList[currentImagePosition].id

                    checkIfImageAlreadySaved()

                    randomIndexOfCatsName = rand(0, namesOfCats.size - 1)
                    toolbarFullPhoto.title = "${resources.getString(R.string.cat)} ${namesOfCats[randomIndexOfCatsName]}"

                    loadImgForFullPhoto(
                            fullPhotoImageView,
                            urlOfImage,
                            loadingPanelForFullImage,
                            progressBarOfFullPhotoLoading,
                            netErrorTextView,
                            netHintTextView,
                            shareMenuItem!!,
                            saveMenuItem!!,
                            deleteMenuItem!!)
                }
            }
        })
    }

    private fun showProgressBar(){
        progressBarOfFullPhotoLoading.visibility = View.VISIBLE
        netErrorTextView.visibility = View.GONE
        netHintTextView.visibility = View.GONE
    }

    private fun showLoadingPanel(){
        fullPhotoImageView.visibility = View.GONE
        loadingPanelForFullImage.visibility = View.VISIBLE
    }

    private fun reSettingShareDeleteSaveButtons(){
        saveMenuItem!!.isVisible = true
        deleteMenuItem!!.isVisible = false
        shareMenuItem!!.isEnabled = false
        saveMenuItem!!.isEnabled = false
        deleteMenuItem!!.isEnabled = false
        shareMenuItem!!.setIcon(R.drawable.ic_share_grey_24dp)
        saveMenuItem!!.setIcon(R.drawable.ic_save_grey_24dp)
        deleteMenuItem!!.setIcon(R.drawable.ic_delete_grey_24dp)
    }

    private fun checkIfImageAlreadySaved(){
        for (path in Data.pathsOfSavedPhotos) {
            if (path.contains(idOfImage)) {
                saveMenuItem!!.isVisible = false
                deleteMenuItem!!.isVisible = true
                pathOfSavedImage = path
                break
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.full_photo_menu, menu)

        shareMenuItem = menu.findItem(R.id.menu_share)
        saveMenuItem = menu.findItem(R.id.menu_save)
        deleteMenuItem = menu.findItem(R.id.menu_delete)


        loadImgForFullPhoto(
                fullPhotoImageView,
                urlOfImage,
                loadingPanelForFullImage,
                progressBarOfFullPhotoLoading,
                netErrorTextView,
                netHintTextView,
                shareMenuItem!!,
                saveMenuItem!!,
                deleteMenuItem!!)

        for (path in Data.pathsOfSavedPhotos) {
            if (path.contains(idOfImage)) {
                saveMenuItem!!.isVisible = false
                deleteMenuItem!!.isVisible = true
                pathOfSavedImage = path
                break
            }
        }

        return true
    }

    private fun doShare() {
        val intent = Intent(Intent.ACTION_SEND)

        if (urlOfImage.contains("unsplash")) {
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, "Смотри какой кот!\n\n" +
                    urlOfImage)
            startActivity(intent)
        } else {
            intent.type = "image/jpeg"
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(urlOfImage)))
            startActivity(Intent.createChooser(intent, "Смотри какой кот!"))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.menu_share -> {
                doShare()
                return true
            }
            R.id.menu_save -> {
                if (verifyStoragePermissions(this)) {
                    saveMenuItem!!.isEnabled = false
                    deleteMenuItem!!.isEnabled = false
                    val imageOfCat = (fullPhotoImageView.drawable as BitmapDrawable).bitmap

                    saveImageToThePhoneStorage(imageOfCat, idOfImage)

                    toast(resources.getString(R.string.cat_saved))
                    saveMenuItem!!.isVisible = false
                    deleteMenuItem!!.isVisible = true
                    deleteMenuItem!!.isEnabled = true
                }
                return true
            }
            R.id.menu_delete -> {
                if (verifyStoragePermissions(this)) {
                    deleteMenuItem!!.isEnabled = false
                    saveMenuItem!!.isEnabled = false
                    deleteImageFromThePhoneStorage(pathOfSavedImage)

                    toast(resources.getString(R.string.cat_deleted))

                    deleteMenuItem!!.isVisible = false
                    saveMenuItem!!.isVisible = true
                    saveMenuItem!!.isEnabled = true
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
