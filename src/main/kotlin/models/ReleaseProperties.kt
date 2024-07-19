package models

import RolloutType

data class ReleaseProperties(
    val isNewRelease: Boolean,
    val rolloutType: RolloutType,
) {

    companion object {
        fun get(gitProperties: GitProperties): ReleaseProperties {
            return when (gitProperties.branchType) {
                BranchType.Main -> {
                    ReleaseProperties(
                        isNewRelease = true,
                        rolloutType = RolloutType.BETA_ROLLOUT
                    )
                }

                BranchType.Release -> {
                    ReleaseProperties(
                        isNewRelease = false,
                        rolloutType = RolloutType.BETA_ROLLOUT
                    )
                }

                BranchType.PlayRelease -> {
                    ReleaseProperties(
                        isNewRelease = false,
                        rolloutType = RolloutType.PROD_ROLLOUT
                    )
                }
            }
        }
    }
}