package notification

import KEY_SLACK_REGRESSION_WEBHOOK
import KEY_SLACK_RELEASE_WEBHOOK
import network.RequestMethod
import network.Service

class SlackNotifier private constructor(
    val releaseCutWebhook: String? = null,
    val regressionWebhook: String? = null,
    private val service: Service
) {

    companion object {

        fun get(args: Array<String>, service: Service): SlackNotifier {
            fun getWebhook(key: String): String? {
                val i = args.indexOf(key)
                if (i != -1) {
                    val webhook = args.getOrNull(i + 1)
                        ?: throw IllegalArgumentException(
                            "$key is specified but value is missing!."
                        )
                    return webhook
                }
                return null
            }

            return SlackNotifier(
                service = service,
                releaseCutWebhook = getWebhook(KEY_SLACK_RELEASE_WEBHOOK),
                regressionWebhook = getWebhook(KEY_SLACK_REGRESSION_WEBHOOK)
            )
        }
    }

    fun notify(webhook: String?, config: SlackNotificationConfig) {
        webhook?.let {
            service.sendRequest(
                method = RequestMethod.POST,
                networkUrl = it,
                requestBody = config.buildContract()
            )
        }
    }
}