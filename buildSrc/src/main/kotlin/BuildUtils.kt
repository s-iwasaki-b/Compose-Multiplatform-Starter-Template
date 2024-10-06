import java.io.File

const val PACKAGE_NAME = "org.starter.project"

fun allSubProjects(rootDir: File, action: (String) -> Unit) {
    rootDir.resolve("module").walk().maxDepth(3).filter {
        it.isDirectory && it.resolve("build.gradle.kts").exists()
    }.forEach {
        val path = it.relativeTo(rootDir).path.replace(File.separator, ":")
        action(path)
    }
}
