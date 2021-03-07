package lv.maros.meteoapp.data.network.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city_table")
data class City(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "wiki_data_id")
    val wikiDataId: String,
    val name: String,
    val city: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,

)
