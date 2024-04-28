plugins {
    id("java-gradle-plugin")
}

group = "org.quiltmc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { setUrl("https://libraries.minecraft.net") }
}

dependencies {
    implementation("com.mojang:datafixerupper:7.0.13")
    implementation("com.google.code.gson:gson:2.10.1")
}

gradlePlugin {
    plugins {
        create("quiltGradle") {
            id = "$group.quilt-gradle"
            implementationClass = "org.quiltmc.quilt_gradle.QuiltGradlePlugin"
        }
    }
}
