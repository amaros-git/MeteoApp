package lv.maros.meteoapp.data.network.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "region_table")
data class Region(
    @ColumnInfo(name = "country_code") val countryCode: String,
    @ColumnInfo(name = "fips_code") val fipsCode: String?,
    @ColumnInfo(name = "iso_code") val isoCode: String,
    @ColumnInfo(name = "name") val name: String,
    @PrimaryKey val wikiDataId: String
)
