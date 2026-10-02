package com.infiniteusecounter;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/** Launches a RuneLite client with this plugin loaded. Run via ./gradlew runClient or from your IDE. */
public class InfiniteUseCounterPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(InfiniteUseCounterPlugin.class);
		RuneLite.main(args);
	}
}
