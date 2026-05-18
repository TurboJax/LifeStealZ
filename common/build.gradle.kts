repositories {
    maven("https://repo.opencollab.dev/main/")
}

dependencies {
    compileOnly("org.slf4j:slf4j-api:2.0.17")

    compileOnly("org.geysermc.floodgate:api:2.2.5-SNAPSHOT")

    compileOnly("com.mysql:mysql-connector-j:9.2.0")
    compileOnly("com.zaxxer:HikariCP:6.2.1")
    compileOnly("org.mariadb.jdbc:mariadb-java-client:3.5.2")
    compileOnly("org.xerial:sqlite-jdbc:3.45.3.0")

    compileOnly("org.yaml:snakeyaml:2.6")
    compileOnly("org.jspecify:jspecify:1.0.0")

    compileOnly("org.projectlombok:lombok:1.18.44")
    annotationProcessor("org.projectlombok:lombok:1.18.44")
    compileOnly("com.googlecode.json-simple:json-simple:1.1.1")
}

tasks {
    processResources {
        filesMatching("version.properties") {
            expand(
                "version" to project.version
            )
        }
    }
//    javadoc {
//        sourceSets {
//            main {
//                allJava
//            }
//        }
//        setDestinationDir(rootProject.projectDir.resolve("docs/chunky/javadoc"))
//        include("org/popcraft/chunky/api/**")
//        exclude("org/popcraft/chunky/api/ChunkyAPIImpl.java")
//    }
}