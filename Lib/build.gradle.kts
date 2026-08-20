// Vendored subset of nightcore (su.nightexpress.nightcore), adapted to Paper-only APIs.
//
// Provenance: https://github.com/nulli0n/nightcore-spigot @ 2.16.4, package-renamed to
// su.nightexpress.dungeons.nightcore. The Spigot/Paper `bridge` abstraction, the reflection-based
// NBT layer, the deprecated command/menu/text/lang generations and the whole database layer were
// dropped; the text pipeline was rewritten onto Adventure.
dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.jspecify)

    // Folia region schedulers. `api` so :Core sees it too - the scheduling seams live here, but Core
    // call sites need the same types. Shaded + relocated by :Core's shadowJar.
    api(libs.folia.scheduler)

    // Soft dependencies of the vendored integration layer.
    compileOnly(libs.placeholderapi)
    compileOnly(libs.vault.api)
}

tasks.withType<JavaCompile>().configureEach {
    // Vendored third-party code: upstream's own @Deprecated markers are noise here.
    options.compilerArgs.addAll(listOf("-Xlint:none", "-nowarn", "-Xmaxerrs", "10000"))
}
