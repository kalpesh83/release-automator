package notification

import com.google.gson.Gson
import com.google.gson.JsonObject

data class SlackNotificationConfig(
    val branch: String? = null,
    val parent: Parent = Parent.ShareChat,
    val versionName: String? = null,
    val versionCode: String? = null,
    val message: String? = null
) {
    fun buildContract(): String {
        val obj = JsonObject()
        branch?.let {
            obj.addProperty("branch", it)
        }
        obj.addProperty("parent", parent.value)
        versionName?.let {
            obj.addProperty("versionName", it)
        }
        versionCode?.let {
            obj.addProperty("versionCode", it)
        }
        message?.let {
            obj.addProperty("message", it)
        }
        return Gson().toJson(obj)
    }
}

enum class Parent(val value: String) {
    ShareChat("ShareChat"),
    Moj("Moj");

    companion object {
        fun get(name: String): Parent {
            return entries.find { name.equals(it.value, true) } ?: ShareChat
        }
    }
}