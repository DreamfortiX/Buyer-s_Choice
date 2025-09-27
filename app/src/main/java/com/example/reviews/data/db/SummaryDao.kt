package com.example.reviews.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SummaryDao {
    @Query("SELECT * FROM summaries WHERE productId = :productId LIMIT 1")
    suspend fun get(productId: String): SummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SummaryEntity)

    @Query("DELETE FROM summaries WHERE productId = :productId")
    suspend fun delete(productId: String)
}
