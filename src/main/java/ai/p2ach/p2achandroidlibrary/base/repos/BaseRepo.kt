package ai.p2ach.p2achandroidlibrary.base.repos

import kotlinx.coroutines.CancellationException
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

abstract class BaseRepo {
    interface RemoteData<S: Any> { val remote: S }
    interface LocalData<D: Any> { val local: D }
    interface NativeData<N: Any> { val native: N }

    data class NetworkConfig(
        val baseUrl: String,
        val connectTimeoutSec: Long = 10,
        val readTimeoutSec: Long = 30,
        val writeTimeoutSec: Long = 30,
        val enableHttpLogging: Boolean = false,
        val interceptors: List<Interceptor> = emptyList()
    )

    class RetrofitClient(private val config: NetworkConfig) {
        private val retrofit by lazy {
            val builder = OkHttpClient.Builder()
                .connectTimeout(config.connectTimeoutSec, TimeUnit.SECONDS)
                .readTimeout(config.readTimeoutSec, TimeUnit.SECONDS)
                .writeTimeout(config.writeTimeoutSec, TimeUnit.SECONDS)
            if (config.enableHttpLogging) {
                builder.addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            }
            config.interceptors.forEach { builder.addInterceptor(it) }
            val client = builder.build()
            Retrofit.Builder()
                .baseUrl(requireNotNull(config.baseUrl.ifBlank { null }))
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }
        fun <T: Any> create(service: Class<T>): T = retrofit.create(service)
    }

    sealed class ApiResult<out T> {
        data class Success<T>(val data: T): ApiResult<T>()
        data class NetworkError(val cause: Throwable): ApiResult<Nothing>()
        data class HttpError(val code: Int, val body: String?): ApiResult<Nothing>()
        data class UnknownError(val throwable: Throwable): ApiResult<Nothing>()
    }

    suspend inline fun <T> safeApiCall(crossinline block: suspend () -> T): ApiResult<T> {
        return try {
            ApiResult.Success(block())
        } catch (ce: CancellationException) {
            throw ce
        } catch (he: HttpException) {
            val body = try { he.response()?.errorBody()?.string() } catch (_: Throwable) { null }
            ApiResult.HttpError(he.code(), body)
        } catch (io: IOException) {
            ApiResult.NetworkError(io)
        } catch (t: Throwable) {
            ApiResult.UnknownError(t)
        }
    }
}