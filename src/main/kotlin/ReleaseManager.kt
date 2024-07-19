import com.lordcodes.turtle.shellRun
import models.GitProperties
import models.ReleaseProperties
import models.VersionProperties

class ReleaseManager(
    private val gitProperties: GitProperties,
    private val releaseProperties: ReleaseProperties
) {

    fun release() {
        when (releaseProperties.rolloutType) {
            RolloutType.BETA_ROLLOUT -> doBetaRollout(releaseProperties.isNewRelease)
            RolloutType.PROD_ROLLOUT -> TODO()
        }
    }

    private fun doBetaRollout(isNewRelease: Boolean) {
        if (isNewRelease) {
            // Pull latest changes from main
            shellRun {
                git.checkout(MAIN_BRANCH)
                git.pull(gitProperties.origin)
            }
            val versionProps = VersionProperties.get()
            val updatedVersionForMain = versionProps.increment(true)
            updatedVersionForMain.updatePropertiesFile()
            val message =
                "Update versionName to ${updatedVersionForMain.versionName} and versionCode to ${updatedVersionForMain.versionCode}"
            // Commit the version changes and push to main
            shellRun {
                git.commitAllChanges(message)
                git.push(gitProperties.origin)
            }
            // Update version and push to release-v* branch
            val updatedVersionForRelease = VersionProperties.get().increment(false)
            val releaseBranchName =
                RELEASE_BRANCH_PREFIX + "${updatedVersionForRelease.major}.${updatedVersionForRelease.minor}"
            updatedVersionForRelease.updatePropertiesFile()
            val releaseMessage =
                "Update versionName to ${updatedVersionForRelease.versionName} and versionCode to ${updatedVersionForRelease.versionCode}"
            shellRun {
                git.checkout(releaseBranchName)
                git.commitAllChanges(releaseMessage)
                git.push(gitProperties.origin)
            }
        } else {
            // Checkout to release branch and pull latest changes
            shellRun {
                git.checkout(gitProperties.branch)
                git.pull(gitProperties.origin)
            }
            val versionProps = VersionProperties.get()
            val updatedVersion = versionProps.increment(false)
            updatedVersion.updatePropertiesFile()
            val releaseMessage =
                "Update versionName to ${updatedVersion.versionName} and versionCode to ${updatedVersion.versionCode}"
            shellRun {
                git.commitAllChanges(releaseMessage)
                git.push(gitProperties.origin)
            }
        }
    }
}