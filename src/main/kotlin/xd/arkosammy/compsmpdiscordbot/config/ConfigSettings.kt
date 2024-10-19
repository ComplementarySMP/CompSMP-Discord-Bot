package xd.arkosammy.compsmpdiscordbot.config

import xd.arkosammy.monkeyconfig.settings.ConfigSetting
import xd.arkosammy.monkeyconfig.settings.NumberSetting
import xd.arkosammy.monkeyconfig.settings.StringSetting
import xd.arkosammy.monkeyconfig.util.SettingLocation

enum class ConfigSettings(private val builder: ConfigSetting.Builder<*, *, *>) {
    BOT_TOKEN(StringSetting.Builder(SettingLocation(SettingGroups.BOT_SETTINGS.groupName, "token"), defaultValue = "")),
    GUILD_ID(NumberSetting.Builder(SettingLocation(SettingGroups.BOT_SETTINGS.groupName, "guild_id"), defaultValue = 0L).withLowerBound(0)),
    ADMIN_ROLE_ID(NumberSetting.Builder(SettingLocation(SettingGroups.BOT_SETTINGS.groupName, "admin_role_id"), defaultValue = 0L)),

    APPROVAL_CHANNEL_ID(NumberSetting.Builder(SettingLocation(SettingGroups.AUTO_APPROVAL_SETTINGS.groupName, "approval_channel_id"), defaultValue = 0L).withLowerBound(0)),
    APPROVAL_ROLE_ID(NumberSetting.Builder(SettingLocation(SettingGroups.AUTO_APPROVAL_SETTINGS.groupName, "approval_role_id"), defaultValue = 0L).withLowerBound(0)),
    APPROVAL_EMOJI_ID(NumberSetting.Builder(SettingLocation(SettingGroups.AUTO_APPROVAL_SETTINGS.groupName, "approval_emoji_id"), defaultValue = 0L).withLowerBound(0));

    val settingLocation: SettingLocation = builder.settingLocation

    companion object {

        val settingBuilders: List<ConfigSetting.Builder<*, *, *>>
            get() = entries.map { e -> e.builder }.toList()

    }

}