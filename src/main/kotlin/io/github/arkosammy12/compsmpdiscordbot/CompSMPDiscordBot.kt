package io.github.arkosammy12.compsmpdiscordbot

import dev.kord.common.entity.Snowflake
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kordex.core.ExtensibleBot
import io.github.arkosammy12.compsmpdiscordbot.config.ConfigUtils
import io.github.arkosammy12.compsmpdiscordbot.extensions.ApprovalExtension
import io.github.arkosammy12.compsmpdiscordbot.extensions.ConfigurableChatCommandsExtension
import io.github.arkosammy12.monkeyconfig.base.ConfigManager
import io.github.arkosammy12.monkeyconfig.builders.tomlConfigManager
import io.github.arkosammy12.monkeyconfig.managers.getRawNumberSettingValue
import io.github.arkosammy12.monkeyconfig.managers.getRawStringSettingValue
import io.github.arkosammy12.monkeyutils.registrars.DefaultConfigRegistrar
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object CompSMPDiscordBot : DedicatedServerModInitializer {

	const val MOD_ID: String = "compsmpdiscordbot"
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)
	lateinit var guildSnowFlake: Snowflake
	lateinit var bot: ExtensibleBot
	lateinit var botJob: Job

	val CONFIG_MANAGER: ConfigManager = tomlConfigManager(MOD_ID, FabricLoader.getInstance().configDir.resolve("compsmpdiscordbot.toml")) {
		ConfigUtils.BOT_TOKEN = stringSetting("bot_token", "0") {

		}

		ConfigUtils.GUILD_ID = numberSetting("guild_id", 0L) {

		}
		ConfigUtils.COMPSMP_ADMIN_ROLE_ID = numberSetting("compsmp_admin_role_id", 0L) {

		}

		section("application_approval") {
			ConfigUtils.APPLICATION_CHANNEL_ID = numberSetting("application_channel_id", 0L) {

			}
			ConfigUtils.APPROVAL_ROLE_ID = numberSetting("approval_role_id", 0L) {

			}
			ConfigUtils.APPROVAL_EMOJI_ID = numberSetting("approval_emoji_id", 0L) {

			}
			ConfigUtils.APPROVAL_EMOJI_NAME = stringSetting("approval_emoji_name", "Approved") {

			}

		}
		ConfigUtils.CHAT_COMMANDS = stringMapSection("chat_commands") {
			addDefaultEntry("ping" to "pong")
			onUpdated = {
				runBlocking {
					bot.unloadExtension(ConfigurableChatCommandsExtension.NAME)
					bot.loadExtension(ConfigurableChatCommandsExtension.NAME)
				}
			}
		}

	}

	override fun onInitializeServer() {
		DefaultConfigRegistrar.registerConfigManager(CONFIG_MANAGER)
		ServerLifecycleEvents.SERVER_STARTING.register(::onServerStarting)
		ServerLifecycleEvents.SERVER_STOPPING.register(::onServerStopping)
		val guildId: Long = CONFIG_MANAGER.getRawNumberSettingValue(ConfigUtils.GUILD_ID)!!
		guildSnowFlake = Snowflake(guildId)
		val token: String = CONFIG_MANAGER.getRawStringSettingValue(ConfigUtils.BOT_TOKEN)!!
		bot = createBot(token)
	}

	private fun onServerStarting(server: MinecraftServer) {
		botJob = bot.startAsync()
	}

	private fun onServerStopping(server: MinecraftServer) {
		runBlocking {
			bot.close()
			botJob.cancelAndJoin()
		}

	}

	fun createBot(token: String): ExtensibleBot {
		return runBlocking {
			ExtensibleBot(token) {
				chatCommands {
					enabled = true
					defaultPrefix = "!"
				}
				extensions {
					add(::ApprovalExtension)
					add(::ConfigurableChatCommandsExtension)
				}
				about {

				}
				hooks {

				}
				@OptIn(PrivilegedIntent::class)
				intents {
					+Intent.GuildMembers
					+Intent.GuildMessageReactions
					+Intent.MessageContent
				}
				cache {
				}
			}
		}
	}

}