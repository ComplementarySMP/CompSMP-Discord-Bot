package xd.arkosammy.compsmpdiscordbot.config

import xd.arkosammy.monkeyconfig.groups.DefaultMutableSettingGroup
import xd.arkosammy.monkeyconfig.groups.MutableSettingGroup

enum class SettingGroups(private val settingGroup: MutableSettingGroup) {
    BOT_SETTINGS(DefaultMutableSettingGroup("bot_settings", registerSettingsAsCommands = false)),
    AUTO_APPROVAL_SETTINGS(DefaultMutableSettingGroup("auto_approval_settings", registerSettingsAsCommands = false));

    val groupName: String
        get() = this.settingGroup.name

    companion object {

        val settingGroups: List<MutableSettingGroup>
            get() = entries.map { e -> e.settingGroup }.toList()

    }

}