package org.arcanaforge.app.core.datastore

data class UserSettings(
    val themeSetting: ThemeSetting = ThemeSetting.System,
    val defaultReversalsEnabled: Boolean = true,
    val defaultRevealMode: String = "one_by_one",
)
