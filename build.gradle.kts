plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.vkr"
version = "0.0.1"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

intellij {
    version.set("2024.2")
    type.set("IC")
    plugins.set(listOf("java")) // нужно для PSI Java и всего, что связано с com.intellij.modules.java
}

tasks {
    patchPluginXml {
        sinceBuild.set("242")
        untilBuild.set("252.*")
    }
}
