plugins {
    id("local.library")
}

android {
    namespace = "com.amadiyawa.feature_user"
}

dependencies {
    api(projects.featureBase)

    ksp(libs.roomCompiler)

    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.junitJupiterEngine)
}