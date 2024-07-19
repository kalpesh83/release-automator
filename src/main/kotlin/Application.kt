import models.GitProperties
import models.ReleaseProperties
import java.io.IOException
import java.util.concurrent.TimeUnit

fun main(array: Array<String>) {
    val a = arrayOf("remote", "https://github.com/kalpesh83/release-automator.git", "branch", "release-v2024.13")
    val gitProperties = GitProperties.get(a)
    val releaseProperties = ReleaseProperties.get(gitProperties)
    ReleaseManager(gitProperties = gitProperties, releaseProperties = releaseProperties)
        .release()
}

fun String.runCommand() {
    try {
        println("Executing: $this")
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(60, TimeUnit.MINUTES)
        println("Execution complete: $this \nOutput: ${proc.inputStream.bufferedReader().readText()}")
    } catch (e: IOException) {
        e.printStackTrace()
    }
}