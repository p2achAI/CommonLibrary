package ai.p2ach.p2achandroidlibrary.base.repos


import android.content.Context
import kotlinx.coroutines.flow.Flow


abstract class BaseRepo<T>(){
    abstract fun localFlow(): Flow<T>
    abstract suspend fun saveLocal(data: T)
    abstract suspend fun clearLocal()
    open fun stream(): Flow<T> = localFlow()
}