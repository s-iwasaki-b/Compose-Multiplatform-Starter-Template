plugins {
    id("kmp-library")
    alias(libs.plugins.mokkery)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.domain)

            api(projects.module.base)
            implementation(projects.module.data.repository)
            implementation(projects.module.domain.service)
        }
        commonTest.dependencies {
            implementation(libs.bundles.test)
        }
    }
}
