package com.example.reviews.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingSource

@Dao
interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(review: ReviewEntity): Long

    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    fun pagingAll(): PagingSource<Int, ReviewEntity>

    @Query("DELETE FROM reviews WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM reviews")
    suspend fun clear()
}
