package network

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection

class Service {

    fun sendRequest(
        method: RequestMethod,
        networkUrl: String,
        requestBody: String,
        requestProperty: Map<String, String> = hashMapOf()
    ): String? {
        return try {
            val url = URL(networkUrl)
            val con: URLConnection = url.openConnection()
            val http: HttpURLConnection = con as HttpURLConnection
            http.setRequestMethod(method.type)
            requestProperty.forEach { (t, u) ->
                http.setRequestProperty(t, u)
            }
            http.setDoOutput(true)

            val out: ByteArray = requestBody.toByteArray()
            val length = out.size

            http.setFixedLengthStreamingMode(length)
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            http.connect()
            http.outputStream.use { os ->
                os.write(out)
            }
            http.inputStream.reader().readText()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}

enum class RequestMethod(val type: String) {
    POST("POST")
}