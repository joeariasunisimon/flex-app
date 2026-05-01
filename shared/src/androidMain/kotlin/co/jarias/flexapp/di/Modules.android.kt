package co.jarias.flexapp.di

import android.content.Context
import co.jarias.flexapp.data.local.DatabaseDriverFactory
import co.jarias.flexapp.data.local.createDataStore
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DatabaseDriverFactory(get()) }
    single { createDataStore(get<Context>()) }
}
