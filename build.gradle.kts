import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val jvmTarget = "17"
val micronautVersion = "4.7.6"
val junitJupiterVersion = "5.9.2"
val logbackClassicVersion = "1.4.12"
val logbackEncoderVersion = "7.3"
val postgresqlVersion = "42.7.2"
val tcVersion = "1.17.6"
val mockkVersion = "1.13.4"
val kotestVersion = "5.5.5"
val rapidsRiversVersion = "202410290928"
val grunndataDtoVersion = "202502101053"


group = "no.nav.hm"
version = properties["version"] ?: "local-build"

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("kapt") version "1.9.25"
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("io.micronaut.application") version "4.4.5"
}

configurations.all {
    resolutionStrategy {
        failOnChangingVersions()
    }
}

dependencies {

    api("ch.qos.logback:logback-classic:$logbackClassicVersion")
    api("net.logstash.logback:logstash-logback-encoder:$logbackEncoderVersion")

    runtimeOnly("org.yaml:snakeyaml")
    implementation("io.micronaut:micronaut-jackson-databind")

    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")

    // micronaut-data
    implementation("io.micronaut.data:micronaut-data-jdbc")
    implementation("jakarta.persistence:jakarta.persistence-api")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    runtimeOnly("io.micronaut.sql:micronaut-jdbc-hikari")
    kapt("io.micronaut.data:micronaut-data-processor")
    kaptTest("io.micronaut.data:micronaut-data-processor")
    implementation("org.postgresql:postgresql:${postgresqlVersion}")
    implementation("io.micronaut.flyway:micronaut-flyway")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut.cache:micronaut-cache-caffeine")
    implementation("io.micronaut.micrometer:micronaut-micrometer-core")
    implementation("io.micronaut.micrometer:micronaut-micrometer-registry-prometheus")
    implementation("io.micronaut:micronaut-management")
    // gcp libs
    implementation("io.micronaut.gcp:micronaut-gcp-common")
    implementation("io.micronaut.objectstorage:micronaut-object-storage-gcp")

    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.micronaut.test:micronaut-test-kotest5")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testImplementation("org.testcontainers:postgresql:${tcVersion}")
    // Rapids and Rivers
    implementation("com.github.navikt:hm-rapids-and-rivers-v2-core:$rapidsRiversVersion")
    implementation("com.github.navikt:hm-rapids-and-rivers-v2-micronaut:$rapidsRiversVersion")
    implementation("com.github.navikt:hm-rapids-and-rivers-v2-micronaut-deadletter:$rapidsRiversVersion")
    implementation("no.nav.hm.grunndata:hm-grunndata-rapid-dto:$grunndataDtoVersion")
    implementation("org.flywaydb:flyway-database-postgresql:10.6.0")
}

micronaut {
    version.set(micronautVersion)
    testRuntime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
    }
}

application {
    mainClass.set("no.nav.hm.grunndata.media.Application")
}

java {
    sourceCompatibility = JavaVersion.toVersion(jvmTarget)
    targetCompatibility = JavaVersion.toVersion(jvmTarget)
    withSourcesJar()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = jvmTarget
    kapt.includeCompileClasspath = false
}

tasks.named<KotlinCompile>("compileTestKotlin") {
    kotlinOptions.jvmTarget = jvmTarget
    kapt.includeCompileClasspath = false
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("skipped", "failed")
        showExceptions = true
        showStackTraces = true
        showCauses = true
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "8.5"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    maven("https://packages.confluent.io/maven/")

}

