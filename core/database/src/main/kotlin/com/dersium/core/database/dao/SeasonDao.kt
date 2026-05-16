package com.dersium.core.database.dao

import androidx.room.*
import com.dersium.core.database.entity.SeasonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SeasonDao {

    @Query("SELECT * FROM seasons ORDER BY startYear DESC")
    fun getAllSeasons(): Flow<List<SeasonEntity>>

    @Query("SELECT * FROM seasons WHERE isActive = 1 LIMIT 1")
    fun getActiveSeason(): Flow<SeasonEntity?>

    @Query("SELECT * FROM seasons WHERE id = :id LIMIT 1")
    suspend fun getSeasonById(id: Long): SeasonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeason(season: SeasonEntity): Long

    @Update
    suspend fun updateSeason(season: SeasonEntity)

    @Query("UPDATE seasons SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE seasons SET isActive = 1 WHERE id = :id")
    suspend fun setActive(id: Long)
}
