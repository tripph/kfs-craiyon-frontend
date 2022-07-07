val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("multiplatform") version "1.7.0"
    kotlin("plugin.serialization") version "1.7.0"

    application
}

group = "me.tripp"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "16"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
            }

        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))


            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
                implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
                implementation("io.ktor:ktor-client-core:$ktor_version")

                implementation("io.ktor:ktor-server-websockets-jvm:$ktor_version")
                implementation("io.ktor:ktor-server-netty:2.0.1")
                implementation("io.ktor:ktor-server-html-builder-jvm:$ktor_version")
                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
                implementation("io.ktor:ktor-client-cio:$ktor_version")
                implementation("ch.qos.logback:logback-classic:$logback_version")

            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.0.0-pre.332-kotlin-1.6.21")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.0.0-pre.332-kotlin-1.6.21")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.9.0-pre.332-kotlin-1.6.21")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:6.3.0-pre.332-kotlin-1.6.21")
            }
        }
        val jsTest by getting
    }
}

application {
    mainClass.set("me.tripp.application.ServerKt")
}

tasks.named<Copy>("jvmProcessResources") {
    val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
    from(jsBrowserDistribution)
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))
}
