allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("publishToMavenLocal") {
    dependsOn(":plugin:publishToMavenLocal")
    dependsOn(":grpc-multiplatform-lib:publishToMavenLocal")
}