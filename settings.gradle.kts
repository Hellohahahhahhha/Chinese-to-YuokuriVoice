pluginManagement {
  repositories {
    maven{url=uri("https://mirrors.aliyun.com/maven/")}
    maven {url = uri("https://mirrors.aliyun.com/google/")}
    maven { url = uri("https://mirrors.aliyun.com/gradle-plugin/")}
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "ChineseToY"

include(":app")