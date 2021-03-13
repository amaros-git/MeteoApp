package lv.maros.meteoapp.hiltmodules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lv.maros.meteoapp.data.cities.CitiesRepository
import lv.maros.meteoapp.data.cities.local.CitiesDatabase
import lv.maros.meteoapp.data.cities.network.CitiesApi

@Module
@InstallIn(SingletonComponent::class)
class CitiesRepositoryModule {

    @Provides
    fun provideCitiesRepository(
        @ApplicationContext appContext: Context
    ) = CitiesRepository(CitiesApi, CitiesDatabase.getInstance(appContext))
}