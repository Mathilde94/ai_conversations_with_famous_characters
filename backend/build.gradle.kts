import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.google.protobuf.gradle.*

plugins {
    id("org.springframework.boot") version "2.7.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.jetbrains.kotlin.plugin.spring") version "1.9.0"
    id("com.google.protobuf") version "0.8.13"
    id("org.jetbrains.kotlin.plugin.noarg") version "1.3.72"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.3.72"

    id("org.jetbrains.kotlinx.kover") version "0.7.5"
    id("application")

    java
    kotlin("jvm") version "1.8.10"

    id("com.diffplug.gradle.spotless") version "3.26.1"
}

application {
    mainClass.set("com.chat.ApplicationKt")
}

group = "org.example"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

val grpcVersion = "1.36.0"
val protobufVersion = "3.15.1"
val grpcKotlinVersion = "1.0.0"
val grpcSpringBootStarterVersion = "2.7.0.RELEASE"

dependencies {
    implementation("io.grpc:grpc-netty:1.54.0")
    implementation("io.grpc:grpc-protobuf:1.54.0")
    implementation("io.grpc:grpc-stub:1.54.0")
    implementation(platform("io.grpc:grpc-bom:1.36.0"))
    implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.4.2"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("io.grpc:grpc-kotlin-stub:1.3.0")
    implementation("io.grpc:protoc-gen-grpc-java:1.54.0")
    implementation("io.grpc:protoc-gen-grpc-kotlin:1.3.0")
    implementation("com.google.protobuf:protobuf-kotlin:3.22.2")
    implementation("com.google.protobuf:protobuf-java:3.22.2")
    implementation("com.google.protobuf:protobuf-java-util:3.22.2")
    implementation("com.google.protobuf:protobuf-gradle-plugin:0.9.2")
    implementation("org.projectlombok:lombok:1.18.16")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("org.springframework.boot:spring-boot-starter-hateoas")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("jakarta.annotation:jakarta.annotation-api:1.3.5")
    implementation("net.devh:grpc-spring-boot-starter:$grpcSpringBootStarterVersion")
    implementation(kotlin("stdlib"))

    // redis things
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    // For tests:
    testImplementation("com.ninja-squad:springmockk:1.1.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test"))
    testImplementation("io.projectreactor:reactor-test:3.6.0")
}


tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint().userData(mapOf("disabled_rules" to "import-ordering"))
    }
}

val ignoredCoverageClasses = listOf("")

koverReport {
    filters {
        excludes {
            classes("com.chat.Application", "com.chat.ApplicationKt")
            packages("com.protobuf.dto.chat.v1", "com.protobuf.rpc.chat.v1", "com.chat.config")
        }
    }

    verify {
        rule {
            isEnabled = true
            bound {
                minValue = 80 // Minimum coverage percentage
            }
        }
    }
}

val protocVersion = "3.18.0"

sourceSets {
    main {
        proto {
            srcDir("../protobufs")
        }
        java {
            srcDir("build/generated/source/proto/main/java")
        }
        kotlin {
            srcDir("build/generated/source/proto/main/grpckt")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.22.2"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.54.0"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.3.0:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}