package com.benrhine.plugins.v1

import com.benrhine.plugins.v1.tasks.*
import com.benrhine.plugins.v1.util.ExtensionHelpers
import groovy.lang.Closure
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.IOException

/** --------------------------------------------------------------------------------------------------------------------
 * SemanticVersioningWithBuildNumberPlugin: Plugin that supports semantic versioning with a build number. Can be used
 * strictly for project versioning or in conjunction with safe agile to match versions to pi and increments.
 * ------------------------------------------------------------------------------------------------------------------  */
class SemanticVersioningWithBuildNumberPlugin : Plugin<Project> {
    /**
     * apply: Invoke the plugin to be applied on a given project.
     *
     * @param project Project
     */
    override fun apply(project: Project) {
        // Initialize `build.gradle` extension closure
        project.extensions.create(VERSION_CONFIG, SemanticVersioningWithBuildNumberPluginExtension::class.java)
        // Initialize tasks included with the plugin
        project.tasks.register(INCREMENT_MAJOR_VERSION, IncrementMajorVersionTask::class.java)
        project.tasks.register(INCREMENT_MINOR_VERSION, IncrementMinorVersionTask::class.java)
        project.tasks.register(INCREMENT_PATCH_VERSION, IncrementPatchVersionTask::class.java)
        project.tasks.register(DECREMENT_MAJOR_VERSION, DecrementMajorVersionTask::class.java)
        project.tasks.register(DECREMENT_MINOR_VERSION, DecrementMinorVersionTask::class.java)
        project.tasks.register(DECREMENT_PATCH_VERSION, DecrementPatchVersionTask::class.java)
        project.tasks.register(PRINT_VERSION, PrintVersionTask::class.java)
        // Apply plugin to project post initialization - without this it will not set the version correctly on initialization
        project.gradle.afterProject(object : Closure<Void?>(project) {
            fun doCall(project: Project) {
                try {
                    // Retrieve the `version.properties` file. This can be either at the default location of the project
                    // root OR at a custom path location specified by the `build.gradle` extension block
                    val prop = ExtensionHelpers.getLocalProperties(project)
                    // Move the locally read properties to the project properties
                    ExtensionHelpers.loadLocalPropertiesToProjectProperties(project, prop)
                    // Determine if this is a remote build (is this a CI build)
                    val isRemoteBuild = ExtensionHelpers.getExtensionDefinedRemoteBuild(project, prop)
                    // Generate the full project version
                    ExtensionHelpers.generateVersion(project, isRemoteBuild)
                    // artifactType is checked and set to project properties above, make sure it gets set into properties
                    // and stored back to the `version.properties` file.
                    if (project.hasProperty("artifact-type")) {
                        prop.setProperty("artifact-type", project.properties["artifact-type"].toString())
                    }
                    // Save `version.properties` to the root project folder OR to the location specified in the
                    // `build.gradle` extension block
                    ExtensionHelpers.writeLocalProperties(project, prop)
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    project.setProperty("version", ExtensionHelpers.generateVersionWithArtifactType(project))
                }
            }
        })
    }

    companion object {
        const val VERSION_CONFIG = "versionConfig"
        const val PRINT_VERSION = "printVersion"
        const val INCREMENT_MAJOR_VERSION = "incrementMajorVersion"
        const val INCREMENT_MINOR_VERSION = "incrementMinorVersion"
        const val INCREMENT_PATCH_VERSION = "incrementPatchVersion"
        const val DECREMENT_MAJOR_VERSION = "decrementMajorVersion"
        const val DECREMENT_MINOR_VERSION = "decrementMinorVersion"
        const val DECREMENT_PATCH_VERSION = "decrementPatchVersion"
    }
}