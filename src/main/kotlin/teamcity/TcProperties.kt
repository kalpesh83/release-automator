package teamcity

import PARENT_APP
import TC_TOKEN
import TC_URL
import notification.Parent

class TcProperties private constructor(
    val baseUrl: String,
    val token: String,
    val parent: Parent
) {

    companion object {
        fun get(args: Array<String>): TcProperties {
            fun getValue(key: String): String {
                val i = args.indexOf(key)
                if (i != -1) {
                    val value = args.getOrNull(i + 1)
                        ?: throw IllegalArgumentException("$key is specified but value is missing!")
                    return value
                }
                throw IllegalArgumentException("$key is missing!")
            }

            return TcProperties(
                baseUrl = getValue(TC_URL),
                token = getValue(TC_TOKEN),
                parent = Parent.get(getValue(PARENT_APP))
            )
        }
    }
}