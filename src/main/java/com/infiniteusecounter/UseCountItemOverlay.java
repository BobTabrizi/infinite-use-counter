package com.infiniteusecounter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.components.TextComponent;
import net.runelite.client.util.QuantityFormatter;

/** Draws the use count in the bottom-left corner of tracked items. */
class UseCountItemOverlay extends WidgetItemOverlay
{
	private static final Color TEXT_COLOR = new Color(0, 255, 255);

	private final InfiniteUseCounterPlugin plugin;
	private final InfiniteUseCounterConfig config;
	private final UseCountStore store;

	@Inject
	UseCountItemOverlay(InfiniteUseCounterPlugin plugin, InfiniteUseCounterConfig config, UseCountStore store)
	{
		this.plugin = plugin;
		this.config = config;
		this.store = store;
		showOnInventory();
		showOnEquipment();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		if (!config.showItemOverlay())
		{
			return;
		}

		TrackedItem item = plugin.lookup(itemId);
		if (item == null)
		{
			return;
		}

		Rectangle bounds = widgetItem.getCanvasBounds();
		graphics.setFont(FontManager.getRunescapeSmallFont());
		TextComponent text = new TextComponent();
		text.setPosition(new Point(bounds.x - 1, bounds.y + bounds.height - 1));
		text.setText(QuantityFormatter.quantityToStackSize(store.get(item)));
		text.setColor(TEXT_COLOR);
		text.render(graphics);
	}
}
