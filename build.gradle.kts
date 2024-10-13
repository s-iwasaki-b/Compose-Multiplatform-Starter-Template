plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.serialization) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.ktorfit) apply false
    alias(libs.plugins.mokkery) apply false
}

tasks.register("changePackageName") {
    val oldPackageName = PACKAGE_NAME
    val newPackageName: String? = project.findProperty("newPackageName") as String?
    val sourceSets = listOf("androidMain", "commonMain", "iosMain", "commonTest")
    val fileExtensions = listOf("kt", "xml", "xcconfig")

    doLast {
        if (newPackageName.isNullOrEmpty()) {
            throw GradleException("Error: Please provide the new package name using -PnewPackageName=...")
        }

        println("Changing package name from $oldPackageName to $newPackageName")
        project.projectDir.walkTopDown().forEach { file ->
            if (file.isFile && fileExtensions.any { file.name.endsWith(it) }) {
                val fileText = file.readText()
                if (fileText.contains(oldPackageName)) {
                    val newText = fileText.replace(oldPackageName, newPackageName)
                    file.writeText(newText)
                    println("Updated package name in ${file.path}")
                }
            }
        }

        fun deleteEmptyParentDirs(dir: File) {
            var currentDir: File? = dir
            while (currentDir != null && currentDir != projectDir) {
                if (currentDir.listFiles()?.isEmpty() == true) {
                    currentDir.delete()
                    println("Deleted empty directory: $currentDir")
                    currentDir = currentDir.parentFile
                } else {
                    break
                }
            }
        }

        println("Changing package directory from $oldPackageName to $newPackageName")
        projectDir.walkTopDown().filter { it.isDirectory && it.name == "src" }.forEach { srcDir ->
            sourceSets.forEach { sourceSet ->
                val oldPackageDir = oldPackageName.replace(".", "/")
                val newPackageDir = newPackageName.replace(".", "/")
                val oldDirPath = srcDir.resolve("$sourceSet/kotlin/$oldPackageDir")
                val newDirPath = srcDir.resolve("$sourceSet/kotlin/$newPackageDir")

                if (oldDirPath.exists()) {
                    newDirPath.parentFile.mkdirs()
                    oldDirPath.copyRecursively(newDirPath, overwrite = true)
                    deleteEmptyParentDirs(oldDirPath)
                    println("Updated directory structure from $oldDirPath to $newDirPath")
                } else {
                    println("Not Found directory: $oldDirPath")
                }
            }
        }
    }
}
