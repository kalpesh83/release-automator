import models.GitProperties
import models.ReleaseProperties
import network.Service
import notification.SlackNotifier
import teamcity.TcBuildManager
import teamcity.TcProperties
import java.io.IOException
import java.util.concurrent.TimeUnit


fun main(array: Array<String>) {
    val gitProperties = GitProperties.get(array)
    val releaseProperties = ReleaseProperties.get(args = array, gitProperties = gitProperties)
    val isCI = System.getenv("CI").toBoolean()

    val service = Service()
    val tcProperties = TcProperties.get(array)
    val slackNotifier = SlackNotifier.get(args = array, service = service)
    val tcBuildManager = TcBuildManager(
        args = array,
        tcProperties = tcProperties,
        service = service
    )
    ReleaseManager(
        isCI = isCI,
        gitProperties = gitProperties,
        releaseProperties = releaseProperties,
        tcBuildManager = tcBuildManager,
        slackNotifier = slackNotifier,
        tcProperties = tcProperties
    ).release()
}

fun String.runCommand(): String {
    try {
        println("Executing: $this")
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(60, TimeUnit.MINUTES)
        println("Execution complete: $this \nOutput: ${proc.inputStream.bufferedReader().readText()}")
        return proc.inputStream.bufferedReader().readText()
    } catch (e: IOException) {
        e.printStackTrace()
        return ""
    }
}