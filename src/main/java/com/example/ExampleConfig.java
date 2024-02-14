package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("example")
public interface ExampleConfig extends Config
{
	@ConfigSection(
		name = "General",
		description = "General settings for event participants.",
		position = 0
	)
	String settings = "settings";
	@ConfigSection(
		name = "Configuration",
		description = "Admin settings for event organizers.",
		position = 2,
		closedByDefault = true
	)
	String config = "config";

	@ConfigItem(
		position = 1,
		keyName = "templeosrs",
		name = "Update TempleOSRS",
		description = "Toggle automatic updates to templeosrs.com on log-out.",
		section = settings
	)
	default boolean templeosrs()
	{
		return false;
	}

	@ConfigItem(
		position = 1,
		keyName = "groupid",
		name = "Group Id",
		description = "They ID of the TempleOSRS Group.",
		secret = true,
		section = config
	)
	default int groupid()
	{
		return 0;
	}

	@ConfigItem(
		position = 2,
		keyName = "groupkey",
		name = "Group Validation Key",
		description = "The required key to edit a TempleOSRS Group.",
		secret = true,
		section = config
	)
	default String groupkey()
	{
		return "";
	}
}
