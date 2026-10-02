package com.infiniteusecounter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UseType
{
	/** Weapon attacks, detected from the player's attack animation. */
	ATTACK("attacks"),
	/** Spells cast while the item is wielded, detected from Magic XP gains. */
	CAST("casts"),
	/** Teleports, detected from a teleport menu option followed by a location jump. */
	TELEPORT("teleports");

	private final String label;
}
