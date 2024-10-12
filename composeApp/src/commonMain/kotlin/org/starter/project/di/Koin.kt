package org.starter.project.di

import org.koin.core.KoinApplication
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.starter.project.data.repository.ZennRepository
import org.starter.project.data.zenn.repository.ZennRepositoryImpl
import org.starter.project.domain.service.ResultHandler
import org.starter.project.domain.service.ZennService
import org.starter.project.domain.zenn.ZennServiceImpl
import org.starter.project.feature.home.HomeScreenViewModel

fun koin(): KoinApplication.() -> Unit = {
    val repositoryModule = module {
        singleOf<ZennRepository>(::ZennRepositoryImpl)
    }

    val serviceModule = module {
        single { ResultHandler() }
        single<ZennService> { ZennServiceImpl(get(), get()) }
    }

    val appModule = module {
        viewModelOf(::HomeScreenViewModel)
    }

    modules(repositoryModule, serviceModule, appModule)
}
