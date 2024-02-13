package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("example")
public interface ExampleConfig extends Config
{
	@ConfigItem(
		position = 1,
		keyName = "templeosrs",
		name = "TempleOSRS",
		description = "Automatically updates your stats on templeosrs.com when you log out"
	)
	default boolean templeosrs()
	{
		return false;
	}
}
