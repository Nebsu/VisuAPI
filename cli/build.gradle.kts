
plugins {
    java
    application
}

application.mainClass.set("up.visulog.cli.CLILauncher")


val run by tasks.getting(JavaExec::class) {
    standardInput = System.`in`
}

dependencies {
    implementation(project(":analyzer"))
    implementation(project(":config"))
    implementation(project(":gitrawdata"))
    testImplementation("junit:junit:4.+")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
}


