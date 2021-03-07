package lv.maros.meteoapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import lv.maros.meteoapp.data.network.models.City
import lv.maros.meteoapp.data.network.models.Region

@Database(entities = [Region::class, City::class], version = 2, exportSchema = false)
abstract class CitiesDatabase: RoomDatabase() {

    abstract val citiesDao: CitiesDao

    companion object {

        @Volatile
        private var INSTANCE: CitiesDatabase? = null

        fun getInstance(context: Context): CitiesDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            CitiesDatabase::class.java,
                            "cities_database"
                    )
                            .fallbackToDestructiveMigration()
                            .build()

                    INSTANCE = instance
                }

                return instance
            }
        }

    }

}