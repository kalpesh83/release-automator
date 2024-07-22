import models.GitProperties
import models.ReleaseProperties
import java.io.IOException
import java.util.concurrent.TimeUnit

fun main(array: Array<String>) {
    val resultHandler = ResultHandler.get(array)
    val gitProperties = GitProperties.get(array)
    val releaseProperties = ReleaseProperties.get(gitProperties)
    val isCI = System.getenv("CI").toBoolean()
    ReleaseManager(
        isCI = isCI,
        gitProperties = gitProperties,
        releaseProperties = releaseProperties,
        resultHandler = resultHandler
    ).release()
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