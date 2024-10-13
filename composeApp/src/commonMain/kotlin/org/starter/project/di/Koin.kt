package org.starter.project.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.starter.project.core.preferences.PreferencesConfig
import org.starter.project.data.repository.ZennRepository
import org.starter.project.data.zenn.datasource.preferences.ZennPreferences
import org.starter.project.data.zenn.repository.ZennRepositoryImpl
import org.starter.project.domain.service.ResultHandler
import org.starter.project.domain.service.ZennService
import org.starter.project.domain.zenn.ZennServiceImpl
import org.starter.project.feature.home.HomeScreenViewModel

expect val platformModule: org.koin.core.module.Module

fun startKoin(platformDeclaration: KoinAppDeclaration? = null) {
    org.koin.core.context.startKoin {
        platformDeclaration?.invoke(this)

        val dataSourceModule = module {
            single { PreferencesConfig(get()) }
            single { ZennPreferences(get<PreferencesConfig>().zennPreferences) }
        }

        val repositoryModule = module {
            single<ZennRepository> { ZennRepositoryImpl(zennPreferences = get()) }
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
            dataSourceModule,
            repositoryModule,
            serviceModule,
            appModule
        )
    }
}
