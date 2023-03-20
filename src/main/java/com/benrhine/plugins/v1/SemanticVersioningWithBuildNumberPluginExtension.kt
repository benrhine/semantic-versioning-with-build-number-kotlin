package com.benrhine.plugins.v1

import java.util.*

/** --------------------------------------------------------------------------------------------------------------------
 * SemanticVersioningWithBuildNumberPluginExtension: Extension class in support of plugin configuration.
 *
 *
 * versionConfig {
 * remoteBuild = true
 * ciBuildNumberEnvVarName = "BUILD_RUN_NUMBER" //BITBUCKET_BUILD_NUMBER
 * artifactType = "SNAPSHOT"
 * includeReleaseTag = true
 * includeBuildNumber = true
 * customVersionPropertiesPath = "$projectDir/src/main/resources/version.properties"
 * }
 * ------------------------------------------------------------------------------------------------------------------  */
open class SemanticVersioningWithBuildNumberPluginExtension {
    /**
     * isRemoteBuild: Check if the build is performed remotely i.e. is this a CI build.
     *
     * @return boolean
     */
    /**
     * setRemoteBuild: Set if remote build.
     *
     * @param remoteBuild boolean
     */
    var isRemoteBuild = false
    /**
     * isIncludeReleaseTag: Do you want to include the word RELEASE on your release build.
     *
     * @return boolean
     */
    /**
     * setIncludeReleaseTag: Set if you want to include the release tag.
     *
     * @param includeReleaseTag boolean
     */
    var isIncludeReleaseTag = false
    /**
     * isIncludeBuildNumber: Do you want to include the build number in your release?
     *
     * @return boolean
     */
    /**
     * setIncludeBuildNumber: Set if you want to include the build number.
     *
     * @param includeBuildNumber boolean
     */
    var isIncludeBuildNumber = false
    /**
     * getCiBuildNumberEnvVarName: Return the ENV VAR name that was set to attempt to get the build number.
     *
     * @return String
     */
    /**
     * setCiBuildNumberEnvVarName: Set the ENV VAR name to your CI/CD predefined variable.
     *
     * @param ciBuildNumberEnvVarName String
     */
    @JvmField
    var ciBuildNumberEnvVarName: String? = null

    /**
     * getArtifactType: What artifact type is set?
     *
     * @return String
     */
    var artifactType = "LOCAL"
        /**
         * setArtifactType: Set the artifact type of the build.
         *
         * @param artifactType String
         */
        set(artifactType) {
            field = artifactType.uppercase(Locale.getDefault())
        }
    /**
     * getCustomVersionPropertiesPath: Return the custom path to properties file.
     *
     * @return String
     */
    /**
     * setCustomVersionPropertiesPath: Set if you want to use a non default properties file.
     *
     * @param customVersionPropertiesPath String
     */
    @JvmField
    var customVersionPropertiesPath = "gradle.properties"
}