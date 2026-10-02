package com.infiniteusecounter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class TrackedItemTest
{
	@Test
	public void matchesNamesCaseInsensitively()
	{
		assertEquals(TrackedItem.BOW_OF_FAERDHINEN_C, TrackedItem.fromName("Bow of Faerdhinen (c)"));
		assertEquals(TrackedItem.BOW_OF_FAERDHINEN_C, TrackedItem.fromName("BOW OF FAERDHINEN (C) "));
		assertEquals(TrackedItem.LAVA_STAFF, TrackedItem.fromName("Mystic lava staff (or)"));
	}

	@Test
	public void ignoresChargedVersions()
	{
		assertNull(TrackedItem.fromName("Bow of faerdhinen"));
		assertNull(TrackedItem.fromName("Amulet of glory(4)"));
		assertNull(TrackedItem.fromName(null));
	}

	@Test
	public void eachItemHasItsOwnConfigKey()
	{
		Set<String> keys = new HashSet<>();
		for (TrackedItem item : TrackedItem.values())
		{
			assertTrue("duplicate key " + item.configKey(), keys.add(item.configKey()));
		}
	}

	@Test
	public void onlyAttackItemsHaveAnimations()
	{
		for (TrackedItem item : TrackedItem.values())
		{
			assertEquals(item.name(), item.getUseType() == UseType.ATTACK, !item.getAttackAnimations().isEmpty());
		}
		assertFalse(TrackedItem.BOW_OF_FAERDHINEN_C.getAttackAnimations().isEmpty());
	}
}
