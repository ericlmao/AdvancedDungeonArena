import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    alias(libs.plugins.paperweight.userdev)
}

dependencies {
    // Mojang-mapped Paper internals (CraftBukkit + net.minecraft), no BuildTools required.
    paperweight.paperDevBundle(libs.versions.paper.get())

    api(project(":NMS"))

    compileOnly(libs.jspecify)
}

// Paper 1.20.5+ runs mojang-mapped at runtime; we target Paper/Folia only, so publish
// the mojang-mapped jar directly and skip reobfuscation entirely.
paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION
