import com.lordcodes.turtle.shellRun
import models.BranchType
import models.GitProperties
import models.ReleaseProperties
import models.VersionProperties
import notification.SlackNotificationConfig
import notification.SlackNotifier
import teamcity.TcBuildManager
import teamcity.TcProperties
import java.io.File

class ReleaseManager(
    private val isCI: Boolean,
    private val gitProperties: GitProperties,
    private val releaseProperties: ReleaseProperties,
    private val tcBuildManager: TcBuildManager,
    private val slackNotifier: SlackNotifier,
    private val tcProperties: TcProperties
) {

    companion object {
        private const val SC_CODE_OWNERS = "* @ShareChat/sc-appx"
        private const val CODE_OWNERS_FILE_PATH = ".github/CODEOWNERS"
    }

    fun release() {
        println("Rollout type: ${releaseProperties.rolloutType}")
        when (releaseProperties.rolloutType) {
            RolloutType.BETA_ROLLOUT -> doBetaRollout(releaseProperties.isNewRelease)
            RolloutType.PROD_ROLLOUT -> updateVersionAndPush(true)
        }
    }

    private fun doBetaRollout(isNewRelease: Boolean) {
        if (isNewRelease) {
            // Pull latest changes from main
            shellRun {
                println("Checking out to `main`")
                git.checkout(MAIN_BRANCH)
//                println("Pulling latest changes from `main`")
//                git.pull(gitProperties.origin, MAIN_BRANCH)
            }
            val versionProps = VersionProperties.get()
            val updatedVersionForMain = versionProps.increment(true)
            println("`versionName` updated: ${versionProps.versionName} -> ${updatedVersionForMain.versionName}")
            println("`versionCode` updated: ${versionProps.versionCode} -> ${updatedVersionForMain.versionCode}")
            updatedVersionForMain.updatePropertiesFile(
                isFirstRelease = false,
                branchType = BranchType.Main,
                rolloutType = RolloutType.BETA_ROLLOUT,
                parent = tcProperties.parent,
                rolloutPercent = releaseProperties.rolloutPercent
            )
            val message = updatedVersionForMain.buildMessage()
            // Commit the version changes and push to main
            shellRun {
                println("Pushing changes to `main`")
                git.gitCommand(listOf("add", VersionProperties.VERSION_FILE_NAME))
                git.commit(message)
                git.push(gitProperties.origin, MAIN_BRANCH)
            }
            // Update version and push to release-v* branch
            val updatedVersionForRelease = VersionProperties.get().increment(false)
            val releaseBranchName =
                RELEASE_BRANCH_PREFIX + "${updatedVersionForRelease.major}.${updatedVersionForRelease.minor}"
            updatedVersionForRelease.updatePropertiesFile(
                isFirstRelease = true,
                branchType = BranchType.Release,
                rolloutType = RolloutType.BETA_ROLLOUT,
                parent = tcProperties.parent,
                rolloutPercent = releaseProperties.rolloutPercent
            )
            val releaseMessage = updatedVersionForRelease.buildMessage()
            shellRun {
                println("Checking out to $releaseBranchName")
                git.checkout(releaseBranchName)
                println("Pushing changes to `$releaseBranchName`")
                git.gitCommand(listOf("add", VersionProperties.VERSION_FILE_NAME))
                git.commit(releaseMessage)
                git.push(gitProperties.origin, releaseBranchName)
            }
            val buildDetailsList = tcBuildManager.triggerBuilds(releaseBranchName, tcBuildManager.fetchBuildIds())
            if (buildDetailsList.isNotEmpty()) {
                val buildDetailsMessage = buildDetailsList.joinToString(separator = "\n\n") {
                    "${it.buildName}\n${it.buildUrl}"
                }
                println("Sending slack message")
                slackNotifier.notify(
                    webhook = slackNotifier.releaseCutWebhook,
                    config = SlackNotificationConfig(
                        versionCode = updatedVersionForMain.versionCode.toString(),
                        versionName = updatedVersionForMain.versionName,
                        branch = releaseBranchName,
                        parent = tcProperties.parent,
                        message = buildDetailsMessage,
                    )
                )
                slackNotifier.notify(
                    webhook = slackNotifier.regressionWebhook,
                    config = SlackNotificationConfig(
                        message = buildDetailsMessage,
                        branch = releaseBranchName,
                        parent = tcProperties.parent
                    )
                )
            }
            val publishBuildIds = tcBuildManager.fetchPublishBuildIds()
            if (publishBuildIds.isNotEmpty()) {
                tcBuildManager.triggerBuilds(releaseBranchName, publishBuildIds)
            }
        } else {
            updateVersionAndPush()
        }
    }

    private fun updateVersionAndPush(addCodeOwners: Boolean = false) {
        // Checkout to release branch and pull latest changes
        shellRun {
            println("Checking out to ${gitProperties.branch}")
            // Used for local testing. On TC the agents will be updated with all the refs already.
            if (isCI.not()) {
                println("Performing git fetch")
                git.gitCommand(listOf("fetch"))
            }
            git.checkout(gitProperties.branch, false)
//            println("Pulling latest changes from ${gitProperties.branch}")
//            git.pull(gitProperties.origin, gitProperties.branch)
        }
        val versionProps = VersionProperties.get()
        val updatedVersion = versionProps.increment(false)
        updatedVersion.updatePropertiesFile(
            isFirstRelease = false,
            branchType = BranchType.get(gitProperties.branch),
            rolloutType = releaseProperties.rolloutType,
            parent = tcProperties.parent,
            rolloutPercent = releaseProperties.rolloutPercent
        )
        println("versionName updated: ${versionProps.versionName} -> ${updatedVersion.versionName}")
        println("versionCode updated: ${versionProps.versionCode} -> ${updatedVersion.versionCode}")
        var releaseMessage = updatedVersion.buildMessage()
        var codeOwnersAdded = false
        if (addCodeOwners && addCodeOwnersIfNotExists()) {
            println("CODEOWNERS added")
            releaseMessage += " and update CODEOWNERS"
            codeOwnersAdded = true
        }
        shellRun {
            println("Pushing changes to ${gitProperties.branch}")
            git.gitCommand(listOf("add", VersionProperties.VERSION_FILE_NAME))
            if (codeOwnersAdded) {
                git.gitCommand(listOf("add", CODE_OWNERS_FILE_PATH))
            }
            git.commit(releaseMessage)
            git.push(gitProperties.origin, gitProperties.branch)
        }
        tcBuildManager.triggerBuilds(gitProperties.branch, tcBuildManager.fetchBuildIds())
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