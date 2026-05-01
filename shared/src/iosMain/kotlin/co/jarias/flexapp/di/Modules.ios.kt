package co.jarias.flexapp.di

import co.jarias.flexapp.data.local.DatabaseDriverFactory
import co.jarias.flexapp.data.local.createDataStore
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DatabaseDriverFactory() }
    single { createDataStore() }
}
