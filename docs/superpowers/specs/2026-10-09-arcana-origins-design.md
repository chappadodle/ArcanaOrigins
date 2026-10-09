# Arcana Origins: design

## Context
You like Elemental Arcana and want its core magic as an **Origins addon**, in the spirit of Monster
Origins but on NeoForge:
- **Simplified:** no mana bar, no levels, no skill tree, no upgrades, no items. You pick a magic
  as your origin, and you simply *have* its spells, on cooldowns.
- **Each type of magic is an origin.**
- **New repo.** "Magic Origins" is taken (a 1.19–1.20 Fabric/Forge addon, plus the Paper plugin
  "Origins-Magic"), and so is "Elemental Origins". You chose **Arcana Origins**, for which no
  existing mod turned up.

Decisions you made:

| Question | Choice |
|---|---|
| Base mod | NeoOrigins |
| How magic appears | 8 element origins on the main origin screen |
| Name | Arcana Origins |
| Minecraft | 1.21.1 |

**Why NeoOrigins:**
- MIT licence, updated two weeks ago, supports 1.21.1 and 26.x.
- Origins and powers are JSON, plus Java power types that addons can register (fixed in 2.0.6).
- Six rebindable skill keys, cooldown icons on the HUD, and resource bars it can hide.
- It loads existing Origins packs.

**Why not Origins (NeoForge) by IAFEnvoy:** it calls itself "still under construction" and
documents no addon API.

## What carries over, and what doesn't
- **Kept, simplified:**
  - The 8 elements.
  - Their best spells as fixed abilities, with each one's visuals and sounds ported from
    Elemental Arcana.
  - Elemental damage and the element chart (×1.5 / ×0.5).
  - A small reaction set: Melt, Vaporize, Freeze, Swirl, and Conduct (lightning through
    soaked foes).
  - The Wet, Frosted and Frozen statuses.
- **Dropped:** mana, levels, XP, stats, the skill tree, spell levels and branches, gear, Essence,
  every item, structures, creatures, Attuned mobs and the story.
- **Abilities:** each one has a single fixed design (no branches), roughly Elemental Arcana's mid
  tier, balanced against vanilla mobs and players, as Origins powers are. Tap a key to cast. Each
  spell has its own cooldown shown on the HUD.

## The 8 origins
Each has three abilities:
- **Skill 1:** the attack, on a short cooldown.
- **Skill 2:** a utility or defence, on a medium cooldown.
- **Skill 3:** the ultimate, on a long cooldown.

Each also has passives and a weakness, as every Origins origin does. All cooldowns below are first
guesses, to be tuned with the gauge.

| Origin | Skill 1 | Skill 2 | Skill 3 | Passives | Weakness |
|---|---|---|---|---|---|
| **Emberborn** (Fire) | Fireball: bursts and ignites (~1.5 s) | Ember Sprite: a turret for 8 s (~20 s) | Pyronado: three flame wheels for 8 s (~60 s) | Immune to fire and lava; your hits on burning foes +25% | Water and rain hurt |
| **Tideborn** (Water) | Hydro Jet: a short shoving stream (~3 s) | Bubble Prison: traps a foe for 4 s (~15 s) | Tsunami (~60 s) | Breathe underwater, swim fast; regenerate in rain | Slowed in hot, dry biomes and the Nether |
| **Rimeborn** (Ice) | Icicle: piercing, frosts (~1 s) | Frost Shield: absorbs and chills attackers (~20 s) | Frost Nova: freezes all around (~45 s) | Immune to freezing; walk on powder snow; water freezes underfoot | Fire hurts ×1.5; slowed in hot biomes |
| **Galeborn** (Wind) | Wind Blade (~1 s) | Gale Dash, with brief invulnerability (~4 s) | Stormeye: a tornado (~60 s) | Double jump; no fall damage | 3 hearts less; knocked back further |
| **Stoneborn** (Earth) | Boulder (~2 s) | Stone Skin: armour and knockback resistance for 10 s (~25 s) | Tremor: a travelling quake (~45 s) | Natural armour; knockback resistance; mine stone faster | Sinks in water; a little slower |
| **Prismborn** (Crystal) | Prism Bolt: splits into shards (~1.5 s) | Prism Ward: reflects projectiles (~20 s) | Geode Sentinel: a taunting crystal guardian for 15 s (~60 s) | Sense ores nearby (an outline pulse); mine faster | Explosions hurt ×1.5 |
| **Stormborn** (Lightning) | Chain Lightning (~2 s) | Thunderclap: a shoving burst (~12 s) | Stormcall: a storm cloud that strikes foes (~75 s) | Immune to lightning; faster in thunderstorms | Shocked while standing in water |
| **Dawnborn** (Radiance) | Smite: a beam of light from above (~2 s) | Sanctuary: a healing zone (~30 s) | Dawnbreak: a small sun (~90 s) | Regenerate in sunlight; the undead burn near you | Weakened in darkness (light below 5) |

