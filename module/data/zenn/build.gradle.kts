plugins {
    id("kmp-library")
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.mokkery)
}

ktorfit {
    compilerPluginVersion.set("2.3.3")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.data)

            api(projects.module.base)
            implementation(projects.module.core)
            implementation(projects.module.data.repository)
        }
        commonTest.dependencies {
            implementation(libs.bundles.test)
        }
    }
}
