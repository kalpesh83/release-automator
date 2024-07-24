package models

import RolloutType
import notification.Parent
import java.io.File
import java.io.FileInputStream
import java.nio.file.NoSuchFileException
import java.util.Calendar
import java.util.Properties

data class VersionProperties(
    val versionName: String,
    val versionCode: Long,
    val major: String,
    val minor: String,
    val patch: String
) {
    companion object {
        private const val VERSION_FILE_NAME = "version.properties"
        private const val VERSION_CODE = "VERSION_CODE"
        private const val VERSION_NAME = "VERSION_NAME"
        private const val ROLL_OUT_PERCENT = "ROLL_OUT_PERCENT"
        private const val ROLL_OUT_ENV = "ROLL_OUT_ENV"
        private const val IS_FIRST_RELEASE = "IS_FIRST_RELEASE"

        fun get(): VersionProperties {
            val file = File(VERSION_FILE_NAME)
            if (file.exists().not()) {
                throw NoSuchFileException("$VERSION_FILE_NAME file not found.")
            }
            val versionProps = Properties().apply {
                load(FileInputStream(file))
            }
            val versionName = versionProps[VERSION_NAME].toString()
            val versionCode = versionProps[VERSION_CODE].toString().toLong()
            val split = versionName.split(".")
            return VersionProperties(
                versionName = versionName,
                versionCode = versionCode,
                major = split[0],
                minor = split[1],
                patch = split[2]
            )
        }
    }

    fun increment(forInitialRelease: Boolean): VersionProperties {
        return if (forInitialRelease) {
            // For first release, we need to update the release number as well as year(if changed)
            // For eg. if previous version was 2024.10.0 then the new version should be 2024.11.0
            val year = Calendar.getInstance().get(Calendar.YEAR)
            val releaseYear = versionName.split(".")[0].toInt()
            val isNewYear = year != releaseYear

            val releaseNumber = if (isNewYear) {
                1
            } else {
                versionName.split(".")[1].toInt() + 1
            }
            // For version number, we are following 242200 convention.

            val newVersionCode = 10000L * (year - 2000) + (releaseNumber * 100)
            val newVersionName = "${year}.${releaseNumber}.0"
            val split = newVersionName.split(".")
            VersionProperties(
                versionName = newVersionName,
                versionCode = newVersionCode,
                major = split[0],
                minor = split[1],
                patch = split[2]
            )
        } else {
            val newVersionName = versionName.split(".").let {
                "${it[0]}.${it[1]}.${it[2].toInt() + 1}"
            }
            val newSplit = newVersionName.split(".")
            VersionProperties(
                versionName = newVersionName,
                versionCode = versionCode + 1,
                major = newSplit[0],
                minor = newSplit[1],
                patch = newSplit[2]
            )
        }
    }

    fun updatePropertiesFile(
        isFirstRelease: Boolean,
        branchType: BranchType,
        rolloutType: RolloutType,
        parent: Parent,
        rolloutPercent: String
    ) {
        val sb = StringBuilder()
        val p = Properties()
        p.load(FileInputStream(VERSION_FILE_NAME))
        val valueCount = p.count()
        val isFirstBeta = isFirstRelease && rolloutType == RolloutType.BETA_ROLLOUT && branchType == BranchType.Release

        p.entries.forEachIndexed { index, it ->
            when (it.key) {
                VERSION_CODE -> sb.append("${it.key}=${versionCode}")
                VERSION_NAME -> sb.append("${it.key}=${versionName}")
                ROLL_OUT_PERCENT -> sb.append("${it.key}=${rolloutPercent}")
                ROLL_OUT_ENV -> sb.append("${it.key}=${rolloutType.type}")
                IS_FIRST_RELEASE -> sb.append("${it.key}=$isFirstRelease")
                else -> sb.append("${it.key}=${it.value}")
            }
            if (index != valueCount - 1) {
                sb.append("\n")
            }
        }
        if (parent == Parent.ShareChat && isFirstBeta) {
            sb.append("\n")
            sb.append("${ROLL_OUT_PERCENT}=${rolloutPercent}")
            sb.append("${ROLL_OUT_ENV}=${rolloutType.type}")
            sb.append("${IS_FIRST_RELEASE}=true")
        }
        File(VERSION_FILE_NAME).writeText(sb.toString())
    }

    fun buildMessage() = "Update versionName to $versionName and versionCode to $versionCode"

}