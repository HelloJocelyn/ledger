plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "joce.practice.quant"
version = "0.1"



dependencies {
    // Web + JSON
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Security + OAuth2 Client
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    implementation("com.yubico:webauthn-server-core:2.8.0")

    // JPA + MySQL
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.mysql:mysql-connector-j")
//    implementation("org.flywaydb:flyway-core")
//    implementation("org.flywaydb:flyway-mysql")

    // 工具类
    implementation("org.apache.commons:commons-lang3:3.14.0")
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    // 测试（有些在 spring-conventions 里已经加过，这里可以按需精简）
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.rest-assured:rest-assured:5.4.0")
    implementation(project(":auth"))
    implementation(project(":ledger"))
}

tasks.test {
    useJUnitPlatform()
}