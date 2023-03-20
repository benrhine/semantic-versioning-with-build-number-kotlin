package com.benrhine.plugins.v1.tasks

import com.benrhine.plugins.v1.util.ExtensionHelpers.generateVersion
import com.benrhine.plugins.v1.util.ExtensionHelpers.getExtensionDefinedRemoteBuild
import com.benrhine.plugins.v1.util.ExtensionHelpers.getLocalProperties
import com.benrhine.plugins.v1.util.ExtensionHelpers.loadLocalPropertiesToProjectProperties
import com.benrhine.plugins.v1.util.ExtensionHelpers.writeLocalProperties
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.IOException

/** --------------------------------------------------------------------------------------------------------------------
 * DecrementMajorVersionTask: Decrement the major version.
 * ------------------------------------------------------------------------------------------------------------------  */
open class DecrementMajorVersionTask : DefaultTask() {
    /**
     * decrementMajorVersion: Task that executes incrementing the major version.
     *
     * @throws Exception (optionally)
     */
    @TaskAction
    @Throws(Exception::class)
    fun decrementMajorVersion() {
        try {
            val prop = getLocalProperties(project)
            // Increment the major version and store it into the local properties object
            val major = prop.getProperty("major").toInt()
            if (major > 0) {
                prop.setProperty("major", (major - 1).toString())
            } else {
                println("Error: Can not decrement values less than 1 - no changes will be made to the version")
            }
            // Move the locally read properties to the project properties
            loadLocalPropertiesToProjectProperties(project, prop)
            // Determine if this is a remote build (is this a CI build)
            val isRemoteBuild = getExtensionDefinedRemoteBuild(project, prop)
            // Generate the full project version
            generateVersion(project, isRemoteBuild)
            // Save `version.properties` to the root project folder OR to the location specified in the
            // `build.gradle` extension block
            writeLocalProperties(project, prop)
            println(project.properties["version"])
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}