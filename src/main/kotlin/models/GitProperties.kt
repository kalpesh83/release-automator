package models

import BRANCH

class GitProperties private constructor(
    val origin: String,
    val branch: String,
    val branchType: BranchType
) {
    companion object {
        private const val REMOTE_URL = "remote"

        fun get(args: Array<String>): GitProperties {
            val origin = args.getOrigin()
            val branch = args.getBranch()
            println("Specified branch: $branch")
            val branchType = BranchType.get(branch)
            return GitProperties(origin = origin, branch = branch, branchType = branchType)
        }

        private fun Array<String>.getOrigin(): String {
            val i = indexOf(REMOTE_URL)
            if (i != -1) {
                return getOrNull(i + 1) ?: throw IllegalArgumentException("Invalid remote origin")
            }
            throw IllegalArgumentException("`remote origin` is expected")
        }

        private fun Array<String>.getBranch(): String {
            val i = indexOf(BRANCH)
            if (i != -1) {
                return getOrNull(i + 1) ?: throw IllegalArgumentException("`branch` value is missing.")
            }
            throw IllegalArgumentException("`branch` is expected.")
        }
    }
}