package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.QuickDialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuickDialDao {
    @Query("SELECT * FROM quick_dials ORDER BY orderIndex ASC")
    fun getAllQuickDials(): Flow<List<QuickDialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuickDial(item: QuickDialEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<QuickDialEntity>)

    @Delete
    suspend fun deleteQuickDial(item: QuickDialEntity)

    @Query("DELETE FROM quick_dials WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM quick_dials")
    suspend fun getCount(): Int
}
