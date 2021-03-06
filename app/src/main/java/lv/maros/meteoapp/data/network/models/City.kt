package lv.maros.meteoapp.data.network.models

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

data class City(
    @PrimaryKey
    @ColumnInfo(name = "wiki_data_id")
    val wikiDataId: String,
    val name: String,
    val city: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val id: Int
)
