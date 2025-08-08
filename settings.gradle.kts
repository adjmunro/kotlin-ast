rootProject.name = "ast"

fun getProperty(gradlePropertiesName: String, systemEnvName: String, instructions: String): String {
    val result = providers.gradleProperty(gradlePropertiesName).orNull
        ?.removeSurrounding(delimiter = "\"")
        ?: System.getenv(systemEnvName)

    return requireNotNull(value = result) {
        """
        Required property not found! Expected:
        - Local: `$gradlePropertiesName` in `~/.gradle/gradle.properties`
        - CI: exported environment variable `$systemEnvName`
        $instructions
        """.trimIndent()
    }
}

pluginManagement {
    /**
     * The `pluginManagement.repositories` block configures the repositories that Gradle uses
     * to search for and download the Gradle plugins and their transitive dependencies.
     * You can also use local repositories or define your own remote repositories.
     */
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }

    // Plugins applied here are automatically applied to all projects
    plugins {

    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {

    /**
     * The `dependencyResolutionManagement.repositories` block is where you configure the source
     * repositories of the dependencies used by all modules in your project. However, you should
     * configure module-specific repositories in the build.gradle.kts files of the respective modules.
     */
    repositories {
        mavenCentral()
        google()

        val githubActor = getProperty(
            gradlePropertiesName = "gpr.user",
            systemEnvName = "GITHUB_ACTOR",
            instructions = " Please set it to your GitHub username.",
        )

        val githubToken = getProperty(
            gradlePropertiesName = "gpr.token",
            systemEnvName = "GITHUB_TOKEN",
            instructions = "Please set it to a valid GitHub token with `read:packages` permission."
        )

        listOf("adjmunro/project-inline").forEach { repository ->
            maven {
                url = uri("https://maven.pkg.github.com/$repository")
                credentials {
                    username = githubActor
                    password = githubToken
                }
            }
        }
    }

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
}
