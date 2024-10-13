plugins {
    `kotlin-dsl`
}

repositories{
    mavenCentral()
}

tasks.register("changePackageName") {
    val oldPackageName = "org.starter.project"
    val newPackageName: String? = project.findProperty("newPackageName") as String?
    val fileExtensions = listOf("kt", "xml", "xcconfig")

    doLast {
        if (newPackageName == null) {
            println("Please provide the new package name using -PnewPackageName=...")
            return@doLast
        }

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
    }
}
