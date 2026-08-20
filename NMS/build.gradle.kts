dependencies {
    api(project(":API"))

    compileOnly(libs.paper.api)
    compileOnly(libs.nightcore)
    compileOnly(libs.annotations)
}
