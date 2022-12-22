package com.github.bun133.autoreload

import java.io.File

data class PluginFileEntry(
    val pluginFileName: String,
    val lastUpdated: Long,
)

fun pluginFileEntry(pluginFile: File): PluginFileEntry {
    return PluginFileEntry(
        pluginFileName = pluginFile.name,
        lastUpdated = pluginFile.lastModified(),
    )
}