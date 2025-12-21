package com.spectrum.phoenix.logic.model

data class UpdateInfo(
    val latestVersion: String = "",
    val apkUrl: String = "",
    val releaseNotes: String = "",
    val forceUpdate: Boolean = false
)
