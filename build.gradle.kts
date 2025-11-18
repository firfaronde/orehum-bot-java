plugins {
    id("java")
    application
}

group = "firfaronde"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.discord4j:discord4j-core:3.3.0")
    implementation("io.github.cdimascio:dotenv-java:3.2.0")
    implementation("org.postgresql:postgresql:42.7.8")
}

application {
    mainClass = "firfaronde.Main"
}