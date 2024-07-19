package models

import MAIN_BRANCH
import PLAY_RELEASE_BRANCH_PREFIX
import RELEASE_BRANCH_PREFIX

enum class BranchType(val type: String) {
    Main(MAIN_BRANCH),
    Release(RELEASE_BRANCH_PREFIX),
    PlayRelease(PLAY_RELEASE_BRANCH_PREFIX);

    companion object {
        fun get(branchName: String): BranchType {
            entries.forEach {
                if (branchName.startsWith(it.type)) {
                    return it
                }
            }
            val message =
                "`branch` value must be one of these: $MAIN_BRANCH, " +
                        "$RELEASE_BRANCH_PREFIX* or $PLAY_RELEASE_BRANCH_PREFIX*"
            throw IllegalArgumentException(message)
        }
    }
}