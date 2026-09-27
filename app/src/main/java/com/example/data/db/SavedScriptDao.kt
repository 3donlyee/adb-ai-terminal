package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedScriptDao {
    @Query("SELECT * FROM saved_scripts ORDER BY id ASC")
    fun getAllScripts(): Flow<List<SavedScriptEntity>>

    @Query("SELECT * FROM saved_scripts WHERE category = :category ORDER BY id ASC")
    fun getScriptsByCategory(category: String): Flow<List<SavedScriptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(script: SavedScriptEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(scripts: List<SavedScriptEntity>)

    @Update
    suspend fun update(script: SavedScriptEntity)

    @Delete
    suspend fun delete(script: SavedScriptEntity)

    @Query("DELETE FROM saved_scripts WHERE isBuiltIn = 0")
    suspend fun clearCustom()
}
