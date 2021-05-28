package seng440.vaccinepassport.room

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class VPassRepository(private val vPassDao: VPassDao) {
    val vPasses: Flow<List<VPassData>> = vPassDao.getAll()
    val numVPasses: Flow<Int> = vPassDao.getCount()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(vPass: VPassData) {
        vPassDao.insert(vPass)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(vPass: VPassData) {
        vPassDao.delete(vPass)
    }

//    @Suppress("RedundantSuspendModifier")
//    @WorkerThread
//    suspend fun getAll() : Flow<List<VPassData>> {
//        return vPassDao.getAll()
//    }
}