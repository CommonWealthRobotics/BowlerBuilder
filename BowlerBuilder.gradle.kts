import BowlerBuilder_gradle.Strings.spotlessLicenseHeaderDelimiter
import BowlerBuilder_gradle.Versions.bowlerBuilderVersion
import BowlerBuilder_gradle.Versions.ktlintVersion
import com.adarshr.gradle.testlogger.theme.ThemeType
import com.github.spotbugs.SpotBugsTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Paths
import java.util.Properties

plugins {
    jacoco
    pmd
    id("com.diffplug.gradle.spotless") version "3.16.0"
    id("org.jlleitschuh.gradle.ktlint") version "6.2.1"
    id("com.github.spotbugs") version "1.6.4"
    id("io.gitlab.arturbosch.detekt") version "1.0.0-RC12"
    id("com.adarshr.test-logger") version "1.6.0"
}

object Versions {
    const val bowlerBuilderVersion = "0.2.0"
    const val ktlintVersion = "0.29.0"
}

object Strings {
    const val spotlessLicenseHeaderDelimiter = "(@|package|import)"
}

allprojects {
    version = bowlerBuilderVersion
    group = "com.neuronrobotics"
}

val bowlerBuilderProject = project(":BowlerBuilder")
val bowlerBuilderKotlinUIProject = project(":BowlerBuilder:KotlinUI")

val kotlinProjects = setOf(
    bowlerBuilderProject,
    bowlerBuilderKotlinUIProject
)

val javaProjects = setOf<Project>() + kotlinProjects

val javafxProjects = setOf(
    bowlerBuilderProject,
    bowlerBuilderKotlinUIProject
)

buildscript {
    repositories {
        mavenCentral() // Needed for kotlin gradle plugin
        maven(url = "https://plugins.gradle.org/m2/")
    }
    dependencies {
        // Gives us the KotlinJvmProjectExtension
        classpath(kotlin("gradle-plugin", property("kotlin.version") as String))
    }
}

allprojects {
    apply {
        plugin("com.diffplug.gradle.spotless")
        plugin("com.adarshr.test-logger")
    }

    repositories {
        jcenter()
        mavenCentral()
        maven(url = "https://dl.bintray.com/commonwealthrobotics/maven-artifacts")
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

    testlogger {
        theme = ThemeType.STANDARD_PARALLEL
    }

    spotless {
        /*
         * We use spotless to lint the Gradle Kotlin DSL files that make up the build.
         * These checks are dependencies of the `check` task.
         */
        kotlinGradle {
            ktlint(ktlintVersion)
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
        plugin("com.github.spotbugs")
        plugin("pmd")
    }

    dependencies {
        fun junitJupiter(name: String, version: String = "5.4.0") =
            create(group = "org.junit.jupiter", name = name, version = version)

        fun testFx(name: String, version: String = "4.0.+") =
            create(group = "org.testfx", name = name, version = version)

        "testCompile"(junitJupiter(name = "junit-jupiter"))
        "testCompile"(testFx(name = "testfx-core", version = "4.0.7-alpha"))
        "testCompile"(testFx(name = "testfx-junit5", version = "4.0.6-alpha"))

        "testRuntime"(
            group = "org.junit.platform",
            name = "junit-platform-launcher",
            version = "1.0.0"
        )
        "testRuntime"(testFx(name = "openjfx-monocle", version = "8u76-b04"))
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.isIncremental = true
    }

    tasks.withType<Test> {
        @Suppress("UnstableApiUsage")
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
            events(
                TestLogEvent.FAILED,
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED,
                TestLogEvent.STARTED
            )
            displayGranularity = 0
            showExceptions = true
            showCauses = true
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
        }

        @Suppress("UnstableApiUsage")
        reports.junitXml.destination = file("${rootProject.buildDir}/test-results/${project.name}")
    }

    tasks.withType<JacocoReport> {
        @Suppress("UnstableApiUsage")
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
            @Suppress("INACCESSIBLE_TYPE")
            licenseHeaderFile(
                "${rootProject.rootDir}/config/spotless/bowlerbuilder.license",
                spotlessLicenseHeaderDelimiter
            )
        }
    }

    checkstyle {
        toolVersion = "8.1"
    }

    spotbugs {
        toolVersion = "3.1.3"
        excludeFilter = file("${rootProject.rootDir}/config/spotbugs/spotbugs-excludeFilter.xml")
    }

    tasks.withType<SpotBugsTask> {
        @Suppress("UnstableApiUsage")
        reports {
            xml.isEnabled = false
            emacs.isEnabled = false
            html.isEnabled = true
        }
    }

    pmd {
        toolVersion = "6.3.0"
        ruleSets = emptyList() // Needed so PMD only uses our custom ruleset
        ruleSetFiles = files("${rootProject.rootDir}/config/pmd/pmd-ruleset.xml")
    }
}

