package com.github.bun133.autoreload

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

const val IntervalSec = 1L

class Autoreload : JavaPlugin() {
    private val cachedEntries = mutableListOf<PluginFileEntry>()
    override fun onEnable() {
        // Plugin startup logic
        pluginEntries()?.let { cachedEntries.addAll(it) }
        server.scheduler.runTaskTimer(this, Runnable {
            checkPlugins()
        }, 10, 20 * IntervalSec)
    }


    override fun onDisable() {
    }

    private fun checkPlugins() {
        val entries = pluginEntries()?.toMutableList()
        val toReload = mutableListOf<PluginFileEntry>()

        entries?.forEach {
            val e = getMatchedPluginEntry(it.pluginFileName)
            if (e != null) {
                // Check Last Modified Time
                if (it.lastUpdated != e.lastUpdated) {
                    // To Reload
                    toReload.add(it)
                }
            } else {
                // This plugin is now Added!
                toReload.add(it)
            }
        }

        toReload.forEach {
            try {
                // Reload Plugin File
                val plugin = server.pluginManager.loadPlugin(File(server.pluginsFolder.absolutePath, it.pluginFileName))
                if (plugin != null) {
                    Bukkit.broadcast(
                        Component.text("[AutoReload] ${it.pluginFileName}を再読み込みしました").color(NamedTextColor.BLUE)
                    )
                } else {
                    // Failed To Load
                    Bukkit.broadcast(
                        Component.text("[AutoReload] ${it.pluginFileName}の読み込みに失敗しました").color(NamedTextColor.RED)
                    )
                }
            } catch (e: Exception) {
                // Failed To Load
                Bukkit.broadcast(
                    Component.text("[AutoReload] ${it.pluginFileName}の読み込みに失敗しました").color(NamedTextColor.RED)
                )
                println(e)
                entries?.remove(it)
            }
        }

        // cache
        cachedEntries.clear()
        entries?.let { cachedEntries.addAll(it) }
    }

    private fun pluginEntries(): List<PluginFileEntry>? {
        val files = server.pluginsFolder.listFiles { _, _ -> true }?.filter { it.extension == "jar" }
        return files?.map { pluginFileEntry(it) }
    }

    private fun getMatchedPluginEntry(fileName: String): PluginFileEntry? {
        return cachedEntries.find { it.pluginFileName == fileName }
    }
}