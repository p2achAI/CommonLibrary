package ai.p2ach.p2achandroidlibrary.base.repos




import ai.p2ach.p2achandroidlibrary.utils.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


/*Repository 패턴으로 구현하려면 하나의 Repo에 Remote, Local의 대한 데이터 소스가 같이 있는게 보기좋은데,
* retrofit 객체를 초기화하고, api 객체를 불러오는 부분에서 많은 boilerplate code가 필요하여 추상 클래스로 작성.
* */


abstract class BaseRepo<API_SERVICE : Any>(
) {

    lateinit var pref : RetrofitSettingClass
    abstract fun buildRetrofitPref() : RetrofitSettingClass

    data class RetrofitSettingClass( var connectionTimeOut : Long?=null,
                                  var readTimeOut : Long?=null,
                                  var writeTimeOut : Long?=null,
                                  var baseUrl : String?=null,
    )

    open fun apiClass(): Class<API_SERVICE>? = null

    val apiService: API_SERVICE? by lazy {
        apiClass()?.takeIf { it.isInterface }?.let {
            buildRetrofit().create(it)
        } ?: run {
            null
        }
    }

    private fun buildRetrofit(): Retrofit {

        pref = buildRetrofitPref()

        val client = OkHttpClient.Builder()
            .apply {
                connectTimeout(pref.connectionTimeOut?:0L, TimeUnit.SECONDS)
                readTimeout(pref.readTimeOut?:0L, TimeUnit.SECONDS)
                writeTimeout(pref.writeTimeOut?:0L ,TimeUnit.SECONDS)
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }.build()

        return Retrofit.Builder()
            .baseUrl(pref.baseUrl?:"")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

}