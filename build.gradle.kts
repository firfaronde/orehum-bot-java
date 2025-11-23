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
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    compileOnly("org.projectlombok:lombok:1.18.38")
    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.11")
}

application {
    mainClass = "firfaronde.Main"
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "firfaronde.Main"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
