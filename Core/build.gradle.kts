plugins {
    alias(libs.plugins.shadow)
}

dependencies {
    // Project modules - these are the only artifacts bundled into the final jar.
    implementation(project(":Lib"))
    implementation(project(":API"))
    implementation(project(":NMS"))
    implementation(project(":MC_1_21_11"))

    implementation(libs.annotations)

    compileOnly(libs.paper.api)

    // SunLight is reached reflectively (its API extends nightcore types) - no compile dependency.

    compileOnly(libs.mythic.dist)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.packetevents.spigot)
    compileOnly(libs.protocollib)

    compileOnly(libs.essentialsx) {
        exclude(group = "com.mojang", module = "brigadier")
        exclude(group = "io.papermc", module = "paperlib")
        exclude(group = "net.md-5", module = "bungeecord-chat")
    }

    compileOnly(libs.tab.api)

    compileOnly(libs.mcmmo) {
        exclude(group = "com.sk89q.worldguard")
    }

    compileOnly(libs.mythiclib.dist)
    compileOnly(libs.mmocore.api)
    compileOnly(libs.combatpets.api)

    compileOnly(libs.aurora.levels)
    compileOnly(libs.aurora)
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    archiveFileName = "${rootProject.name}-${project.version}.jar"
    destinationDirectory = rootProject.layout.buildDirectory.dir("libs")

    // Mirrors the old maven-shade <include>su.nightexpress.dungeonarena:*</include>
    dependencies {
        include(dependency("su.nightexpress.dungeonarena:.*:.*"))
    }

    mergeServiceFiles()
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}
