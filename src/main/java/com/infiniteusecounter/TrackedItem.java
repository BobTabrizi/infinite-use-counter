package com.infiniteusecounter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import net.runelite.api.gameval.AnimationID;

/**
 * Items with unlimited charges whose uses are counted. Each entry keeps its own count.
 * Items are matched by in-game name so recolours/ornament variants that share a name share a count.
 */
@Getter
public enum TrackedItem
{
	// Weapons
	BOW_OF_FAERDHINEN_C("Bow of Faerdhinen (c)", UseType.ATTACK,
		Set.of(AnimationID.HUMAN_BOW),
		"Bow of Faerdhinen (c)"),
	BLADE_OF_SAELDOR_C("Blade of Saeldor (c)", UseType.ATTACK,
		Set.of(AnimationID.HUMAN_SWORD_SLASH, AnimationID.HUMAN_SWORD_STAB),
		"Blade of Saeldor (c)"),

	// Infinite-rune staves
	STAFF_OF_AIR("Staff of air", UseType.CAST, "Staff of air", "Air battlestaff", "Mystic air staff"),
	STAFF_OF_WATER("Staff of water", UseType.CAST, "Staff of water", "Water battlestaff", "Mystic water staff"),
	STAFF_OF_EARTH("Staff of earth", UseType.CAST, "Staff of earth", "Earth battlestaff", "Mystic earth staff"),
	STAFF_OF_FIRE("Staff of fire", UseType.CAST, "Staff of fire", "Fire battlestaff", "Mystic fire staff"),
	LAVA_STAFF("Lava battlestaff", UseType.CAST, "Lava battlestaff", "Mystic lava staff", "Lava battlestaff (or)", "Mystic lava staff (or)"),
	MUD_STAFF("Mud battlestaff", UseType.CAST, "Mud battlestaff", "Mystic mud staff"),
	STEAM_STAFF("Steam battlestaff", UseType.CAST, "Steam battlestaff", "Mystic steam staff", "Steam battlestaff (or)", "Mystic steam staff (or)"),
	SMOKE_STAFF("Smoke battlestaff", UseType.CAST, "Smoke battlestaff", "Mystic smoke staff"),
	MIST_STAFF("Mist battlestaff", UseType.CAST, "Mist battlestaff", "Mystic mist staff"),
	DUST_STAFF("Dust battlestaff", UseType.CAST, "Dust battlestaff", "Mystic dust staff"),
	KODAI_WAND("Kodai wand", UseType.CAST, "Kodai wand"),

	// Teleport items
	ETERNAL_GLORY("Amulet of eternal glory", UseType.TELEPORT, "Amulet of eternal glory"),
	ETERNAL_SLAYER_RING("Slayer ring (eternal)", UseType.TELEPORT, "Slayer ring (eternal)"),
	ETERNAL_TELEPORT_CRYSTAL("Eternal teleport crystal", UseType.TELEPORT, "Eternal teleport crystal"),
	ROYAL_SEED_POD("Royal seed pod", UseType.TELEPORT, "Royal seed pod"),
	ECTOPHIAL("Ectophial", UseType.TELEPORT, "Ectophial"),
	DRAKANS_MEDALLION("Drakan's medallion", UseType.TELEPORT, "Drakan's medallion"),
	CAMULET("Camulet", UseType.TELEPORT, "Camulet"),
	ARDOUGNE_CLOAK_4("Ardougne cloak 4", UseType.TELEPORT, "Ardougne cloak 4"),
	DESERT_AMULET_4("Desert amulet 4", UseType.TELEPORT, "Desert amulet 4"),
	FREMENNIK_SEA_BOOTS_4("Fremennik sea boots 4", UseType.TELEPORT, "Fremennik sea boots 4"),
	KANDARIN_HEADGEAR_4("Kandarin headgear 4", UseType.TELEPORT, "Kandarin headgear 4"),
	EXPLORERS_RING_4("Explorer's ring 4", UseType.TELEPORT, "Explorer's ring 4"),
	KARAMJA_GLOVES_4("Karamja gloves 4", UseType.TELEPORT, "Karamja gloves 4"),
	MORYTANIA_LEGS_4("Morytania legs 4", UseType.TELEPORT, "Morytania legs 4"),
	WILDERNESS_SWORD_4("Wilderness sword 4", UseType.TELEPORT, "Wilderness sword 4"),
	RADAS_BLESSING_4("Rada's blessing 4", UseType.TELEPORT, "Rada's blessing 4"),
	WESTERN_BANNER_4("Western banner 4", UseType.TELEPORT, "Western banner 4"),
	MAX_CAPE("Max cape", UseType.TELEPORT, "Max cape"),
	ACHIEVEMENT_DIARY_CAPE("Achievement diary cape", UseType.TELEPORT, "Achievement diary cape", "Achievement diary cape (t)"),
	CONSTRUCTION_CAPE("Construction cape", UseType.TELEPORT, "Construct. cape", "Construct. cape(t)"),
	QUEST_POINT_CAPE("Quest point cape", UseType.TELEPORT, "Quest point cape", "Quest point cape (t)");

	private static final Map<String, TrackedItem> BY_NAME = new HashMap<>();

	static
	{
		for (TrackedItem item : values())
		{
			for (String name : item.itemNames)
			{
				BY_NAME.put(normalize(name), item);
			}
		}
	}

	private final String displayName;
	private final UseType useType;
	/** Animations that count as an attack; only used by {@link UseType#ATTACK} items. */
	private final Set<Integer> attackAnimations;
	private final String[] itemNames;

	TrackedItem(String displayName, UseType useType, String... itemNames)
	{
		this(displayName, useType, Collections.emptySet(), itemNames);
	}

	TrackedItem(String displayName, UseType useType, Set<Integer> attackAnimations, String... itemNames)
	{
		this.displayName = displayName;
		this.useType = useType;
		this.attackAnimations = attackAnimations;
		this.itemNames = itemNames;
	}

	public static TrackedItem fromName(String name)
	{
		return name == null ? null : BY_NAME.get(normalize(name));
	}

	/** Config key the count is stored under. Never rename these once released. */
	public String configKey()
	{
		return "count_" + name().toLowerCase(Locale.ROOT);
	}

	private static String normalize(String name)
	{
		return name.trim().toLowerCase(Locale.ROOT);
	}
}
