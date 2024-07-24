package models

import ROLLOUT_PERCENT
import RolloutType

data class ReleaseProperties(
    val isNewRelease: Boolean,
    val rolloutType: RolloutType,
    val rolloutPercent: String
) {

    companion object {
        fun get(args: Array<String>, gitProperties: GitProperties): ReleaseProperties {
            val i = args.indexOf(ROLLOUT_PERCENT)
            var percent = "-"
            if (i != -1) {
                percent = args.getOrNull(i + 1) ?: "-"
            }
            return when (gitProperties.branchType) {
                BranchType.Main -> {
                    ReleaseProperties(
                        isNewRelease = true,
                        rolloutType = RolloutType.BETA_ROLLOUT,
                        rolloutPercent = percent
                    )
                }

                BranchType.Release -> {
                    ReleaseProperties(
                        isNewRelease = false,
                        rolloutType = RolloutType.BETA_ROLLOUT,
                        rolloutPercent = percent
                    )
                }

                BranchType.PlayRelease -> {
                    ReleaseProperties(
                        isNewRelease = false,
                        rolloutType = RolloutType.PROD_ROLLOUT,
                        rolloutPercent = percent
                    )
                }
            }
        }
    }
}