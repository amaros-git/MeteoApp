package lv.maros.meteoapp.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import lv.maros.meteoapp.data.network.models.City
import lv.maros.meteoapp.data.network.models.Region

@Dao
interface CitiesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegion(region: Region)

    @Query("SELECT * FROM region_table")
    suspend fun getRegions(): List<Region>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCity(city: City)

    @Query("SELECT * FROM city_table ORDER BY name ASC ")
    suspend fun getCities(): List<City>

    @Query("SELECT * FROM city_table WHERE name = :cityName")
    suspend fun getCityByName(cityName: String): City?

   /* @Query("SELECT * FROM election_table")
    fun observeElections(): LiveData<List<Election>>


    @Update(entity = Election::class)
    suspend fun updateElection(electionUpdate: ElectionUpdate)

    @Query("UPDATE election_table SET isFollowed = :shouldFollow WHERE id = :electionId")
    suspend fun changeFollowingStatus(electionId: Int, shouldFollow: Boolean)

    @Query("SELECT * FROM election_table WHERE id = :electionId")
    suspend fun getElection(electionId: Int): Election?*/

}