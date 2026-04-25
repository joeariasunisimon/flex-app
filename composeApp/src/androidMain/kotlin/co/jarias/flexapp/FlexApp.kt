package co.jarias.flexapp

import android.app.Application
import co.jarias.flexapp.di.appModule
import co.jarias.flexapp.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class FlexApp : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()
            androidContext(this@FlexApp)
            modules(appModule)
        }
    }
}
