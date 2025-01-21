import org.jetbrains.intellij.model.ProductRelease

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.coderknock.codegen"
version = "0.0.2"

repositories {
    // 添加阿里云镜像地址
    maven("https://maven.aliyun.com/repository/public/")
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.3")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf(/* Plugin Dependencies */))
}

dependencies {
    implementation("cn.hutool:hutool-all:5.8.35")
    implementation("org.jboss.forge.roaster:roaster-api:2.30.1.Final")
    implementation("org.jboss.forge.roaster:roaster-jdt:2.30.1.Final")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("223")
    }

    signPlugin {
        certificateChainFile.set(file("~/idea/chain.crt"))
        privateKeyFile.set(file("~/idea/private.pem"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
