plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.13.0"
}

group = "com.coderknock.codegen"
version = "0.0.4"

repositories {
    // 添加阿里云镜像地址
    maven("https://maven.aliyun.com/repository/public/")
    mavenCentral()
}

// Configure Gradle IntelliJ Platform Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
intellijPlatform {
    intellijVersion.set("2022.3")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf(/* Plugin Dependencies */))
}

dependencies {
    implementation("cn.hutool:hutool-all:5.8.36")
    implementation("org.jboss.forge.roaster:roaster-api:2.30.1.Final")
    implementation("org.jboss.forge.roaster:roaster-jdt:2.30.1.Final")
    implementation("org.jetbrains:marketplace-zip-signer:0.1.38")
    // JGit for Git operations
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.3.0.202506031305-r")
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

    intellijPlatform {
        patchPluginXml {
            sinceBuild.set("223")
            untilBuild.set(null as String?)
        }

        signPlugin {
            certificateChain.set(file(providers.environmentVariable("CERTIFICATE_CHAIN")))
            privateKey.set(file(providers.environmentVariable("PRIVATE_KEY")))
            password.set(providers.environmentVariable("PRIVATE_KEY_PASSWORD"))
        }

        publishPlugin {
            token.set(providers.environmentVariable("PUBLISH_TOKEN"))
        }
    }
}