configure(kotlinProjects) {
    val kotlinVersion = property("kotlin.version") as String

    apply {
        plugin("kotlin")
        plugin("io.gitlab.arturbosch.detekt")
    }

    repositories {
        maven { url = uri("https://dl.bintray.com/kotlin/ktor") }
        maven { url = uri("https://dl.bintray.com/kotlin/kotlinx") }
    }

    dependencies {
        // Weird syntax, see: https://github.com/gradle/kotlin-dsl/issues/894
        "compile"(kotlin("stdlib-jdk8", kotlinVersion))
        "compile"(kotlin("reflect", kotlinVersion))
        "compile"(kotlin("script-runtime", kotlinVersion))
        "compile"(
            group = "org.jetbrains.kotlinx",
            name = "kotlinx-coroutines-core",
            version = "1.0.0"
        )
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjvm-default=enable", "-progressive")
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
                        .artifact(
                            mapOf(
                                "file" to compileKotlin.destinationDir,
                                "type" to "java-classes-directory",
                                "builtBy" to compileKotlin
                            )
                        )
                }
            }
        }
    }

    spotless {
        kotlin {
            trimTrailingWhitespace()
            indentWithSpaces(2)
            endWithNewline()
            @Suppress("INACCESSIBLE_TYPE")
            licenseHeaderFile(
                "${rootProject.rootDir}/config/spotless/bowlerbuilder.license",
                spotlessLicenseHeaderDelimiter
            )
        }
    }

    detekt {
        toolVersion = "1.0.0-RC12"
        input = files(
            "src/main/kotlin",
            "src/test/kotlin"
        )
        parallel = true
        config = files("${rootProject.rootDir}/config/detekt/config.yml")
    }
}

configure(kotlinProjects.intersect(javafxProjects)) {
    dependencies {
        "compile"(
            group = "org.jetbrains.kotlinx",
            name = "kotlinx-coroutines-javafx",
            version = "1.0.0"
        )
        "compile"(group = "no.tornado", name = "tornadofx", version = "1.7.18")
    }
}

configure(javaProjects + kotlinProjects) {
    @Suppress("UnstableApiUsage")
    val createPropertiesTask = tasks.register("createProperties") {
        dependsOn("processResources")
        doLast {
            val propFileDir = Paths.get(
                buildDir.path,
                "resources",
                "main"
            ).toFile().apply {
                mkdirs()
            }

            val propFile = Paths.get(
                propFileDir.path,
                "version.properties"
            ).toFile()

            val prop = Properties()
            prop["version"] = version as String
            prop.store(propFile.outputStream(), null)
        }
    }

    tasks.named("classes") {
        dependsOn(createPropertiesTask)
    }
}

tasks.wrapper {
    gradleVersion = "5.4.1"
    distributionType = Wrapper.DistributionType.ALL
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

/**
 * Configures the [detekt][io.gitlab.arturbosch.detekt.extensions.DetektExtension] extension.
 */
fun org.gradle.api.Project.`detekt`(configure: io.gitlab.arturbosch.detekt.extensions.DetektExtension.() -> Unit): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("detekt", configure)
