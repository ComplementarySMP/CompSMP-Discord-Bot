package xd.arkosammy.compsmpdiscordbot

import dev.kord.common.entity.Snowflake
import dev.kord.core.event.message.MessageCreateEvent
import dev.kordex.core.ExtensibleBot
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory
import xd.arkosammy.compsmpdiscordbot.config.ConfigSettings
import xd.arkosammy.compsmpdiscordbot.config.SettingGroups
import xd.arkosammy.compsmpdiscordbot.extensions.TestExtension
import xd.arkosammy.monkeyconfig.managers.ConfigManager
import xd.arkosammy.monkeyconfig.managers.TomlConfigManager
import xd.arkosammy.monkeyconfig.managers.getSettingValue
import xd.arkosammy.monkeyconfig.settings.NumberSetting
import xd.arkosammy.monkeyconfig.settings.StringSetting

object CompSMPDiscordBot : ModInitializer {

	const val MOD_ID: String = "compsmp-discord-bot"
    val LOGGER = LoggerFactory.getLogger(MOD_ID)
    val CONFIG_MANAGER: ConfigManager = TomlConfigManager(MOD_ID, SettingGroups.settingGroups, ConfigSettings.settingBuilders)

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("Hello Fabric world!")

		runBlocking {

			main()

		}


	}
	suspend fun main() {

		LOGGER.info("suspended fun!")

		val token: String = CONFIG_MANAGER.getSettingValue<String, StringSetting>(ConfigSettings.BOT_TOKEN.settingLocation)

		val bot: ExtensibleBot = ExtensibleBot(token) {
		chatCommands {
			defaultPrefix = "?"
			enabled = true

			prefix { default ->
				val guildIdNum: Long = CONFIG_MANAGER.getSettingValue<Long, NumberSetting<Long>>(ConfigSettings.GUILD_ID.settingLocation)
            	val guildSnowFlake: Snowflake = Snowflake(guildIdNum)
				if (guildId == guildSnowFlake) {
					// For the test server, we use ! as the command prefix
					"!"
				} else {
					// For other servers, we use the configured default prefix
					default
				}
			}
		}

		extensions {
			add(::TestExtension)
		}

		}
		bot.on<MessageCreateEvent> {
             // ignore other bots, even ourselves. We only serve humans here!
             if (message.author?.isBot != false) return@on

             // check if our command is being invoked
             if (message.content != "!ping") return@on

             // all clear, give them the pong!
             message.channel.createMessage("pong!")

		}
		bot.startAsync()

	}
}