[![](https://jitpack.io/v/ngocji/jiadapter.svg)](https://jitpack.io/#ngocji/jiadapter)
# Getting Started (Gradle / Android Studio)
Add settings.gradle.kts
```
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

Add dependency to your application.
```
implementation 'com.github.ngocji:jiadapter:1.0.1'
```