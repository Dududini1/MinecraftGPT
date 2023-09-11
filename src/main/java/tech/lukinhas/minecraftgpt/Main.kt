package tech.lukinhas.minecraftgpt

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import tech.lukinhas.minecraftgpt.Utils.OpenAI.chatCompletion
import java.io.IOException
import java.util.*

class Main : JavaPlugin() {
    override fun onEnable() {
        saveDefaultConfig()
        getCommand("minecraftgpt")?.setExecutor(this)
        getCommand("minecraftgpt")?.tabCompleter = this
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (command.name.equals("minecraftgpt", ignoreCase = true)) {
            if (args.isEmpty()) {
                sender.sendMessage(ChatColor.GRAY.toString() + "[MinecraftGPT] " + ChatColor.WHITE + "Use /minecraftgpt help to see the available subcommands.")
            } else if (args[0].equals("chat", ignoreCase = true)) {
                if (args.size >= 2) {
                    val messageRaw = StringBuilder()
                    for (i in 1 until args.size) {
                        messageRaw.append(args[i]).append(" ")
                    }
                    val message = listOf(
                            "user" to messageRaw.toString()
                    )

                    val key = getConfig().getString("key")!!
                    val model = getConfig().getString("model")!!
                    val url = getConfig().getString("url")!!

                    Bukkit.getScheduler().runTaskAsynchronously(this, Runnable {
                        try {
                            val response = chatCompletion(model, key, message, url)
                            val messageToSend = ChatColor.GRAY.toString() + "[MinecraftGPT] " + ChatColor.WHITE + response
                            if (sender is Player) {
                                sender.sendMessage(messageToSend)
                            } else {
                                sender.sendMessage(messageToSend)
                            }
                        } catch (e: IOException) {
                            throw RuntimeException(e)
                        }
                    })
                } else {
                    sender.sendMessage(ChatColor.GRAY.toString() + "[MinecraftGPT] " + ChatColor.WHITE + "Correct usage: /minecraftgpt chat <message>")
                }
            } else if (args[0].equals("version", ignoreCase = true)) {
                sender.sendMessage(ChatColor.GRAY.toString() + "[MinecraftGPT] " + ChatColor.WHITE + "The plugin version is " + ChatColor.DARK_GRAY + description.version)
            } else if (args[0].equals("reload", ignoreCase = true)) {
                try {
                    reloadConfig()
                    sender.sendMessage(ChatColor.GRAY.toString() + "[MinecraftGPT] " + ChatColor.WHITE + "The plugin was reloaded successfully.")
                } catch (e: Exception) {
                    sender.sendMessage(ChatColor.GRAY.toString() + "[MinecraftGPT] " + ChatColor.WHITE + "The plugin was not reloaded because of an error. Check the logs to see the problem.")
                }
            } else if (args[0].equals("help", ignoreCase = true)) {
                @Suppress("DEPRECATION")
                sender.sendMessage(ChatColor.GRAY.toString() + "[MinecraftGPT] " + ChatColor.WHITE + "Available subcommands:")
                sender.sendMessage(ChatColor.GRAY.toString() + "/minecraftgpt chat <message>:" + ChatColor.WHITE + " Sends a request to the NovaAI API and returns the message.")
                sender.sendMessage(ChatColor.GRAY.toString() + "/minecraftgpt version:" + ChatColor.WHITE + " Sends the version of the plugin.")
                sender.sendMessage(ChatColor.GRAY.toString() + "/minecraftgpt reload:" + ChatColor.WHITE + " Reloads the plugin's configuration.")
                sender.sendMessage(ChatColor.GRAY.toString() + "/minecraftgpt help:" + ChatColor.WHITE + " Shows this message.")
            } else {
                sender.sendMessage(ChatColor.GRAY.toString() + "[MinecraftGPT] " + ChatColor.WHITE + "Unknown subcommand. Use /minecraftgpt help to see the available subcommands.")
            }
            return true
        }
        return false
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        val completions: MutableList<String> = ArrayList()
        if (command.name.equals("minecraftgpt", ignoreCase = true)) {
            if (args.size == 1) {
                completions.add("chat")
                completions.add("version")
                completions.add("reload")
                completions.add("help")
            }
        } else if (command.name.equals("mcgpt", ignoreCase = true)) {
            if (args.size == 1) {
                completions.add("chat")
                completions.add("version")
                completions.add("reload")
                completions.add("help")
            }
        }
        return completions
    }
}
