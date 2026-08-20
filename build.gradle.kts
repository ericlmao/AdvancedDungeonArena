// The root project holds no sources; it only carries shared configuration.
// Deliberately no `java` plugin here so that `:clean` does not race with
// `:Core:shadowJar`, which writes the final jar into the root build directory.
allprojects {
    group = "su.nightexpress.dungeonarena"
    version = "8.5.1"
}

subprojects {
    apply(plugin = "java-library")

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.nightexpressdev.com/releases")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://jitpack.io")
        maven("https://mvn.lumine.io/repository/maven-public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://repo.dmulloy2.net/repository/public/")
        maven("https://repo.essentialsx.net/releases/")
        maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
        maven("https://repo.auroramc.gg/releases")
        maven("https://nexus.neetgames.com/repository/maven-public")
    }

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion = JavaLanguageVersion.of(25)
        withSourcesJar()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    tasks.withType<Javadoc>().configureEach {
        options.encoding = "UTF-8"
    }
}
