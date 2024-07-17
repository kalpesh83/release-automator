import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

fun main(array: Array<String>) {
    val version = "1.1.0"
//    val s =
//        "curl -L --url https://kalpesh83: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@maven.pkg.github.com/kalpesh83/release-automator/sharechat/library/automator/$version/automator-$version.jar -o automator.jar"
//    val ex = Runtime.getRuntime().exec(s)
//    ex.inputStream.reader().use {
//        println(it.readText())
//    }
//    ex.waitFor()
//    println(s.runCommand())
//    Thread.sleep(1000)
//
//    println(output)
//    println("git init".runCommand())
//    println("git status".runCommand())
    "git add .".runCommand()
    "git commit -m 'Init project'".runCommand()
    "git push origin HEAD".runCommand()
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