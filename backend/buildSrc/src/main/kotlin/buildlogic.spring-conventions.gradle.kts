//
//import org.gradle.kotlin.dsl.dependencies
//import org.gradle.kotlin.dsl.withType
//import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
//
//plugins {
//    id("org.springframework.boot")
//    id("io.spring.dependency-management")
//    kotlin("jvm")
//    kotlin("plugin.spring")
//    kotlin("plugin.jpa")
//}
//
//java {
//    toolchain {
//        languageVersion.set(JavaLanguageVersion.of(21)) // 或 17
//    }
//}
//
//tasks.withType<JavaCompile> {
//    options.encoding = "UTF-8"
//}
//
//tasks.withType<KotlinCompile> {
//    kotlinOptions {
//        jvmTarget = "21"
//        freeCompilerArgs = freeCompilerArgs + listOf(
//            "-Xjsr305=strict"
//        )
//    }
//}
//
//tasks.withType<Test> {
//    useJUnitPlatform()
//}
//
//// 这里也可以顺手放一些所有 Spring 模块通用的依赖（可选）
//dependencies {
//    // 这些是“几乎所有 Spring 模块都要用”的东西，可以抽这里
//    "implementation"("org.springframework.boot:spring-boot-starter-logging")
//    "testImplementation"("org.springframework.boot:spring-boot-starter-test")
//}
