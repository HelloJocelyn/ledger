import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("com.diffplug.spotless") apply false
}

subprojects {
    // 只在有 Java 或 Kotlin 的模块中启用 Spotless
    pluginManager.withPlugin("java") {
        apply(plugin = "com.diffplug.spotless")
    }
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        apply(plugin = "com.diffplug.spotless")
    }

    // 关键点：插件应用后再配置（而且用 typed API，Kotlin DSL 不会 unresolved）
    plugins.withId("com.diffplug.spotless") {
        extensions.configure<SpotlessExtension>("spotless") {
            java {
                googleJavaFormat("1.22.0")
                target("src/**/*.java")
            }

            kotlin {
                ktlint("1.3.0")
                target("src/**/*.kt")
            }

            kotlinGradle {
                ktlint("1.3.0")
                target("*.gradle.kts", "gradle/**/*.gradle.kts")
            }
        }
    }
}
