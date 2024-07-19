import com.lordcodes.turtle.shellRun
import models.GitProperties
import models.ReleaseProperties
import models.VersionProperties
import java.io.File

class ReleaseManager(
    private val gitProperties: GitProperties,
    private val releaseProperties: ReleaseProperties
) {

    companion object {
        private const val SC_CODE_OWNERS = "* @ShareChat/sc-appx"
        private const val CODE_OWNERS_FILE_PATH = ".github/CODEOWNERS"
    }

    fun release() {
        when (releaseProperties.rolloutType) {
            RolloutType.BETA_ROLLOUT -> doBetaRollout(releaseProperties.isNewRelease)
            RolloutType.PROD_ROLLOUT -> updateVersionAndPush(true)
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
            val message = updatedVersionForMain.buildMessage()
            // Commit the version changes and push to main
            shellRun {
                git.commitAllChanges(message)
                git.push(gitProperties.origin, MAIN_BRANCH)
            }
            // Update version and push to release-v* branch
            val updatedVersionForRelease = VersionProperties.get().increment(false)
            val releaseBranchName =
                RELEASE_BRANCH_PREFIX + "${updatedVersionForRelease.major}.${updatedVersionForRelease.minor}"
            updatedVersionForRelease.updatePropertiesFile()
            val releaseMessage = updatedVersionForRelease.buildMessage()
            shellRun {
                git.checkout(releaseBranchName)
                git.commitAllChanges(releaseMessage)
                git.push(gitProperties.origin, releaseBranchName)
            }
        } else {
            updateVersionAndPush()
        }
    }

    private fun updateVersionAndPush(addCodeOwners: Boolean = false) {
        // Checkout to release branch and pull latest changes
        shellRun {
            // Used for local testing. On TC the agents will be updated with all the refs already.
//            git.gitCommand(listOf("fetch"))
            git.checkout(gitProperties.branch, false)
            git.pull(gitProperties.origin, gitProperties.branch)
        }
        val versionProps = VersionProperties.get()
        val updatedVersion = versionProps.increment(false)
        updatedVersion.updatePropertiesFile()
        var releaseMessage = updatedVersion.buildMessage()
        if (addCodeOwners && addCodeOwnersIfNotExists()) {
            releaseMessage += " and update CODEOWNERS"
        }
        shellRun {
            git.commitAllChanges(releaseMessage)
            git.push(gitProperties.origin, gitProperties.branch)
        }
    }

    private fun addCodeOwnersIfNotExists(): Boolean {
        val file = File(CODE_OWNERS_FILE_PATH)
        file.readLines().forEach {
            if (it.contains(SC_CODE_OWNERS)) return false
        }
        file.appendText("\n$SC_CODE_OWNERS")
        return true
    }
}