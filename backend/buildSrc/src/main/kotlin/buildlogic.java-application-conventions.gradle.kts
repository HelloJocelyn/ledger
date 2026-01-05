
import org.gradle.kotlin.dsl.withType

plugins {
    // Java 或 Kotlin 你二选一或都要，看模块用什么语言
    java
//    kotlin("jvm")
}

//java {
//    toolchain {
//        languageVersion.set(JavaLanguageVersion.of(21)) // 或 17，看你需求
//    }
//}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

//tasks.withType<KotlinCompile> {
//    kotlinOptions {
//        jvmTarget = "21"  // 和 toolchain 对齐
//        freeCompilerArgs = freeCompilerArgs + listOf(
//            "-Xjsr305=strict"
//        )
//    }
//}

tasks.withType<Test> {
    useJUnitPlatform()
}
