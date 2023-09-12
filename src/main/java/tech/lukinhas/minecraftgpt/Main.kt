@file:Suppress("DEPRECATION")

package tech.lukinhas.minecraftgpt

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import tech.lukinhas.minecraftgpt.Utils.OpenAI.chatCompletion
import java.io.IOException

class Main : JavaPlugin() {
    override fun onEnable() {
        saveDefaultConfig()
        val command = getCommand("minecraftgpt")
        command?.setExecutor(this)
        command?.tabCompleter = this
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (command.name.equals("minecraftgpt", ignoreCase = true)) {
            when (args.getOrNull(0)?.toLowerCase()) {
                null -> sender.sendMessage("${ChatColor.GRAY}[MinecraftGPT] ${ChatColor.WHITE}Use /minecraftgpt help to see the available subcommands.")
                "chat" -> handleChatCommand(sender, args)
                "version" -> sender.sendMessage("${ChatColor.GRAY}[MinecraftGPT] ${ChatColor.WHITE}The plugin version is ${ChatColor.DARK_GRAY}${description.version}")
                "reload" -> handleReloadCommand(sender)
                "help" -> showHelp(sender)
                else -> sender.sendMessage("${ChatColor.GRAY}[MinecraftGPT] ${ChatColor.WHITE}Unknown subcommand. Use /minecraftgpt help to see the available subcommands.")
            }
            return true
        }
        return false
    }

    private fun handleChatCommand(sender: CommandSender, args: Array<String>) {
        if (args.size >= 2) {
            val message = listOf("user" to args.drop(1).joinToString(" "))
            val key = config.getString("key")!!
            val model = config.getString("model")!!
            val url = config.getString("url")!!

            Bukkit.getScheduler().runTaskAsynchronously(this) { _: BukkitTask ->
                try {
                    val response = chatCompletion(model, key, message, url)
                    val messageToSend = "${ChatColor.GRAY}[MinecraftGPT] ${ChatColor.WHITE}$response"
                    sender.sendMessage(messageToSend)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
        } else {
            sender.sendMessage("${ChatColor.GRAY}[MinecraftGPT] ${ChatColor.WHITE}Correct usage: /minecraftgpt chat <message>")
        }
    }

    private fun handleReloadCommand(sender: CommandSender) {
        try {
            reloadConfig()
            sender.sendMessage("${ChatColor.GRAY}[MinecraftGPT] ${ChatColor.WHITE}The plugin was reloaded successfully.")
        } catch (e: Exception) {
            sender.sendMessage("${ChatColor.GRAY}[MinecraftGPT] ${ChatColor.WHITE}The plugin was not reloaded because of an error. Check the logs to see the problem.")
        }
    }

    private fun showHelp(sender: CommandSender) {
        sender.sendMessage("${ChatColor.GRAY}[MinecraftGPT] ${ChatColor.WHITE}Available subcommands:")
        sender.sendMessage("${ChatColor.GRAY}/minecraftgpt chat <message>:${ChatColor.WHITE} Sends a request to the NovaAI API and returns the message.")
        sender.sendMessage("${ChatColor.GRAY}/minecraftgpt version:${ChatColor.WHITE} Sends the version of the plugin.")
        sender.sendMessage("${ChatColor.GRAY}/minecraftgpt reload:${ChatColor.WHITE} Reloads the plugin's configuration.")
        sender.sendMessage("${ChatColor.GRAY}/minecraftgpt help:${ChatColor.WHITE} Shows this message.")
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        val completions: MutableList<String> = ArrayList()
        if (command.name.equals("minecraftgpt", ignoreCase = true) || command.name.equals("mcgpt", ignoreCase = true)) {
            if (args.size == 1) {
                completions.addAll(listOf("chat", "version", "reload", "help"))
            }
        }
        return completions
    }
}
