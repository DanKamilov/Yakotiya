package io.fomdev.yaphoto

import android.app.Fragment
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.photos_fragment.*

/**
 * A placeholder fragment containing a simple view.
 */
class MainActivityFragment : Fragment() {

    private val screenWidth by lazy {
        Point().also {
            (activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                    .defaultDisplay
                    .apply { getSize(it) }
        }.x
    }

    private val photosAdapter = RVPhotosAdapter()

    private var recylcerView: RecyclerView? = null
    private var loadingPanel: LinearLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val photosFragmentLayout = inflater.inflate(R.layout.photos_fragment, container, false)

        recylcerView = photosFragmentLayout.findViewById<RecyclerView>(R.id.recyclerView)
        loadingPanel = photosFragmentLayout.findViewById(R.id.loadingPanelForRV)

        settingRV()

        Data.screenWidth = screenWidth

        setTouchListeners()

        loadLinksOfImages()

        return photosFragmentLayout
    }

    private fun settingRV(){
        recylcerView?.setHasFixedSize(true)
        recylcerView?.setItemViewCacheSize(40)
        recylcerView?.isDrawingCacheEnabled = true
        recylcerView?.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        recylcerView?.adapter = photosAdapter
        recylcerView?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
    }

    private fun loadLinksOfImages(){
        Data.mCompositeDisposable.add(Data.photosRepository.getPhotosObservableByPage(Data.currentPage
        )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { json ->
                            Instruments.fillImagesUrlsList(json)
                        },
                        { error ->
                            error.printStackTrace()
                            showNetError()
                        },
                        {
                            //учтем возможность переключения в раздел Сохраненных
                            if(Data.currentAdapter == Data.LIST_FROM_INTERNET) {
                                showRecyclerView()
                                photosAdapter.notifyItemRangeChanged(0, Data.imagesList.size)
                                photosAdapter.notifyDataSetChanged()
                            }
                        }))
    }

    private fun setTouchListeners(){
        loadingPanel?.setOnTouchListener(object: OnSwipeTouchListener(activity){
            override fun onSwipeBottom() {
                super.onSwipeBottom()
                showProgressBar()
                loadLinksOfImages()
            }
        })
    }

    private fun showProgressBar(){
        progressBarOfMainLoading.visibility = View.VISIBLE
        netErrorTextView.visibility = View.GONE
        netHintTextView.visibility = View.GONE
    }

    private fun showNetError(){
        progressBarOfMainLoading.visibility = View.GONE
        netErrorTextView.visibility = View.VISIBLE
        netHintTextView.visibility = View.VISIBLE
    }

    private fun showRecyclerView(){
        loadingPanel?.visibility = View.GONE
        recylcerView?.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        //обновление адаптера при возврате из FullPhotoActivity
        photosAdapter.notifyItemRangeChanged(0, Data.imagesList.size)
        photosAdapter.notifyDataSetChanged()
    }

    fun getAdapterOfRV():RVPhotosAdapter
    {
        return photosAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        Data.mCompositeDisposable.clear()
    }
}
