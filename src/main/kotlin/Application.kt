import models.GitProperties
import models.ReleaseProperties
import java.io.IOException
import java.util.concurrent.TimeUnit

fun main(array: Array<String>) {
    val gitProperties = GitProperties.get(array)
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