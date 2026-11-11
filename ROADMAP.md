# Development Roadmap

Welcome to the **Create: Mob Grinding** roadmap! 

If you're a developer wanting to contribute to the mod, feel free to pick any open task from the checklists below. To contribute, simply fork the repository, implement your feature or fix, and open a Pull Request.

💡 **Have a great idea?** If you are a player and have suggestions for future additions, please open an Issue on our GitHub page and label it with the `enhancement` tag!

---

## Version 1.5.0 (Planned)
Features planned for the next update:

- [x] **Stackable Spawner Chunks:** The Rotational Mob Spawner accepts up to 8 chunks of the same mob (configurable). Each chunk spawns one more mob per cycle (8 chunks work like 8 spawners), at a slightly lower stress cost than separate spawners. Replaces the need for Speed and Quantity upgrades.
- [x] **Automation Support:** Insert and extract Spawner Chunks with Funnels and Mechanical Arms.
- [x] **Comparator Output:** The spawner outputs its spawn progress, the grinder how full its Vacuum inventory is.
- [x] **Config Overhaul:** Expose the hardcoded values (mob cap, stress per tier, extraction health threshold, chunk stacking...) and reorganize the config into clear sections.
- [x] **Ponder Scenes:** Ponders for the Chunk Bag, Fan Filter Upgrade, Display Link support and all the upgrades.
- [x] **Grinder Display Link:** Show the grinder's kill counter on Display Boards.
- [x] **Wandering Trader Trade:** The Wandering Trader can sell a Broken Spawner Chunk for 32 Emeralds (single use).
- [ ] **1.20.1 Port:** Port the mod to Minecraft 1.20.1 (Forge) on a new `mc1.20.1/dev` branch. Last step of the update.

## Completed
Features already released:

- [x] **Mob Tiers for Other Mods:** Built-in tiers for Ars Nouveau, Twilight Forest and The Aether mobs, plus datapack/KubeJS docs in the README.
- [x] **Advanced Fan:** A Fan Filter Upgrade for Create's Encased Fan to ignore Items, Players or Mobs (mode cycled with a Wrench).
- [x] **Display Link Support:** Show the spawner's progress and mob on Display Boards.
- [x] **Rotational Spawner Texture Revamp:** Redesign and improve the block model and texture of the Rotational Mob Spawner.
- [x] **Spawner Chunk Bag:** A portable bag dedicated to storing Spawner Chunks.
  - [x] **Inventory Size:** Internal capacity equal to a standard vanilla chest (27 slots).
  - [x] **Quick Swap:** Right-clicking the bag on a Rotational Spawner opens its inventory. Clicking a chunk inside instantly swaps it with the Spawner's current chunk (or sets it if empty).

## Known Bugs
Issues that have been reported and need fixing:

- [ ] *(No major bugs reported currently! Found one? Open an issue!)*
