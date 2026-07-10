import os
import json

base_dir = "src/main/resources/data/createmobgrinding/recipe/sequenced_assembly"
os.makedirs(base_dir, exist_ok=True)

mobs = {
    # Tier 1
    "pig": ("minecraft:porkchop", 1),
    "cow": ("minecraft:beef", 1),
    "sheep": ("minecraft:mutton", 1),
    "chicken": ("minecraft:chicken", 1),
    # Tier 2
    "zombie": ("minecraft:rotten_flesh", 2),
    "skeleton": ("minecraft:bone", 2),
    "spider": ("minecraft:spider_eye", 2),
    "creeper": ("minecraft:gunpowder", 2),
    "piglin": ("minecraft:gold_ingot", 2),
    # Tier 3
    "enderman": ("minecraft:ender_pearl", 3),
    "blaze": ("minecraft:blaze_rod", 3),
    "ghast": ("minecraft:ghast_tear", 3),
    "magma_cube": ("minecraft:magma_cream", 3),
    "wither_skeleton": ("minecraft:wither_skeleton_skull", 3),
    # Tier 4
    "evoker": ("minecraft:totem_of_undying", 4),
    "vindicator": ("minecraft:iron_axe", 4),
    "ravager": ("minecraft:saddle", 4),
    # Tier 5
    "wither": ("minecraft:nether_star", 5),
    "elder_guardian": ("minecraft:sponge", 5),
    "warden": ("minecraft:echo_shard", 5),
    "ender_dragon": ("minecraft:dragon_breath", 5)
}

tier_chances = {
    1: 100.0,
    2: 90.0,
    3: 75.0,
    4: 50.0,
    5: 20.0
}

for mob_id, data in mobs.items():
    ingredient = data[0]
    tier = data[1]
    
    exp_item = "create:experience_block" if tier >= 3 else "create:experience_nugget"
    success_chance = tier_chances[tier]
    
    results_array = [
        {
          "chance": success_chance,
          "id": "createmobgrinding:mob_spawner_chunk",
          "components": {
            "createmobgrinding:spawner_entity": f"minecraft:{mob_id}"
          }
        }
    ]
    if success_chance < 100.0:
        results_array.append({
          "chance": 100.0 - success_chance,
          "id": "createmobgrinding:broken_spawner_chunk"
        })
        
    recipe = {
      "type": "create:sequenced_assembly",
      "ingredient": {
        "item": "createmobgrinding:blank_spawner_chunk"
      },
      "loops": 5,
      "results": results_array,
      "sequence": [
        {
          "type": "create:deploying",
          "ingredients": [
            {
              "item": "createmobgrinding:unfinished_spawner_chunk"
            },
            {
              "type": "neoforge:components",
              "item": "createmobgrinding:filled_soul_extractor",
              "components": {
                "createmobgrinding:spawner_entity": f"minecraft:{mob_id}"
              }
            }
          ],
          "results": [
            {
              "id": "createmobgrinding:unfinished_spawner_chunk",
              "components": {
                "createmobgrinding:spawner_entity": f"minecraft:{mob_id}"
              }
            }
          ]
        },
        {
          "type": "create:deploying",
          "ingredients": [
            {
              "item": "createmobgrinding:unfinished_spawner_chunk"
            },
            {
              "item": exp_item
            }
          ],
          "results": [
            {
              "id": "createmobgrinding:unfinished_spawner_chunk",
              "components": {
                "createmobgrinding:spawner_entity": f"minecraft:{mob_id}"
              }
            }
          ]
        },
        {
          "type": "create:pressing",
          "ingredients": [
            {
              "item": "createmobgrinding:unfinished_spawner_chunk"
            }
          ],
          "results": [
            {
              "id": "createmobgrinding:unfinished_spawner_chunk",
              "components": {
                "createmobgrinding:spawner_entity": f"minecraft:{mob_id}"
              }
            }
          ]
        }
      ],
      "transitional_item": {
        "id": "createmobgrinding:unfinished_spawner_chunk",
        "components": {
          "createmobgrinding:spawner_entity": f"minecraft:{mob_id}"
        }
      }
    }
    
    with open(os.path.join(base_dir, f"chunk_{mob_id}.json"), "w") as f:
        json.dump(recipe, f, indent=2)

print("Generated all recipes!")
