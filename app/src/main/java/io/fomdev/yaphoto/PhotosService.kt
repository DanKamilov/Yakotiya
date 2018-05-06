package io.fomdev.yaphoto

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface PhotosService {
    @GET("/search/photos")

    //query=cats&page=1&per_page=20&orientation=landscape&client_id=87c068d18c55166a2fa81c95be0f769fc74c91b1d91636478d4cfedb5cb0f6db
    fun queryGetPhotos(@Query("query") query: String,
                       @Query("page") page: Int,
                       @Query("per_page") per_page: Int,
                       @Query("orientation") orientation: String/*,
                             @Field("client_id") client_id: String*/

    ): Observable<UnsplashResultsResponse>
}