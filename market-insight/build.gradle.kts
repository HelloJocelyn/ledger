plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "joce.practice.market"
version = "unspecified"

dependencies {
    // Web + JSON
    implementation("org.springframework.boot:spring-boot-starter-web")


    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // Security + OAuth2 Client
    implementation("org.springframework.boot:spring-boot-starter-security")

    // JPA + MySQL
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.mysql:mysql-connector-j")

    // other
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("org.apache.commons:commons-csv:1.11.0")
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