**Friendly fire:**
- Your spells never hit your own pets, your tamed animals or villagers (as Elemental Arcana's
  SpellTargets rules do).
- In PvP they hit players normally.

## Architecture
A new mod with mod id `arcanaorigins` and package `com.chappadodle.arcanaorigins`.

**Location:**
- Local: `…/claude but its not claude code/arcana-origins/`
- GitHub: public repo `chappadodle/ArcanaOrigins`, MIT licence.

**Dependencies:**
- NeoForge 21.1.252 or newer, on 1.21.1.
- NeoOrigins (1.21.1 build), as a required dependency from the Modrinth Maven
  (`maven.modrinth:neo-origins:<version>`). It's the one new download. JEI and Veil stay
  optional, as in Elemental Arcana.

**Ported from Elemental Arcana and trimmed** (same author, MIT, so copied in):
- `magic/`:
  - `Element`, elemental damage types (datapack `damage_type` JSON) and the chart.
  - The statuses Wet, Frosted and Frozen (`WetEffect`, `FrozenEffect`).
  - A trimmed `ElementalReactions`.
  - `SpellTargets`, with its spares rules.
- `spell/`: the 24 abilities as plain classes, each with a `cast(ServerPlayer)` and fixed
  numbers. They reuse ported pieces of these Elemental Arcana systems:
  - `SpellProjectile`, without levels or mana; the held and conjured parts are dropped, so every
    cast is tap-to-throw.
  - `MobJet` / Hydro Jet stream.
  - Bubble prisons.
  - The tornado (Stormeye).
  - The `Tremor` wave.
  - `GeodeSentinel`, `Stormcalls`, `Dawnbreaks`, `Sanctuaries`, `PrismWards`.
  - `EmberSprite`, Pyronado.
- **Client:** the matching renderers, particles (`GlowParticleOptions`, the fire, water, ice and
  wind emitters), models, `GlowHalo`, optional Bloom, and the sound mixes (vanilla layers plus
  the few signature `.ogg` files).
- **NeoOrigins integration (`power/`):**
  - One registered **Java power type**, `arcanaorigins:spell` (field `spell: "fireball"`, plus
    the cooldown and key fields), that casts the named ability when its key is pressed.
  - Passives use NeoOrigins' built-in JSON power types wherever they exist: damage modifiers,
    effect immunity, attribute modifiers, and conditioned effects such as "in water" or
    "in sunlight".
  - Our own small Java power types only for what's missing (e.g. double jump, frost walking,
    the ore sense).
- **Data:**
  - `data/arcanaorigins/origins/origins/<name>.json` (×8).
  - `…/powers/*.json`.
  - A layer file that merges the 8 origins into the main origin layer.
  - Lang, icons and origin-screen descriptions.
- **Tools, ported from Elemental Arcana:**
  - `tools/server_console.sh` (server tests).
  - The isolated `tools/autotest.sh` (keeping its dbus and KWin isolation exactly).
  - `mp_test.sh`.
  - The gauge (`/arcanaorigins gauge`) and the AutoTest `fight` step.
  - The asset generator scripts for each element's textures.

## Milestones
Each milestone follows the Elemental Arcana workflow: write a spec under
`docs/superpowers/specs/`, build, test, then commit and push with the noreply identity.
1. **M0, the API check** (before anything else). Clone or read NeoOrigins' 1.21.1 source and
   confirm:
   - how an addon registers a power type (registry key and base class/interface);
   - how an active power gets its key press and cooldown, and shows its HUD icon;
   - how an addon layer merges origins into the main screen;
   - that its resource bars can stay hidden.

   Then build a throwaway power that logs on key press.

   **Fallback** if custom Java power types turn out impractical: origins carry marker powers
   only, and our mod reads the player's origin through the documented
   `player.getData(OriginAttachments.originData())`, then handles its own keybinds and
   cooldown HUD.
