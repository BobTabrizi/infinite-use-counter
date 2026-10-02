package com.infiniteusecounter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(InfiniteUseCounterConfig.GROUP)
public interface InfiniteUseCounterConfig extends Config
{
	String GROUP = "infiniteusecounter";

	@ConfigItem(
		keyName = "showInfobox",
		name = "Show infobox",
		description = "Show an infobox with the use count of your wielded weapon or staff",
		position = 0
	)
	default boolean showInfobox()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showItemOverlay",
		name = "Show on items",
		description = "Show use counts on tracked items in your inventory and equipment",
		position = 1
	)
	default boolean showItemOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "milestoneInterval",
		name = "Milestone message every",
		description = "Post a chat message every this many uses of an item (0 to disable)",
		position = 2
	)
	default int milestoneInterval()
	{
		return 1000;
	}

	@ConfigItem(
		keyName = "debugLogging",
		name = "Debug logging",
		description = "Log detection details (e.g. unrecognised attack animations) to the client log",
		position = 3
	)
	default boolean debugLogging()
	{
		return false;
	}
}
