package com.infiniteusecounter;

import com.google.inject.Provides;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.QuantityFormatter;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Infinite Use Counter",
	description = "Counts attacks, casts and teleports made with items that have unlimited charges",
	tags = {"charges", "counter", "bowfa", "corrupted", "teleport", "eternal"}
)
public class InfiniteUseCounterPlugin extends Plugin
{
	/** How long after clicking a teleport option we wait for the teleport to happen. */
	private static final int TELEPORT_WINDOW_TICKS = 30;
	/** A single-tick move further than this many tiles is treated as a teleport. */
	private static final int TELEPORT_MIN_DISTANCE = 10;
	/** Menu options on tracked items that never teleport. */
	private static final Set<String> NON_TELEPORT_OPTIONS = Set.of(
		"wear", "wield", "remove", "drop", "examine", "use", "check", "destroy", "cancel", "take", "deposit", "withdraw"
	);

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private InfiniteUseCounterConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private UseCountStore store;

	@Inject
	private UseCountItemOverlay itemOverlay;

	private final Map<Integer, Optional<TrackedItem>> itemIdCache = new HashMap<>();

	private TrackedItem wielded;
	private int wieldedItemId = -1;
	private UseCountInfoBox infoBox;

	private int lastAttackTick = -1;
	private int lastMagicXp = -1;

	private TrackedItem pendingTeleport;
	private int pendingTeleportExpiry;
	private WorldPoint lastLocation;

	@Provides
	InfiniteUseCounterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(InfiniteUseCounterConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(itemOverlay);
		clientThread.invokeLater(this::updateWielded);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(itemOverlay);
		removeInfoBox();
		wielded = null;
		wieldedItemId = -1;
		lastAttackTick = -1;
		lastMagicXp = -1;
		pendingTeleport = null;
		lastLocation = null;
		itemIdCache.clear();
		store.clearCache();
	}

	/** Returns the tracked item for an item id, or null. Must be called on the client thread. */
	TrackedItem lookup(int itemId)
	{
		if (itemId <= 0)
		{
			return null;
		}
		return itemIdCache.computeIfAbsent(itemId, id ->
			Optional.ofNullable(TrackedItem.fromName(itemManager.getItemComposition(itemManager.canonicalize(id)).getName())))
			.orElse(null);
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == InventoryID.WORN)
		{
			updateWielded();
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		Player player = client.getLocalPlayer();
		if (event.getActor() != player || wielded == null || wielded.getUseType() != UseType.ATTACK)
		{
			return;
		}

		int animation = player.getAnimation();
		if (animation == -1)
		{
			return;
		}

		if (!wielded.getAttackAnimations().contains(animation))
		{
			debug("Ignoring animation {} while wielding {}", animation, wielded.getDisplayName());
			return;
		}

		// Guard against the same attack being reported twice in one tick
		int tick = client.getTickCount();
		if (tick == lastAttackTick)
		{
			return;
		}
		lastAttackTick = tick;
		recordUse(wielded);
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (event.getSkill() != Skill.MAGIC)
		{
			return;
		}

		int previousXp = lastMagicXp;
		lastMagicXp = event.getXp();
		// The first update after login just reports the current total
		if (previousXp < 0 || event.getXp() <= previousXp)
		{
			return;
		}

		if (wielded != null && wielded.getUseType() == UseType.CAST)
		{
			recordUse(wielded);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		String option = Text.removeTags(event.getMenuOption()).toLowerCase();
		if (NON_TELEPORT_OPTIONS.contains(option))
		{
			return;
		}

		TrackedItem item = lookup(event.getItemId());
		if (item == null)
		{
			item = TrackedItem.fromName(Text.removeTags(event.getMenuTarget()));
		}
		if (item == null || item.getUseType() != UseType.TELEPORT)
		{
			return;
		}

		pendingTeleport = item;
		pendingTeleportExpiry = client.getTickCount() + TELEPORT_WINDOW_TICKS;
		debug("Waiting for teleport from {} ({})", item.getDisplayName(), option);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return;
		}

		WorldPoint location = player.getWorldLocation();
		if (pendingTeleport != null)
		{
			if (client.getTickCount() > pendingTeleportExpiry)
			{
				debug("Teleport from {} timed out", pendingTeleport.getDisplayName());
				pendingTeleport = null;
			}
			else if (lastLocation != null && location.distanceTo2D(lastLocation) > TELEPORT_MIN_DISTANCE)
			{
				recordUse(pendingTeleport);
				pendingTeleport = null;
			}
		}
		lastLocation = location;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING)
		{
			lastMagicXp = -1;
			pendingTeleport = null;
			lastLocation = null;
		}
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		store.clearCache();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (InfiniteUseCounterConfig.GROUP.equals(event.getGroup()))
		{
			clientThread.invokeLater(this::updateInfoBox);
		}
	}

	private void updateWielded()
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
		Item weapon = equipment == null ? null : equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
		wieldedItemId = weapon == null ? -1 : weapon.getId();
		wielded = lookup(wieldedItemId);
		updateInfoBox();
	}

	private void updateInfoBox()
	{
		boolean show = config.showInfobox() && wielded != null && wielded.getUseType() != UseType.TELEPORT;
		if (infoBox != null && (!show || infoBox.getItem() != wielded))
		{
			removeInfoBox();
		}
		if (show && infoBox == null)
		{
			infoBox = new UseCountInfoBox(itemManager.getImage(wieldedItemId), this, store, wielded);
			infoBoxManager.addInfoBox(infoBox);
		}
	}

	private void removeInfoBox()
	{
		if (infoBox != null)
		{
			infoBoxManager.removeInfoBox(infoBox);
			infoBox = null;
		}
	}

	private void recordUse(TrackedItem item)
	{
		long count = store.increment(item);
		debug("{} {} -> {}", item.getDisplayName(), item.getUseType().getLabel(), count);

		int interval = config.milestoneInterval();
		if (interval > 0 && count % interval == 0)
		{
			String message = new ChatMessageBuilder()
				.append(ChatColorType.HIGHLIGHT)
				.append("Your " + item.getDisplayName() + " has made " + QuantityFormatter.formatNumber(count) + " " + item.getUseType().getLabel() + ".")
				.build();
			chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.GAMEMESSAGE)
				.runeLiteFormattedMessage(message)
				.build());
		}
	}

	private void debug(String format, Object... args)
	{
		if (config.debugLogging())
		{
			log.info(format, args);
		}
	}
}
