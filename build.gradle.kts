import org.gradle.api.Project
import org.gradle.api.plugins.quality.FindBugs
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.testing.jacoco.tasks.JacocoReport
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.0.0")
    }
}

plugins {
    application
    `maven-publish`
    jacoco
    java
    checkstyle
    pmd
    maven
    id("com.github.johnrengelman.shadow") version "2.0.1"
    id("com.diffplug.gradle.spotless") version "3.5.1"
}

apply {
    plugin("pmd")
    plugin("findbugs")
    plugin("jacoco")
    plugin("org.junit.platform.gradle.plugin")
}

group = "com.neuronrobotics.bowlerbuilder"

// Spotless is used to lint and reformat source files.
spotless {
    kotlinGradle {
        // Configure the formatting of the Gradle Kotlin DSL files (*.gradle.kts)
        ktlint("0.9.1")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
    freshmark {
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
    format("extraneous") {
        target("src/**/*.fxml")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
}

repositories {
    mavenCentral()
    jcenter()
//    maven { setUrl("https://repository-bubblecloud.forge.cloudbees.com/release/") }
//    maven { setUrl("https://clojars.org/repo") }
//    maven { setUrl("https://oss.sonatype.org/content/repositories/releases/")  }
//    maven { setUrl("https://jline.sourceforge.net/m2repo") }
//    maven { setUrl("https://repo.spring.io/milestone") }
//    maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots/")  }
//    maven { setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")  }
//    maven { setUrl("https://jenkinsci.artifactoryonline.com/jenkinsci/public/") }
//    maven { setUrl("https://plugins.gradle.org/m2/") }
//    maven { setUrl("https://dl.bintray.com/clearcontrol/ClearControl") }
//    maven { setUrl("https://jitpack.io") }
//    maven { setUrl("http://maven-eclipse.github.io/maven") }
}

dependencies {
    compile(group = "org.controlsfx", name = "controlsfx", version = "8.40.14")
    compile(group = "com.google.guava", name = "guava", version = "23.6-jre")
    compile(group = "com.google.inject", name = "guice", version = "4.1.0")
    compile(group = "com.google.inject.extensions", name = "guice-assistedinject", version = "4.1.0")
    compile(group = "org.apache.commons", name = "commons-text", version = "1.2")
    compile(group = "commons-io", name = "commons-io", version = "2.6")
    compile(group = "commons-validator", name = "commons-validator", version = "1.6")
    compile(group = "com.google.code.findbugs", name = "annotations", version = "3.0.1")
    compile(group = "io.reactivex.rxjava2", name = "rxjava",  version = "2.1.9")
//    compile(group = "eu.mihosoft.vrl.jcsg", name = "jcsg", version = "0.5.6")
//    compile(group = "com.neuronrobotics", name = "BowlerScriptingKernel", version = "0.28.0")
//    compile(group = "org.kohsuke", name = "github-api", version = "1.90")
//    compile(group = "com.neuronrobotics", name = "JavaCad", version = "0.11.0")
//    compile(group = "org.bubblecloud.jbullet", name = "jbullet", version = "2.72.2.4") //down
//    compile 'org.bubblecloud.jbullet:jbullet:2.72.2.4'
//    compile "com.neuronrobotics:JavaCad:0.11.0"
//    compile(project(":lib/src/jbullet"))
//    compile(files("jars/BowlerScriptingKernel-0.31.0.jar"))
    compile(group = "com.neuronrobotics", name = "BowlerScriptingKernel", version="0.31.3")

    fun junitJupiter(name: String, version: String = "5.0.0") =
            create(group = "org.junit.jupiter", name = name, version = version)
    fun testFx(name: String, version: String = "4.0.+") =
            create(group = "org.testfx", name = name, version = version)

    testCompile(junitJupiter(name = "junit-jupiter-api"))
    testCompile(junitJupiter(name = "junit-jupiter-engine"))
    testCompile(junitJupiter(name = "junit-jupiter-params"))
    testCompile(testFx(name = "testfx-core", version = "4.0.7-alpha"))
    testCompile(testFx(name = "testfx-junit5", version = "4.0.6-alpha"))
    testCompile(group = "com.google.guava", name = "guava-testlib", version = "23.0")
    testCompile(group = "org.mockito", name = "mockito-core", version = "2.12.0")

    testRuntime(testFx(name = "openjfx-monocle", version = "8u76-b04"))
    testRuntime(group = "org.junit.platform", name = "junit-platform-launcher", version = "1.0.0")

}

//test {
//    testLogging {
//        exceptionFormat = 'full'
//    }
//}

application {
    mainClassName = "com.neuronrobotics.bowlerbuilder.BowlerBuilder"
}

checkstyle {
    configFile = file("$rootDir/checkstyle.xml")
    toolVersion = "8.1"
}

pmd {
    isConsoleOutput = true
    sourceSets = setOf(java.sourceSets["main"], java.sourceSets["test"])
    reportsDir = file("${project.buildDir}/reports/pmd")
    ruleSetFiles = files(file("$rootDir/pmd-ruleset.xml"))
    ruleSets = emptyList()
}

findbugs {
    sourceSets = setOf(java.sourceSets["main"], java.sourceSets["test"])
    effort = "max"
}

tasks.withType<FindBugs> {
    reports {
        xml.isEnabled = false
        emacs.isEnabled = true
    }
    finalizedBy(task("${name}Report") {
        mustRunAfter(this@withType)
        doLast {
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

tasks.withType<JacocoReport> {
    reports {
        xml.isEnabled = true
        html.isEnabled = true
    }
}
afterEvaluate {
    val junitPlatformTest : JavaExec by tasks
    jacoco {
        applyTo(junitPlatformTest)
    }
    task<JacocoReport>("jacocoJunit5TestReport") {
        executionData(junitPlatformTest)
        sourceSets(java.sourceSets["main"])
        sourceDirectories = files(java.sourceSets["main"].allSource.srcDirs)
        classDirectories = files(java.sourceSets["main"].output)
    }
}

/*
 * Allows you to run the UI tests in headless mode by calling gradle with the -Pheadless argument
 */
if (project.hasProperty("jenkinsBuild") || project.hasProperty("headless")) {
    println("Running UI Tests Headless")
    junitPlatform {
        filters {
            tags {
                /*
                 * A category for UI tests that cannot run in headless mode, ie work properly with real windows
                 * but not with the virtualized ones in headless mode.
                 */
                exclude("NonHeadlessTests")
            }
        }
    }
    tasks {
        "junitPlatformTest"(JavaExec::class) {
            jvmArgs = listOf(
                    "-Djava.awt.headless=true",
                    "-Dtestfx.robot=glass",
                    "-Dtestfx.headless=true",
                    "-Dprism.order=sw",
                    "-Dprism.text=t2k"
            )
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("BowlerBuilder") {
            artifactId = "BowlerBuilder"
            version = "0.0.1"
            val shadowJar: ShadowJar by tasks
            artifact (shadowJar) {
                classifier = null
            }
        }
    }
}

task<Wrapper>("wrapper") {
    gradleVersion = "4.1"
}

/**
 * Retrieves the [java][org.gradle.api.plugins.JavaPluginConvention] project convention.
 */
val Project.`java`: org.gradle.api.plugins.JavaPluginConvention get() =
    convention.getPluginByName("java")

/**
 * Retrieves the [checkstyle][org.gradle.api.plugins.quality.CheckstyleExtension] project extension.
 */
val Project.`checkstyle`: org.gradle.api.plugins.quality.CheckstyleExtension get() =
    extensions.getByName("checkstyle") as org.gradle.api.plugins.quality.CheckstyleExtension

/**
 * Configures the [checkstyle][org.gradle.api.plugins.quality.CheckstyleExtension] project extension.
 */
fun Project.`checkstyle`(configure: org.gradle.api.plugins.quality.CheckstyleExtension.() -> Unit) =
        extensions.configure("checkstyle", configure)

/**
 * Retrieves the [pmd][org.gradle.api.plugins.quality.PmdExtension] project extension.
 */
val Project.`pmd`: org.gradle.api.plugins.quality.PmdExtension get() =
    extensions.getByName("pmd") as org.gradle.api.plugins.quality.PmdExtension

/**
 * Configures the [pmd][org.gradle.api.plugins.quality.PmdExtension] project extension.
 */
fun Project.`pmd`(configure: org.gradle.api.plugins.quality.PmdExtension.() -> Unit) =
        extensions.configure("pmd", configure)

/**
 * Retrieves the [findbugs][org.gradle.api.plugins.quality.FindBugsExtension] project extension.
 */
val Project.`findbugs`: org.gradle.api.plugins.quality.FindBugsExtension get() =
    extensions.getByName("findbugs") as org.gradle.api.plugins.quality.FindBugsExtension

/**
 * Configures the [findbugs][org.gradle.api.plugins.quality.FindBugsExtension] project extension.
 */
fun Project.`findbugs`(configure: org.gradle.api.plugins.quality.FindBugsExtension.() -> Unit) =
        extensions.configure("findbugs", configure)

/**
 * Retrieves the [junitPlatform][org.junit.platform.gradle.plugin.JUnitPlatformExtension] project extension.
 */
val Project.`junitPlatform`: org.junit.platform.gradle.plugin.JUnitPlatformExtension get() =
    extensions.getByName("junitPlatform") as org.junit.platform.gradle.plugin.JUnitPlatformExtension

/**
 * Configures the [junitPlatform][org.junit.platform.gradle.plugin.JUnitPlatformExtension] project extension.
 */
fun Project.`junitPlatform`(configure: org.junit.platform.gradle.plugin.JUnitPlatformExtension.() -> Unit) =
        extensions.configure("junitPlatform", configure)
