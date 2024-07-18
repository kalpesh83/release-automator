import com.lordcodes.turtle.GitCommands
import com.lordcodes.turtle.shellRun
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

private const val REMOTE_URL= "remote_url"

fun main(array: Array<String>) {
//    "git add .".runCommand()
//    "git commit -m 'Init project'".runCommand()
//    "git push origin HEAD".runCommand()
    shellRun {
        git.commitAllChanges("Update gradle scripts")
        git.pushToOrigin()
    }
}

private fun a(){

}

private fun String.exec() {
    val process = Runtime.getRuntime().exec(this)
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    var read: Int
    val buffer = CharArray(4096)
    val output = StringBuffer()
    while ((reader.read(buffer).also { read = it }) > 0) {
        output.append(buffer, 0, read)
    }
    reader.close()
    process.waitFor()
    println(output)
}

fun String.runCommand() {
    try {
        println("Executing: $this")
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
//            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(60, TimeUnit.MINUTES)
        println("Execution complete: $this \nOutput: ${proc.inputStream.bufferedReader().readText()}")
    } catch (e: IOException) {
        e.printStackTrace()
    }
}