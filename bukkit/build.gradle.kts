repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.opencollab.dev/main/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://maven.zetaplugins.com/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")

    compileOnly("com.zetaplugins:zetacore:1.2.1")

    compileOnly("com.mysql:mysql-connector-j:9.2.0")
    compileOnly("com.zaxxer:HikariCP:6.2.1")
    compileOnly("org.mariadb.jdbc:mariadb-java-client:3.5.2")
    compileOnly("org.xerial:sqlite-jdbc:3.45.3.0")

    compileOnly("org.projectlombok:lombok:1.18.44")
    annotationProcessor("org.projectlombok:lombok:1.18.44")
    compileOnly("com.googlecode.json-simple:json-simple:1.1.1")

    implementation("org.bstats:bstats-bukkit:3.0.2")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("org.geysermc.floodgate:api:2.2.4-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT")

    compileOnly("net.kyori:adventure-text-minimessage:4.26.1")
    compileOnly("net.kyori:adventure-text-serializer-legacy:4.26.1")
    // implementation(project(":chunky-common"))
    // implementation(project(":chunky-paper"))
    // implementation(project(":chunky-folia"))
}

tasks {
     processResources {
         var props = mapOf("version" to project.version,
             "name" to project.findProperty("artifactName"),
             "description" to project.findProperty("description"))
         inputs.properties(props)
         filteringCharset = "UTF-8"
         filesMatching("plugin.yml") {
             expand(props)
         }
     }
    shadowJar {
        // minimize {
        //     exclude(project(":chunky-common"))
        //     exclude(project(":chunky-paper"))
        //     exclude(project(":chunky-folia"))
        // }
        relocate("org.bstats", "${project.group}.${rootProject.name}.lib.bstats")
        manifest {
            attributes("paperweight-mappings-namespace" to "mojang")
        }
        archiveFileName.set("${project.property("artifactName")}-Bukkit-${project.version}.jar")
    }
}