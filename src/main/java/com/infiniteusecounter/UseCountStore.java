package com.infiniteusecounter;

import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;

/**
 * Per-account, per-item use counts, persisted in the RuneScape profile config so each
 * account keeps its own totals.
 */
@Singleton
public class UseCountStore
{
	private final ConfigManager configManager;
	private final Map<TrackedItem, Long> cache = new EnumMap<>(TrackedItem.class);

	@Inject
	UseCountStore(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	public long get(TrackedItem item)
	{
		return cache.computeIfAbsent(item, this::load);
	}

	public long increment(TrackedItem item)
	{
		long count = get(item) + 1;
		cache.put(item, count);
		configManager.setRSProfileConfiguration(InfiniteUseCounterConfig.GROUP, item.configKey(), count);
		return count;
	}

	/** Drop cached values, e.g. after switching accounts. */
	public void clearCache()
	{
		cache.clear();
	}

	private long load(TrackedItem item)
	{
		Long count = configManager.getRSProfileConfiguration(InfiniteUseCounterConfig.GROUP, item.configKey(), Long.class);
		return count == null ? 0 : count;
	}
}
