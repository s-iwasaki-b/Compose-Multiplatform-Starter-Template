package org.starter.project.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.starter.project.core.api.ApiClient
import org.starter.project.core.api.ApiClientImpl
import org.starter.project.core.preferences.PreferencesConfig
import org.starter.project.data.repository.ZennRepository
import org.starter.project.data.zenn.datasource.api.ZennApi
import org.starter.project.data.zenn.datasource.api.createZennApi
import org.starter.project.data.zenn.datasource.preferences.ZennPreferences
import org.starter.project.data.zenn.datasource.preferences.ZennPreferencesImpl
import org.starter.project.data.zenn.repository.ZennRepositoryImpl
import org.starter.project.domain.service.ResultHandler
import org.starter.project.domain.service.ZennService
import org.starter.project.domain.zenn.ZennServiceImpl
import org.starter.project.feature.home.HomeScreenViewModel

expect val platformModule: org.koin.core.module.Module

fun startKoin(platformDeclaration: KoinAppDeclaration? = null) {
    org.koin.core.context.startKoin {
        platformDeclaration?.invoke(this)

        val coreModule = module {
            single<ApiClient> { ApiClientImpl() }
            singleOf(::PreferencesConfig)
        }

        val dataSourceModule = module {
            single<ZennApi> { get<ApiClient>().ktorfit.createZennApi() }
            single<ZennPreferences> { ZennPreferencesImpl(get<PreferencesConfig>().zennPreferences) }
        }

        val repositoryModule = module {
            single<ZennRepository> { ZennRepositoryImpl(get(), get()) }
        }

        val serviceModule = module {
            single { ResultHandler() }
            single<ZennService> { ZennServiceImpl(get(), get()) }
        }

        val appModule = module {
            viewModelOf(::HomeScreenViewModel)
        }

        modules(
            platformModule,
            coreModule,
            dataSourceModule,
            repositoryModule,
            serviceModule,
            appModule
        )
    }
}
