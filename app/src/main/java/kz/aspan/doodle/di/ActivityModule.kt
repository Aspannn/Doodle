package kz.aspan.doodle.di

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kz.aspan.doodle.data.remote.api.SetupApi
import kz.aspan.doodle.data.remote.ws.CustomGsonMessageAdapter
import kz.aspan.doodle.data.remote.ws.DrawingApi
import kz.aspan.doodle.data.remote.ws.FlowStreamAdapter
import kz.aspan.doodle.repository.DefaultSetupRepository
import kz.aspan.doodle.repository.SetupRepository
import kz.aspan.doodle.util.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@ExperimentalCoroutinesApi
@Module
@InstallIn(ActivityRetainedComponent::class)
object ActivityModule {

    @ActivityRetainedScoped
    @Provides
    fun provideSetupRepository(
        setupApi: SetupApi,
        @ApplicationContext context: Context
    ): SetupRepository = DefaultSetupRepository(setupApi, context)


    @ActivityRetainedScoped
    @Provides
    fun provideDrawingApi(
        app: Application,
        okkHttpClient: OkHttpClient,
        gson: Gson
    ): DrawingApi {
        return Scarlet.Builder()
            .backoffStrategy(LinearBackoffStrategy(Constants.RECONNECT_INTERVAL))
            .lifecycle(AndroidLifecycle.ofApplicationForeground(app))
            .webSocketFactory(
                okkHttpClient.newWebSocketFactory(
                    if (Constants.USE_LOCALHOST) Constants.WS_BASE_URL_LOCALHOST else Constants.WS_BASE_URL
                )
            )
            .addStreamAdapterFactory(FlowStreamAdapter.Factory)
            .addMessageAdapterFactory(CustomGsonMessageAdapter.Factory(gson))
            .build()
            .create()
    }

    @ActivityRetainedScoped
    @Provides
    fun provideSetupApi(okHttpClient: OkHttpClient): SetupApi {
        return Retrofit.Builder()
            .baseUrl(if (Constants.USE_LOCALHOST) Constants.HTTP_BASE_URL_LOCALHOST else Constants.HTTP_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(SetupApi::class.java)
    }
}