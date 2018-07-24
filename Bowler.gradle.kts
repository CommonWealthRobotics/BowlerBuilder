import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.util.GFileUtils

plugins {
    jacoco
    pmd
    id("com.diffplug.gradle.spotless") version "3.10.0"
}

allprojects {
    version = "0.0.0"
    group = "com.neuronrobotics"
}

// val bowlerScriptKernelProject = project(":bowler-script-kernel")
val bowlerBuilderProject = project(":BowlerBuilder")
val bowlerBuilderJavaUIProject = project(":BowlerBuilder:JavaUI")
val bowlerBuilderCoreProject = project(":BowlerBuilder:Core")

val kotlinProjects = setOf(
        bowlerBuilderProject,
        bowlerBuilderJavaUIProject,
        bowlerBuilderCoreProject
)

val javaProjects = setOf<Project>(
//        bowlerScriptKernelProject
) + kotlinProjects

val javafxProjects = setOf(
        bowlerBuilderProject,
        bowlerBuilderJavaUIProject
)

// /////////////////////////////////////////////////////////////////////////////////////////
// https://github.com/CommonWealthRobotics/BowlerBuilder/blob/robot-cad/build.gradle.kts //
// /////////////////////////////////////////////////////////////////////////////////////////

buildscript {
    repositories {
        mavenCentral() // Needed for kotlin gradle plugin
    }
    dependencies {
        // Gives us the KotlinJvmProjectExtension
        classpath(kotlin("gradle-plugin", property("kotlin.version") as String))
    }
}

