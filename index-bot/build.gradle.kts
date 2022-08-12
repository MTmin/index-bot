import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.5.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.5.20"
    kotlin("plugin.spring") version "1.4.30"
}

group = "com.tgse"
version = "2.0.0"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    // google()
    maven {
        url = uri("http://maven.aliyun.com/nexus/content/groups/public")
    }
    maven {
        url = uri("https://snapshots.elastic.co/maven/")
    }
    maven {
        name = "lucene-snapshots"
        url = uri("https://s3.amazonaws.com/download.elasticsearch.org/lucenesnapshots/83f9835")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.springframework.boot:spring-boot-starter")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("org.dom4j:dom4j:2.1.3")
    implementation("org.jsoup:jsoup:1.14.1")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("io.reactivex.rxjava3:rxjava:3.0.13")

    // telegram
    api("com.github.pengrad:java-telegram-bot-api:6.1.0") {
        exclude(group = "com.google.code.gson")
    }
    implementation("com.google.code.gson:gson:2.9.1")
    // await status
    implementation("org.ethereum:leveldbjni-all:1.18.3")
    // elasticsearch
    implementation("org.elasticsearch.client:elasticsearch-rest-high-level-client:7.12.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
