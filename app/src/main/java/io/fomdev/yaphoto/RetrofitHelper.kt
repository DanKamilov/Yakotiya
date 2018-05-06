package io.fomdev.yaphoto


import android.os.Build
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.KeyStore
import java.util.*
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import kotlin.collections.ArrayList




internal class RetrofitHelper {

    val photosService: PhotosService by lazy {
        val retrofit = createRetrofit()
        retrofit.create<PhotosService>(PhotosService::class.java)
    }

    private fun createOkHttpClient(): OkHttpClient {
        /*используем для того, чтобы в случае изменений остальных параметров у запроса
        параметр client_id всегда подставлялся сам и его не надо было передавать*/

        var httpClient = OkHttpClient.Builder()

        httpClient
                .addInterceptor { chain ->
            val original = chain.request()
            val originalHttpUrl = original.url()

            val url = originalHttpUrl.newBuilder()
                    .addQueryParameter("client_id", "87c068d18c55166a2fa81c95be0f769fc74c91b1d91636478d4cfedb5cb0f6db")
                    .build()

            // Request customization: add request headers
            val requestBuilder = original.newBuilder()
                    .url(url)

            val request = requestBuilder.build()
            chain.proceed(request)
        }

        if(isTLSEnableNeeded())
           httpClient = enableTls12(client = httpClient)

        return httpClient.build()
    }

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
                .baseUrl("https://api.unsplash.com")
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // <- add this
                .client(createOkHttpClient())
                .build()
    }

    fun isTLSEnableNeeded(): Boolean {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP
    }

    private fun enableTls12(client: OkHttpClient.Builder): OkHttpClient.Builder {
        try {
            val trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            val trustManagers = trustManagerFactory.getTrustManagers()
            if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
                throw IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers))
            }
            val trustManager = trustManagers[0] as X509TrustManager
            client.sslSocketFactory(TLSSocketFactory(), trustManager)
            val cs = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1)
                    .build()
            val specs: ArrayList<ConnectionSpec> = ArrayList()
            specs.add(cs)
            specs.add(ConnectionSpec.COMPATIBLE_TLS)
            specs.add(ConnectionSpec.CLEARTEXT)
            client.connectionSpecs(specs)
        } catch (exc: Exception) {
        }

        return client
    }
}
