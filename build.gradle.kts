plugins {
    java
}

version = "0.0.1"
group = "up"

dependencies {
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
}

allprojects {
    repositories {
        mavenCentral()
    }

    plugins.apply("java")

    java.sourceCompatibility = JavaVersion.VERSION_1_10

}