2. **M1, scaffold and core magic, plus Emberborn** as the template origin:
   - repo, Gradle (from Elemental Arcana's build), the NeoOrigins dependency;
   - elements, damage, chart, statuses, reactions, `SpellProjectile`;
   - the `spell` power type;
   - the Fire origin end to end: three abilities, passives, weakness, icon and text.
3. **M2:** Tideborn and Rimeborn (and the reactions between Fire, Water and Ice).
4. **M3:** Galeborn and Stoneborn.
5. **M4:** Prismborn, Stormborn and Dawnborn.
6. **M5, balance and release:**
   - a gauge pass against vanilla foes (zombie groups, a ravager, the Warden, the Wither, the
     dragon) and against each other origin in duels;
   - the README and mod page;
   - showcase screenshots;
   - 1.0.0.

## Verification
- **Unit tests** (JUnit): the chart, reactions, cooldown numbers, ability rules.
- **Server tests** (`tools/server_tests/*.txt` via `server_console.sh`, with zero
  `Exception|/ERROR]` lines):
  - each origin can be set (`/origin set @s origins:origin arcanaorigins:emberborn`) and its
    powers apply;
  - passives and weaknesses do what they say (damage taken in water, fire immunity, etc.).
- **Autotests** (hidden, isolated client):
  - per origin: pick it, cast each ability at slowed husks, take screenshots of the effects,
    and log the damage;
  - check that the HUD cooldowns show and no mana bar appears.
- **Multiplayer:** the MP smoke test with two origins.
- **Gauge:** fights logged for M5.
- **Your play-test:** in a new Prism instance, "Arcana Origins", with NeoOrigins.

## M0 findings (2026-10-09)

Read from NeoOrigins' 1.21.1 source (2.2.29):

- **Registry:** power types live in `neoorigins:power_type`
  (`com.cyberday1.neoorigins.power.registry.PowerTypes.REGISTRY_KEY`). An addon registers its own
  with a `DeferredRegister` on that key, and JSON names them by id (`"type": "arcanaorigins:spell"`).
- **Active powers:** `power.builtin.base.AbstractActivePower<C extends Config>` handles the key
  slot, the per-power cooldown (`cooldown_ticks`), hunger and resource costs (we use neither),
  and the HUD icon (`HudIconConfig`: `cooldown_icon`, `always_show_icon`). A subclass implements
  `execute(ServerPlayer, C)` and returns false to take no cooldown. Passive power types extend
  `api.power.PowerType` (`onGranted`, `onRevoked`, `onTick`, `onHit`, `onKill`, `capabilities`).
- **The origin screen:** `data/neoorigins/origins/origin_layers/origin.json` merges additively
  into the built-in origin layer.
- **Names:** NeoOrigins ships an origin called Frostborn (and Fire, Water, Earth and Air
  Mages), so our Ice origin is **Rimeborn**.
- **The dependency:** `maven.modrinth:neo-origins:cA9nVTuh` (2.2.29 for 1.21.1).
- **Verified:** a dedicated server lists `arcanaorigins:emberborn` on the origin layer, with our
  `arcanaorigins:spell` power loaded.

## M1 notes (2026-10-09): the core and Emberborn

- **Layout:** the code keeps Elemental Arcana's package layout (`api/`, `content/`, `client/`), so
  later spells port with `tools/port_from_ea.py` (it renames packages, the mod class and the
  central registry, `ModContent` → `ModMagic`). New here: `magic/` (the `Spell` interface and the
  `Spells` table by name), `power/` (our NeoOrigins power types), `api/Origins` (the eight
  origins' elements), `api/ProjectileSpells`.
- **Spells:** each is a `Spell` (`cast(ServerPlayer)`, at one fixed strength). Projectile spells
  also implement the trimmed `ProjectileSpell` and fly as the trimmed `SpellProjectile`: thrown at
  once from the right hand toward the crosshair (from the eyes, its glow filled the screen), never
  held or charged.
- **Registration order matters:** `Spells.bootstrap()` runs in the mod's constructor, so the
  projectile spells (and the 3D models the client loads for them) exist before the model bake;
  otherwise the projectile renders as the missing-model cube.
- **The chart in PvP:** a player carries their Arcana origin's element (`CreatureElements`), so
  Fire into a Tideborn is resisted and into a Rimeborn strong.
- **Who's spared:** area spells spare your pets and villagers, and hit other players only where
  the server allows PvP (`SpellTargets`).
- **Power types:** `arcanaorigins:spell` (an active: `spell`, `cooldown_ticks`,
  `cooldown_icon`) and `arcanaorigins:aura_bonus` (a passive: your hits on a foe carrying an
  element, `pyro`, `hydro` or `cryo`, are `multiplier` times as hard; Emberborn's Scorch).
- **Emberborn:**
  - Fireball: 7 damage, a 2.5-block blast, 5 s burning, 1.5 s cooldown.
  - Ember Sprite: 20 s cooldown.
  - Pyronado: 5 a sweep (Elemental Arcana's 3 grew with spell power), 60 s cooldown.
  - Born of Fire (`neoorigins:prevent_action` fire), Scorch (+25% on burning foes), Quenched
    (`neoorigins:damage_in_water` with rain).
- **Tests:** `tools/autotest/emberborn.txt` presses the real skill keys (the AutoTest `skill <n>`
  step sends NeoOrigins' `ActivatePowerPayload`). Measured on a 200-health husk: Fireball 8,
  Ember Sprite 15, Pyronado 7 (its wheels shove the foe out of their orbit: it's a ring to keep
  foes off). A second press inside the cooldown does nothing. The HUD shows the cooldown icons on
  V, G and N, and no mana bar.

## M2 notes (2026-10-09): Tideborn and Rimeborn

- **Ported systems:**
  - the magic shield (`SpellShield`, `ShieldSpell`, `ShieldEvents`; the shield stores its spell
    by name) and its client (shards, shield hearts);
  - Bubble Prison (`BubblePrisons`, renderer);
  - the frozen shell (`FrozenState` keeps the synced `frozen_until` in step with Frozen);
  - Tsunami (`Tsunamis`, `GroundPath`);
  - the Hydro Jet's stream visuals (`WaterBeams`, `HydroStreamOptions`, water cubes);
  - the icicle and its shatter.

  New: `core/MagicAttachments` (shield, bubble, frozen_until only).
- **Hydro Jet:** a tap sprays for 1.5 s, following the caster's aim (NeoOrigins keys are taps;
  `HydroJetSpell` ticks the sprays itself). Range 16, 1.5 a hit every 4 ticks, soaks, pushes,
  splashes and douses fires, at Elemental Arcana's Surge look.
- **Icicle:** one icicle, speed 2.4, 6 damage, pierces one foe. It frosts (the vanilla freeze)
  and slows, Freezes the wet, and freezes water where it lands.
- **Frost Shield:** Elemental Arcana's, raised at its level 4 (absorbs 10 for 10 s, Cold Touch,
  Ice Parry).
- **Frost Nova (the ultimate):** radius 6, 6 damage, and Freezes everything it may touch.
- **Bubble Prison and Tsunami:** as Elemental Arcana had them. Each fails without taking a
  cooldown when it has no target or no room (`execute` returns false).
- **New passive:** `arcanaorigins:frost_walk` (water freezes under you; not while sneaking).
- **Dropped:** walking on powder snow (vanilla only allows it for leather boots or an entity type
  tag, which needs a mixin for players).
- **Passives from NeoOrigins' JSON:** water breathing, swim speed in water, Regeneration in rain
  (`persistent_effect` + `in_rain`), slowness in hot or dry biomes and in the Nether
  (`attribute_modifier` + `location_condition`), freeze immunity, and fire damage ×1.5
  (`modify_damage` on `#minecraft:is_fire`).
- **Measured (200-health husk):**
  - Tideborn: Hydro Jet 12 (and Wet), Bubble Prison traps, Tsunami 4.
  - Rimeborn: Icicle 7, Frost Nova 7, leaving the husk Frozen in its shell.
  - Frost Walker froze a pool's edge.
