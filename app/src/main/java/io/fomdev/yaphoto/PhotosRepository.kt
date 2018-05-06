package io.fomdev.yaphoto

import io.reactivex.Observable

class PhotosRepository {
    fun getPhotosObservableByPage(page: Int): Observable<UnsplashResultsResponse> {
        return RetrofitHelper().photosService.queryGetPhotos(query = "cat",page = page, per_page = 30,orientation = "portrait")
    }
}