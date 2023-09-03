package net.autoflicker;

import net.runelite.client.config.*;

@ConfigGroup("autoflicker")
public interface AutoFlickerConfig extends Config {
    @ConfigSection(
            name = "Key binding",
            description = "",
            position = 0
    )
    String config = "Key binding";

    @ConfigItem(
            keyName = "keybinding",
            name = "Key Binding",
            description = "",
            position = 1,
            section = config
    )
    default Keybind keyBinding() {
        return Keybind.NOT_SET;
    }
}
