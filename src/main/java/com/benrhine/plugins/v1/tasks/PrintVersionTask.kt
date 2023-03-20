package com.benrhine.plugins.v1.tasks

import com.benrhine.plugins.v1.util.ExtensionHelpers.generateVersion
import com.benrhine.plugins.v1.util.ExtensionHelpers.getExtensionDefinedRemoteBuild
import com.benrhine.plugins.v1.util.ExtensionHelpers.getLocalProperties
import com.benrhine.plugins.v1.util.ExtensionHelpers.loadLocalPropertiesToProjectProperties
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.IOException

/**  --------------------------------------------------------------------------------------------------------------------
 * PrintVersionTask: Print the current version at any point via task.
 * ------------------------------------------------------------------------------------------------------------------  */
open class PrintVersionTask : DefaultTask() {
    /**
     * printVersion: Task that executes printing the version.
     */
    @TaskAction
    fun printVersion() {
        try {
            val prop = getLocalProperties(project)
            // Move the locally read properties to the project properties
            loadLocalPropertiesToProjectProperties(project, prop)
            // Determine if this is a remote build (is this a CI build)
            val isRemoteBuild = getExtensionDefinedRemoteBuild(project, prop)
            // Generate the full project version
            generateVersion(project, isRemoteBuild)
            // Print out the complete project version
            println(project.properties["version"])
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}