package com.benrhine.plugins.v1.util

import com.benrhine.plugins.v1.SemanticVersioningWithBuildNumberPluginExtension
import org.gradle.api.Project
import java.io.*
import java.util.*

/** --------------------------------------------------------------------------------------------------------------------
 * ExtensionHelpers: Re-usable helper functions.
 * ------------------------------------------------------------------------------------------------------------------  */
object ExtensionHelpers {
    /**
     * getLocalProperties:
     *
     * @param project Project
     * @return Properties
     * @throws IOException an Exception
     */
    @JvmStatic
    @Throws(IOException::class)
    fun getLocalProperties(project: Project): Properties {
        val input: InputStream = FileInputStream(getExtensionDefinedPath(project))
        val prop: Properties = OrderedProperties()

        // Load the `gradle.properties` file into the plugin
        prop.load(input)
        return prop
    }

    /**
     * writeLocalProperties
     *
     * @param project Project
     * @param prop Properties
     *
     * @throws IOException an Exception
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeLocalProperties(project: Project, prop: Properties) {
        val output: OutputStream = FileOutputStream(getExtensionDefinedPath(project))
        prop.store(output, "Updating .properties - THIS FILE REGENERATED WHEN TASK IS EXECUTED")

        // System.out.println(prop);
    }

    /**
     * loadLocalPropertiesToProjectProperties
     *
     * @param project Project
     *
     * @param prop Properties
     */
    @JvmStatic
    fun loadLocalPropertiesToProjectProperties(project: Project, prop: Properties) {
        project.setProperty("major", prop.getProperty("major"))
        project.setProperty("minor", prop.getProperty("minor"))
        project.setProperty("patch", prop.getProperty("patch"))
        project.setProperty("artifact-type", prop.getProperty("artifact-type"))
    }

    /**
     * getExtensionDefinedPath:
     *
     * @param project Project
     *
     * @return String
     */
    fun getExtensionDefinedPath(project: Project): String {
        val extension =
            project.extensions.findByName("versionConfig") as SemanticVersioningWithBuildNumberPluginExtension?
        var path = "gradle.properties"
        if (extension != null) {
            path = extension.customVersionPropertiesPath
        }
        return path
    }

    /**
     * getExtensionDefinedRemoteBuild:
     *
     * @param project Project
     * @param prop Properties
     *
     * @return boolean
     */
    @JvmStatic
    fun getExtensionDefinedRemoteBuild(project: Project, prop: Properties): Boolean {
        var isRemoteBuild = false
        if (project.hasProperty("remote-build")) {
            val extension =
                project.extensions.findByName("versionConfig") as SemanticVersioningWithBuildNumberPluginExtension?
            if (extension != null) {
                isRemoteBuild = extension.isRemoteBuild
            } else {
                val propIsRemoteBuild = prop.getProperty("remote-build")
                if (propIsRemoteBuild != null && !propIsRemoteBuild.isEmpty()) {
                    try {
                        isRemoteBuild = java.lang.Boolean.parseBoolean(propIsRemoteBuild)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        println("Warning: Could not parse value from properties - defaulting remote build to false")
                    }
                }
            }
        }
        return isRemoteBuild
    }

    /**
     * getExtensionDefinedArtifactType:
     *
     * @param project Project
     * @param prop Properties
     * @return String
     */
    internal fun getExtensionDefinedArtifactType(project: Project, prop: Properties): String {
        val extension =
            project.extensions.findByName("versionConfig") as SemanticVersioningWithBuildNumberPluginExtension?
        var path = prop.getProperty("artifact-type")
        if (extension != null) {
            path = extension.artifactType
        }
        return path
    }

    /**
     * generateVersion:
     *
     * @param project Project
     *
     * @param isRemoteBuild boolean
     */
    @JvmStatic
    fun generateVersion(project: Project, isRemoteBuild: Boolean) {
        if (isRemoteBuild) {
            project.setProperty("version", generateVersionWithBuildNumberAndArtifactType(project))
        } else {
            project.setProperty("version", generateVersionWithArtifactType(project))
        }
    }

