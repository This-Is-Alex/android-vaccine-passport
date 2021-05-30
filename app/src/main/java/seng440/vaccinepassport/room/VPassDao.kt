package seng440.vaccinepassport.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VPassDao {

    @Insert
    suspend fun insert(vPass: VPassData): Long

    @Update
    suspend fun update(vPass: VPassData)

    @Delete
    suspend fun delete(vPass: VPassData)

    @Query("SELECT * FROM vPass ORDER BY date DESC")
    fun getAll(): Flow<List<VPassData>>

    @Query("SELECT COUNT(*) FROM vPass")
    fun getCount(): Flow<Int>
}