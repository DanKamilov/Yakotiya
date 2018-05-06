package io.fomdev.yaphoto

/**
 * Created by DiKey on 23.04.2018.
 */
data class UnsplashResultsResponse(val results: List<UnsplashUrlsResponse>)

data class UnsplashUrlsResponse(val id: String,val urls: UnsplashUrlsDataResponse)

data class UnsplashUrlsDataResponse(
        val raw: String,
        val full: String,
        val regular: String,
        val small: String,
        val thumb: String
)