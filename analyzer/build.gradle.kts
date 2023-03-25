
plugins {
    `java-library`
}

dependencies {
    implementation(project(":config"))
    implementation(project(":gitrawdata"))
    testImplementation("junit:junit:4.+")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
}


