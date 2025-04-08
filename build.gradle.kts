plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij") version "1.17.4"

}

group = "org.vimMode"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

intellij {
    version.set("2024.1.7")
    type.set("IC")
    plugins.set(listOf(/* Plugin Dependencies */))
}

dependencies {
    implementation("com.github.jnr:jnr-unixsocket:0.38.18")
    implementation("org.msgpack:msgpack-core:0.8.22")
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("243.*")
        pluginDescription.set("Vim Mode: Connect Neovim inside IntelliJ.")
        changeNotes.set("Initial setup with notification action.")
    }

    // Commented out for now unless publishing
    /*
    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
    */
}
