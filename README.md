# Infinite Use Counter

A RuneLite plugin that counts how many times you've used items with unlimited charges.
Each item keeps its own count, saved separately for each account.

| Type | Items | How a use is detected |
|---|---|---|
| Attacks | Bow of Faerdhinen (c), Blade of Saeldor (c) | The weapon's attack animation plays |
| Casts | Elemental, combination and mystic staves, Kodai wand | Magic XP is gained while the staff is wielded |
| Teleports | Eternal glory, eternal slayer ring, elite diary items, royal seed pod, ectophial, max/diary/construction/quest capes, etc. | A teleport option is clicked on the item, then you move more than 10 tiles in one tick within 30 ticks |

Counts are shown:
- in an infobox for your wielded weapon or staff
- in the corner of tracked items in your inventory and equipment
- as a chat message every 1,000 uses (configurable)

The full item list is in `TrackedItem.java`.

## Limitations
- Only uses made while the plugin is running are counted. Earlier usage can't be recovered.
- "Casts" counts every spell that gives Magic XP while the staff is wielded, including spells that don't use the staff's rune.
- Attack animations for the Blade of Saeldor (c) haven't been confirmed in-game yet. Turn on **Debug logging** to log any animation that isn't counted.

## Development
```
./gradlew build       # compile + unit tests
./gradlew runClient   # launch RuneLite with the plugin loaded
```
