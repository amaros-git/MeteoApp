package lv.maros.meteoapp.hiltmodules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lv.maros.meteoapp.utils.MyLocationService

@Module
@InstallIn(SingletonComponent::class)
class MyLocationServiceModule {

    @Provides
    fun provideCitiesRepository(@ApplicationContext appContext: Context) =
        MyLocationService(appContext)
}