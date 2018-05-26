buildCache {
    local(DirectoryBuildCache::class.java) {
        isEnabled = true
        directory = file("${rootDir.path}/build-cache")
    }
}

rootProject.name = "Bowler"

include(":BowlerScriptKernel")
include(":BowlerBuilder")

/**
 * This configures the gradle build so we can use non-standard build file names.
 * Additionally, this project can support sub-projects who's build file is written in Kotlin.
 *
 * @param project The project to configure.
 */
fun configureGradleBuild(project: ProjectDescriptor) {
    val projectBuildFileBaseName = project.name
    assert(projectBuildFileBaseName[0].isUpperCase()) { "Project must begin with upper case letter" }

    val gradleBuild = File(project.projectDir, "$projectBuildFileBaseName.gradle")
    val kotlinBuild = File(project.projectDir, "$projectBuildFileBaseName.gradle.kts")
    assert(!(gradleBuild.exists() && kotlinBuild.exists())) {
        "Project ${project.name} can not have both a ${gradleBuild.name} and a ${kotlinBuild.name} file. " +
                "Rename one so that the other can serve as the base for the project's build"
    }
    project.buildFileName = when {
        gradleBuild.exists() -> gradleBuild.name
        kotlinBuild.exists() -> kotlinBuild.name
        else -> throw AssertionError("Project `${project.name}` must have a either a file " +
                "containing ${gradleBuild.name} or ${kotlinBuild.name}")
    }

    // Any nested children projects also get configured.
    project.children.forEach { configureGradleBuild(it) }
}

configureGradleBuild(rootProject)
