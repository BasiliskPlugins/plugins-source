package net.autopumper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("autoPumper")
public interface AutoPumperConfig extends Config {
    @ConfigSection(
            keyName = "config",
            name = "Config",
            description = "",
            position = 0
    )
    String config = "Config";

    @ConfigItem(
            keyName = "soloPump",
            name = "Enable Solo Pumping?",
            description = "This will automate the coke filling and refuelling",
            section = config,
            position = 1
    )
    default boolean soloPump()
    {
        return true;
    }
}
