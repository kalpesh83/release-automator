package teamcity

import KEY_BUILD_IDS
import notification.SlackNotifier
import network.RequestMethod
import network.Service
import notification.SlackNotificationConfig

class TcBuildManager(
    private val args: Array<String>,
    private val tcProperties: TcProperties,
    private val service: Service
) {

    fun triggerBuilds(branch: String): List<BuildDetails> {
        return args.fetchBuildIds().mapNotNull {
            trigger(id = it, branch = branch)
        }
    }

    private fun Array<String>.fetchBuildIds(): List<String> {
        val i = indexOf(KEY_BUILD_IDS)
        if (i != -1) {
            val valueString = getOrNull(i + 1)
                ?: throw IllegalArgumentException("$KEY_BUILD_IDS key is specified but value is missing!")
            val ids = valueString.split(",")
            if (ids.isEmpty()) {
                throw IllegalArgumentException("Expected non-empty build_ids value")
            }
            return ids
        }
        return listOf()
    }

    private fun trigger(id: String, branch: String): BuildDetails? {
        val updatedBranchName = if (branch.startsWith("refs/heads/")) branch else "refs/heads/$branch"
        val map = hashMapOf("Authorization" to "Bearer ${tcProperties.token}")
        val requestBody = """
            {
                "branchName": "$updatedBranchName",
         	    "buildType": {
             	    "id": "$id"
         	    }
            }
      """.trimIndent()
        return service.sendRequest(
            method = RequestMethod.POST,
            networkUrl = tcProperties.baseUrl,
            requestBody = requestBody,
            requestProperty = map
        )?.let {
            BuildDetails.parse(it)
        }
    }

}