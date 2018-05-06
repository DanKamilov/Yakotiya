package io.fomdev.yaphoto

import android.content.Context
import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import io.fomdev.yaphoto.Data.Companion.imagesList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by DiKey on 24.04.2018.
 */

class RVPhotosAdapter() : RecyclerView.Adapter<RVPhotosAdapter.ViewHolder>() {

    private var context: Context? = null

    private val VIEW_TYPE_CARD = 1
    private val VIEW_TYPE_FOOTER = 2

    private val intentToFullImageActivity by lazy {
        Intent(context, FullPhotoActivity::class.java)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RVPhotosAdapter.ViewHolder {
        var neededCardView = R.layout.photos_cardview
        if (viewType == VIEW_TYPE_FOOTER) {
            neededCardView = R.layout.bottom_button_cardview
        }

        val cardView = LayoutInflater
                .from(parent!!.context)
                .inflate(neededCardView, parent, false) as CardView
        context = parent.context
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val cardView = holder.cardView

        if (holder.itemViewType == VIEW_TYPE_CARD) {

            val firstPhotoButton = cardView.findViewById<ImageView>(R.id.first_photo)
            val secondPhotoButton = cardView.findViewById<ImageView>(R.id.second_photo)
            val firstLoadingPanel = cardView.findViewById<RelativeLayout>(R.id.loadingPanel1)
            val secondLoadingPanel = cardView.findViewById<RelativeLayout>(R.id.loadingPanel2)

            firstLoadingPanel.getLayoutParams().width = Data.screenWidth / 2
            firstLoadingPanel.getLayoutParams().height = Data.screenWidth / 2

            secondLoadingPanel.getLayoutParams().width = Data.screenWidth / 2
            secondLoadingPanel.getLayoutParams().height = Data.screenWidth / 2

            firstPhotoButton.getLayoutParams().width = Data.screenWidth / 2
            firstPhotoButton.getLayoutParams().height = Data.screenWidth / 2

            firstPhotoButton.setOnClickListener {
                intentToFullImageActivity.putExtra("urlOfImage", imagesList[2 * position].regular)
                intentToFullImageActivity.putExtra("idOfImage", imagesList[2 * position].id)
                intentToFullImageActivity.putExtra("position", 2 * position)
                context!!.startActivity(intentToFullImageActivity)
            }

            Instruments.loadImg(firstPhotoButton, imagesList[2 * position].small, firstLoadingPanel)

            if (2 * position + 1 < imagesList.size) {
                /*настраиваем(размеры в пол ширины экрана) и делаем видимым второе фото из карда,
                если позиция входит в размеры массива(количество изображений четно)*/

                secondPhotoButton.getLayoutParams().width = Data.screenWidth / 2
                secondPhotoButton.getLayoutParams().height = Data.screenWidth / 2

                secondPhotoButton.setOnClickListener {
                    intentToFullImageActivity.putExtra("urlOfImage", imagesList[2 * position + 1].regular)
                    intentToFullImageActivity.putExtra("idOfImage", imagesList[2 * position + 1].id)
                    intentToFullImageActivity.putExtra("position", (2 * position + 1))
                    context!!.startActivity(intentToFullImageActivity)
                }
                Instruments.loadImg(secondPhotoButton, imagesList[2 * position + 1].small, secondLoadingPanel)

            } else {
                /*Если количество изображений нечетно, то последнему фото ставим размеры в ширину
                экрана и скрываем 2ое imageView*/
                firstPhotoButton.getLayoutParams().width = Data.screenWidth
                firstPhotoButton.getLayoutParams().height = Data.screenWidth
                secondPhotoButton.visibility = View.GONE
                secondLoadingPanel.visibility = View.GONE
            }
        } else {//Если долистали до конца и пришла кнопка 'Хочу еще!'
            val moreCatsButton = cardView.findViewById<Button>(R.id.more_cats)

            moreCatsButton.setOnClickListener {

                Data.currentPage += 1
                moreCatsButton.text = context?.resources?.getString(R.string.loading)
                moreCatsButton.isClickable = false

                if (Data.currentPage <= 4) {
                    //ограничим число загруженх фото 4*30. Чтобы люди не залипали слишком на долго
                    Data.mCompositeDisposable.add(
                            Data.photosRepository
                                    .getPhotosObservableByPage(Data.currentPage)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    { json ->
                                        Instruments.fillImagesUrlsList(json)
                                    },
                                    { error ->
                                        error.printStackTrace()
                                        Data.currentPage -= 1
                                        moreCatsButton.text = context?.resources?.getString(R.string.net_error_on_button)
                                        moreCatsButton.isClickable = true
                                    },
                                    {
                                        notifyItemRangeChanged(0, Data.imagesList.size)
                                        notifyDataSetChanged()
                                        moreCatsButton.text = context?.resources?.getString(R.string.want_more)
                                        moreCatsButton.isClickable = true
                                    }))

                } else {
                    moreCatsButton.text = context?.resources?.getString(R.string.cats_ends)
                    moreCatsButton.isClickable = false
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (isList()) {
            if (imagesList.size % 2 != 0)
                return if (position == (imagesList.size / 2 + 1)) VIEW_TYPE_FOOTER else VIEW_TYPE_CARD
            else
                return if (position == imagesList.size / 2) VIEW_TYPE_FOOTER else VIEW_TYPE_CARD
        } else//если выбран раздел Сохраненных
            return VIEW_TYPE_CARD
    }

    override fun getItemCount(): Int {
        if (isList()) {
            if (imagesList.size % 2 != 0)
                return (imagesList.size / 2) + 2//доп. кард для лишнего изображения(нечетное) + кнопка Хочу еще
            else
                return (imagesList.size / 2) + 1//изза кнопки Хочу еще
        } else {
            if (imagesList.size % 2 != 0)//(в разделе Сохраненных кнопка ХОЧУ ЕЩЕ - не нужна
                return (imagesList.size / 2) + 1//доп. кард для лишнего изображения(нечетное)
            else
                return imagesList.size / 2
        }
    }

    fun isList(): Boolean {
        var isList = false
        for (element in imagesList) {
            if (element.small.contains("unsplash")) {
                isList = true
                break
            }
        }
        return isList
    }

    class ViewHolder(val cardView: CardView) : RecyclerView.ViewHolder(cardView)
}