allprojects {
    apply {
        plugin("com.diffplug.gradle.spotless")
    }

    // Configures the Jacoco tool version to be the same for all projects that have it applied.
    pluginManager.withPlugin("jacoco") {
        // If this project has the plugin applied, configure the tool version.
        jacoco {
            toolVersion = "0.8.0"
        }
    }

    tasks.withType<Test> {
        testLogging {
            showStandardStreams = true
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    spotless {
        /*
         * We use spotless to lint the Gradle Kotlin DSL files that make up the build.
         * These checks are dependencies of the `check` task.
         */
        kotlinGradle {
            ktlint("0.23.1")
            trimTrailingWhitespace()
        }
        freshmark {
            trimTrailingWhitespace()
            indentWithSpaces(2)
            endWithNewline()
        }
        format("extraneous") {
            target("src/**/*.fxml")
            trimTrailingWhitespace()
            indentWithSpaces(2)
            endWithNewline()
        }
    }
}

configure(javaProjects) {
    apply {
        plugin("java")
        plugin("jacoco")
        plugin("checkstyle")
        plugin("findbugs")
        plugin("pmd")
    }

    repositories {
        jcenter()
        mavenCentral()
    }

    dependencies {
        fun junitJupiter(name: String, version: String = "5.2.0") =
                create(group = "org.junit.jupiter", name = name, version = version)

        fun testFx(name: String, version: String = "4.0.+") =
                create(group = "org.testfx", name = name, version = version)

        "testCompile"(junitJupiter(name = "junit-jupiter-api"))
        "testCompile"(junitJupiter(name = "junit-jupiter-engine"))
        "testCompile"(junitJupiter(name = "junit-jupiter-params"))
        "testCompile"(testFx(name = "testfx-core", version = "4.0.7-alpha"))
        "testCompile"(testFx(name = "testfx-junit5", version = "4.0.6-alpha"))
        "testCompile"(group = "com.google.guava", name = "guava-testlib", version = "23.0")
        "testCompile"(group = "org.mockito", name = "mockito-core", version = "2.12.0")

        "testRuntime"(group = "org.junit.platform", name = "junit-platform-launcher", version = "1.0.0")
        "testRuntime"(testFx(name = "openjfx-monocle", version = "8u76-b04"))
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.isIncremental = true
    }

    tasks.withType<Test> {
        extensions.configure(typeOf<JacocoTaskExtension>()) {
            /*
             * Fix for Jacoco breaking Build Cache support.
             * https://github.com/gradle/gradle/issues/5269
             */
            isAppend = false
        }

        useJUnitPlatform {
            filter {
                includeTestsMatching("*Test")
                includeTestsMatching("*Tests")
                includeTestsMatching("*Spec")
            }

            /*
             * Performance tests are only really run during development.
             * They don't need to run in CI or as part of regular development.
             */
            excludeTags("performance")

            /*
             * Marking a test as `slow` will excluded it from being run as part of the regular CI system.
             */
            excludeTags("slow")
        }

        if (project.hasProperty("jenkinsBuild") || project.hasProperty("headless")) {
            jvmArgs = listOf(
                    "-Djava.awt.headless=true",
                    "-Dtestfx.robot=glass",
                    "-Dtestfx.headless=true",
                    "-Dprism.order=sw",
                    "-Dprism.text=t2k"
            )
        }

        testLogging {
            events(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.STARTED)
            displayGranularity = 0
            showExceptions = true
            showCauses = true
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
        }

        reports.junitXml.destination = file("${rootProject.buildDir}/test-results/${project.name}")
    }

    tasks.withType<JacocoReport> {
        reports {
            html.isEnabled = true
            xml.isEnabled = true
            csv.isEnabled = false
        }
    }

    spotless {
        java {
            googleJavaFormat()
            removeUnusedImports()
            trimTrailingWhitespace()
            indentWithSpaces(2)
            endWithNewline()
        }
    }

    checkstyle {
        toolVersion = "8.1"
    }

    findbugs {
        toolVersion = "3.0.1"
        excludeFilter = file("${rootProject.rootDir}/config/findbugs/findbugs-excludeFilter.xml")
    }

    tasks.withType<FindBugs> {
        // Configure the FindBugs task to output in emacs mode (human readable format).
        reports {
            xml.isEnabled = false
            emacs.isEnabled = true
        }

        finalizedBy(task("${name}Report") {
            description = "Reports the FindBugs task output to the warn logger."
            mustRunAfter(this@withType)
            doLast {
                /*
                 * If an error is generated by the FindBugs task, then display it to the console.
                 */
                this@withType
                        .reports
                        .emacs
                        .destination
                        .takeIf { it.exists() }
                        ?.readText()
                        .takeIf { !it.isNullOrBlank() }
                        ?.also { logger.warn(it) }
            }
        })
    }

    pmd {
        toolVersion = "6.3.0"
        ruleSets = emptyList() // Needed so PMD only uses our custom ruleset
        ruleSetFiles = files("${rootProject.rootDir}/config/pmd/pmd-ruleset.xml")
    }
}

configure(kotlinProjects) {
    val kotlinVersion = "1.2.41"

    apply {
        plugin("kotlin")
    }

    dependencies {
        // Weird syntax, see: https://github.com/gradle/kotlin-dsl/issues/894
        "compile"(kotlin("stdlib-jdk8", kotlinVersion))
        "compile"(kotlin("reflect"))
        "compile"(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "0.22.5")

        "testCompile"(kotlin("test"))
        "testCompile"(kotlin("test-junit"))
    }

    kotlin {
        // Enable coroutines supports for Kotlin.
        experimental.coroutines = Coroutines.ENABLE
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjvm-default=enable")
        }
    }

    val compileKotlin: KotlinCompile by tasks
    afterEvaluate {
        /*
         * Needed to configure kotlin to work correctly with the "java-library" plugin.
         * See:
         * https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_known_issues
         */
        pluginManager.withPlugin("java-library") {
            configurations {
                "apiElements" {
                    outgoing
                            .variants
                            .getByName("classes")
                            .artifact(mapOf(
                                    "file" to compileKotlin.destinationDir,
                                    "type" to "java-classes-directory",
                                    "builtBy" to compileKotlin
                            ))
                }
            }
        }
    }

    spotless {
        kotlin {
            ktlint("0.23.1")
            trimTrailingWhitespace()
            indentWithSpaces(2)
            endWithNewline()
        }
    }
}

configure(kotlinProjects.intersect(javafxProjects)) {
    dependencies {
        "compile"(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-javafx", version = "0.22.5")
    }
}

val jacocoTestResultTaskName = "jacocoTestReport"

val jacocoRootReport = task<JacocoReport>("jacocoRootReport") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Generates code coverage report for all sub-projects."

    val jacocoReportTasks =
            javaProjects
                    .filter {
                        // Filter out source sets that don't have tests in them
                        // Otherwise, Jacoco tries to generate coverage data for tests that don't exist
                        !it.java.sourceSets["test"].allSource.isEmpty
                    }
                    .map { it.tasks[jacocoTestResultTaskName] as JacocoReport }
    dependsOn(jacocoReportTasks)

    val allExecutionData = jacocoReportTasks.map { it.executionData }
    executionData(*allExecutionData.toTypedArray())

    // Pre-initialize these to empty collections to prevent NPE on += call below.
    additionalSourceDirs = files()
    sourceDirectories = files()
    classDirectories = files()

    javaProjects.forEach { testedProject ->
        val sourceSets = testedProject.java.sourceSets
        this@task.additionalSourceDirs = this@task.additionalSourceDirs?.plus(files(sourceSets["main"].allSource.srcDirs))
        this@task.sourceDirectories += files(sourceSets["main"].allSource.srcDirs)
        this@task.classDirectories += files(sourceSets["main"].output)
    }

    reports {
        html.isEnabled = true
        xml.isEnabled = true
        csv.isEnabled = false
    }
}

val checkTask = tasks.maybeCreate("check", Task::class.java).apply {
    description = "Check all sub-projects"
    group = LifecycleBasePlugin.VERIFICATION_GROUP

//    dependsOn(jacocoRootReport)
}

val buildTask = tasks.maybeCreate("build", Task::class.java).apply {
    description = "Build all sub-projects"
    group = LifecycleBasePlugin.BUILD_GROUP
    dependsOn(checkTask)
}

configure(javaProjects + kotlinProjects) {
    checkTask.dependsOn(tasks.getByName("check"))
    buildTask.dependsOn(tasks.getByName("build"))
}

task<Wrapper>("wrapper") {
    gradleVersion = "4.9"
    distributionType = Wrapper.DistributionType.ALL

    doLast {
        /*
         * Copy the properties file into the buildSrc project.
         * Related issues:
         *
         * https://youtrack.jetbrains.com/issue/KT-14895
         * https://youtrack.jetbrains.com/issue/IDEA-169717
         * https://youtrack.jetbrains.com/issue/IDEA-153336
         */
        val buildSrcWrapperDir = File(rootDir, "buildSrc/gradle/wrapper")
        GFileUtils.mkdirs(buildSrcWrapperDir)
        copy {
            from(propertiesFile)
            into(buildSrcWrapperDir)
        }
    }
}

/**
 * Configures the [publishing][org.gradle.api.publish.PublishingExtension] project extension.
 */
fun Project.`publishing`(configure: org.gradle.api.publish.PublishingExtension.() -> Unit) =
        extensions.configure("publishing", configure)

/**
 * Configures the [checkstyle][org.gradle.api.plugins.quality.CheckstyleExtension] project extension.
 */
fun Project.`checkstyle`(configure: org.gradle.api.plugins.quality.CheckstyleExtension.() -> Unit) =
        extensions.configure("checkstyle", configure)

/**
 * Configures the [findbugs][org.gradle.api.plugins.quality.FindBugsExtension] project extension.
 */
fun Project.`findbugs`(configure: org.gradle.api.plugins.quality.FindBugsExtension.() -> Unit) =
        extensions.configure("findbugs", configure)

/**
 * Retrieves the [java][org.gradle.api.plugins.JavaPluginConvention] project convention.
 */
val Project.`java`: org.gradle.api.plugins.JavaPluginConvention
    get() = convention.getPluginByName("java")

/**
 * Configures the [kotlin][org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension] project extension.
 */
fun Project.`kotlin`(configure: org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension.() -> Unit): Unit =
        extensions.configure("kotlin", configure)
