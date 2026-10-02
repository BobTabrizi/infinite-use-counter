package com.infiniteusecounter;

import java.awt.Color;
import java.awt.image.BufferedImage;
import lombok.Getter;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.util.QuantityFormatter;

class UseCountInfoBox extends InfoBox
{
	private final UseCountStore store;
	@Getter
	private final TrackedItem item;

	UseCountInfoBox(BufferedImage image, Plugin plugin, UseCountStore store, TrackedItem item)
	{
		super(image, plugin);
		this.store = store;
		this.item = item;
	}

	@Override
	public String getText()
	{
		return QuantityFormatter.quantityToStackSize(store.get(item));
	}

	@Override
	public Color getTextColor()
	{
		return Color.WHITE;
	}

	@Override
	public String getTooltip()
	{
		return item.getDisplayName() + ": " + QuantityFormatter.formatNumber(store.get(item)) + " " + item.getUseType().getLabel();
	}
}
