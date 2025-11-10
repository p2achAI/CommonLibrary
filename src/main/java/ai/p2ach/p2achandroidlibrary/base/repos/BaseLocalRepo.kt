package ai.p2ach.p2achandroidlibrary.base.repos

import kotlinx.coroutines.flow.Flow

abstract class BaseLocalRepo<T> : BaseRepo<T>() {

    abstract fun localFlow(): Flow<T>
    abstract suspend fun saveLocal(data: T)
    abstract suspend fun clearLocal()
    override fun stream(): Flow<T> =localFlow()
}