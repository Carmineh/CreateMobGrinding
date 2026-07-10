# Changelog 1.2

## GitHub Release Notes

### 🚀 What's New
* **Tiered DNA Syringes**: Split the original Soul Extractor into three tiers (Basic, Advanced, and Elite). Each tier requires progressively higher tier materials to craft, and filled variants now automatically preserve their respective tier appearances.
* **Scrap Recovery**: You can now salvage Broken Spawner Chunks (Scrap)! Use Sandpaper on a Broken Spawner Chunk to polish it back into a Blank Spawner Chunk.
* **Create Custom Attributes**: Added native integration with Create's Attribute Filter system for Spawner Chunks! You can now easily filter items in Brass Funnels using:
  * *Is a Spawner Chunk*: Matches any chunk state (Blank, Scrap, Incomplete, or Complete).
  * *Has Spawner DNA*: Matches any chunk currently holding mob DNA (Incomplete or Complete), as well as filled syringes.
  * *Is a Blank Spawner Chunk*: Matches only pristine Blank chunks.
  * *Is a Scrap Spawner Chunk*: Matches only broken/scrap chunks.

### 🛠️ Bug Fixes
* **Sequenced Assembly Fix**: Fixed a major bug where completing a sequenced assembly for a Spawner Chunk would always yield a Blaze spawner regardless of the injected DNA. The assembly process now strictly checks and respects the mob type stored in the syringe components.
* **Texture Fixes**: Fixed broken spawner chunk missing textures and corrected syringe visual mappings.

---

## Modrinth Changelog

### Version 1.2 - The Automation Update

This update heavily improves the Create sequenced assembly lines and automation capabilities, alongside bug fixes and balancing tweaks!

**New Features & Changes:**
- **3 New Syringe Tiers**: The Soul Extractor has been split into **Basic**, **Advanced**, and **Elite** variants.
- **Scrap Recovery**: Added a Sandpaper polishing recipe to clean Broken Spawner Chunks back into reusable Blank Spawner Chunks!
- **Custom Filter Attributes**: Brass Funnels just got smarter! You can now use Create's Attribute Filters to sort your factory lines using custom attributes: `"Is a Spawner Chunk"`, `"Has Spawner DNA"`, `"Is a Blank Spawner Chunk"`, and `"Is a Scrap Spawner Chunk"`.

**Fixes:**
- Fixed a critical bug where Sequenced Assembly would ignore the mob DNA and always output a Blaze Spawner. Assembly lines now correctly output the exact mob you injected!
- Fixed missing textures for broken spawner chunks.

---

## CurseForge Changelog

**Version 1.2 Update Notes**

**Highlights:**
* **Tiered Syringes:** Added Basic, Advanced, and Elite Soul Extractors.
* **Smart Filtering:** Added custom Create Filter Attributes! Use an Attribute Filter to easily sort out Blank chunks, Scrap chunks, or anything containing Spawner DNA.
* **Scrap Polishing:** You can now polish a Broken Spawner Chunk with Sandpaper to recover a Blank Spawner Chunk.
* **Bugfix (Sequenced Assembly):** Spawner Chunks crafted via Sequenced Assembly now correctly inherit the mob type from the syringe used, fixing the issue where they all defaulted to Blaze.
* **Bugfix:** Fixed broken texture mapping for various items.
