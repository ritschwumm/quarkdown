plugins {
    kotlin("jvm") version "1.8.21"
    application
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(project(":core"))
    implementation(project(":stdlib"))
}

application {
    mainClass.set("eu.iamgio.quarkdown.cli.QuarkdownCliKt")
}