    /**
     * generateVersionWithArtifactType:
     *
     * @param project Project
     *
     * @return String
     */
    fun generateVersionWithArtifactType(project: Project): String {
        val extension =
            project.extensions.findByName("versionConfig") as SemanticVersioningWithBuildNumberPluginExtension?
        val artifactType = extension!!.artifactType
        val includeReleaseTag = extension.isIncludeReleaseTag
        val version = project.properties["major"].toString() + "." + project.properties["minor"] + "." +
                project.properties["patch"]

        // Check if a custom artifact type is being specified through the extension
        if (!artifactType.isEmpty()) {
            checkArtifactType(artifactType)
            project.setProperty("artifact-type", artifactType)
            if (artifactType.equals("RELEASE", ignoreCase = true)) {
                if (!includeReleaseTag) {
                    return version
                }
            }
        }
        // If no artifact type is specified through the extension, use the default value from gradle.properties
        return version + "-" + project.properties["artifact-type"]
    }

    /**
     * checkArtifactType:
     *
     * @param artifactType String
     */
    private fun checkArtifactType(artifactType: String) {
        when (artifactType) {
            "LOCAL", "SNAPSHOT", "CANARY", "ALPHA", "BETA", "RELEASE", "EXPERIMENTAL" -> {}
            else -> {
                println("INFO ONLY!!! Inbound artifact type DOES NOT MATCH supported / expected artifact types")
                println("This will not affect plugin function, this is only to inform the user they may have misspelled the artifact type or that they are using an unusual type.")
            }
        }
    }

    /**
     * generateVersionWithBuildNumberAndArtifactType:
     *
     * @param project Project
     *
     * @return String
     */
    fun generateVersionWithBuildNumberAndArtifactType(project: Project): String {
        val extension =
            project.extensions.findByName("versionConfig") as SemanticVersioningWithBuildNumberPluginExtension?
        val ciBuildNumberEnvVarName = extension!!.ciBuildNumberEnvVarName
        val artifactType = extension.artifactType
        val includeReleaseTag = extension.isIncludeReleaseTag
        val includeBuildNumber = extension.isIncludeBuildNumber
        val version = project.properties["major"].toString() + "." + project.properties["minor"] + "." +
                project.properties["patch"]
        val buildNumber: String?
        // Check that the ENV VAR for the build number is passed in and is not null or empty
        return if (ciBuildNumberEnvVarName != null && !ciBuildNumberEnvVarName.isEmpty()) {
            // Check if it matches known CI build number environment vars
            buildNumber = if (ciBuildNumberEnvVarName.equals("BUILD_RUN_NUMBER", ignoreCase = true) ||
                ciBuildNumberEnvVarName.equals("BITBUCKET_BUILD_NUMBER", ignoreCase = true)
            ) {
                // Attempt to get the build number
                println("Warning: Provided ENV VAR name matches predefined GitHub or BitBucket build number variable")
                System.getenv(extension.ciBuildNumberEnvVarName)
            } else {
                // Even if it doesn't match known build environment vars, try to get it anyway.
                println("Warning: Unknown ENV VAR name - This may have unexpected results")
                System.getenv(extension.ciBuildNumberEnvVarName)
            }
            // Check if the build number is null, if it is throw an exception
//            if (buildNumber == null) {
//                throw new RuntimeException("Provided ENV VAR for build number returned null value - Unable to build version that includes build number");
//            }

            // Check if a custom artifact type is being specified through the extension
            if (!artifactType.isEmpty()) {
                checkArtifactType(artifactType)
                project.setProperty("artifact-type", artifactType)
                if (artifactType.equals("RELEASE", ignoreCase = true)) {
                    if (!includeReleaseTag && !includeBuildNumber) {
                        return version
                    } else if (includeReleaseTag && !includeBuildNumber) {
                        return "$version-$artifactType"
                    } else if (!includeReleaseTag) {
                        if (buildNumber != null) {
                            return "$version.$buildNumber"
                        } else {
                            println("Warning: Build number from ENV VAR was null - VERSION WILL NOT INCLUDE BUILD NUMBER")
                        }
                    }
                }
            }
            if (buildNumber != null) {
                // If no artifact type is specified through the extension, use the default value from gradle.properties
                version + "." + buildNumber + "-" + project.properties["artifact-type"]
            } else {
                println("Warning: Build number from ENV VAR was null - VERSION WILL NOT INCLUDE BUILD NUMBER")
                // If no artifact type is specified through the extension, use the default value from gradle.properties
                version + "-" + project.properties["artifact-type"]
            }
        } else {
            println("Warning: No ENV VAR for build number has been set | Please add `ciBuildNumberEnvVarName = YOUR-VALUE` to the versionConfig block")
            println("Warning: Version will NOT set build number for this project run")
            generateVersionWithArtifactType(project)
        }
    }
}