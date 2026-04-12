# Seam — Active Tasks

## 1. `/back` Dimension Restriction
**Effort**: Low — single permission check before teleporting.

Permission-based dimension blocking: `seam.backinto.<dimension_path>` (e.g. `seam.backinto.overworld`, `seam.backinto.the_nether`). If false, `/back` refuses to teleport the player into that dimension.

Uses `Identifier.getPath()` from the target world's registry key. Default behavior: allowed (permission defaults to true / op level 0).

- [x] In `BackCommand.back()`, after retrieving `lastLocation`, extract the world's dimension path via `lastLocation.getWorld().getRegistryKey().getValue().getPath()`
- [x] Check `permission(target, "seam.backinto." + dimensionPath, 0)` — op level 0 so all players have it by default; admins deny it via LuckPerms
- [x] If denied, send `Back-Dimension-Blocked` lang message with `{dimension}` placeholder
- [x] Add `Back-Dimension-Blocked` to `lang.yml`
- [x] Build verify

## 2. `/tpacancel`
**Effort**: Low — hooks into existing `TPAUtil` infrastructure.

Current state: `TPAUtil.teleportRequests` is keyed by **target UUID**. To cancel, the sender needs to find which target has their request. Need a reverse lookup.

- [x] Add `cancelTeleportRequest(UUID senderUuid)` to `TPAUtil` — iterate `teleportRequests`, remove entries where `request.senderUuid` matches, notify both parties
- [x] Create `TPACancelCommand extends CommandBase` — `super("tpacancel", "seam.tpacancel", 2)`, calls the new util method
- [x] Add lang entries: `TPA-Cancel-None`, `TPA-Cancel-Sender`, `TPA-Cancel-Target` (reused existing)
- [x] Register in `Seam.java`, add `tpacancel: true` to `modules.yml`
- [x] Build verify

## 3. `/seen <player>`
**Effort**: Low — reads existing `PlayerData` + server player manager.

Needs: store `lastLogin` and `firstJoin` timestamps in `PlayerData` + YAML.

- [x] Add `long firstJoin` and `long lastLogin` fields to `PlayerData`, serialize in `toConfiguration()`/`fromConfiguration()`
- [x] Set `lastLogin = System.currentTimeMillis()` on player join, `firstJoin` only if not already set
- [x] Create `SeenCommand extends CommandBase` — `super("seen", "seam.seen", 2)`; `PlayerStorageManager.findByUsername()` for offline lookup
- [x] Add lang entries: `Seen-Online`, `Seen-Offline`, `Seen-IP`, `Seen-Unknown`
- [x] Register + modules.yml + build verify

## 4. `/near`
**Effort**: Low — simple distance check on online players.

- [x] Create `NearCommand extends CommandBase` — `super("near", "seam.near", 2)`; same-world filter, sorted by distance
- [x] Add config: `Near-Radius: 200` in `settings.yml`; `SettingsManager.getNearRadius()` getter
- [x] Add lang entries: `Near-Players`, `Near-None`
- [x] Register + modules.yml + build verify

## 5. `/msg`, `/r`, Social Spy — Private Messaging Suite
**Effort**: Medium — shared `MessageUtil`, reply tracking, vanilla override, social spy hooks.

Implementing together since Social Spy is tightly coupled to `/msg` infrastructure.

### Phase A — MessageUtil (shared infrastructure)
- [x] Create `me.novoro.seam.utils.MessageUtil` (final utility class):
  - `Map<UUID, UUID> lastMessagePartner` — tracks last DM partner for `/r`
  - `sendPrivateMessage(ServerPlayerEntity sender, ServerPlayerEntity target, String message)`:
    - Send `Msg-Sent` to sender, `Msg-Received` to target
    - Update reply map for both directions
    - Call `notifySpies()` — iterate online players with `socialSpyToggle == true`, send `SocialSpy-Format` (skip sender + target)
  - `getLastMessagePartner(UUID)` — for `/r` lookups
  - `removePlayer(UUID)` — clean up on disconnect (call from Seam's player-leave handler)

### Phase B — Vanilla `/msg` conflict resolution
- [x] Created `CommandNodeAccessor` mixin (`@Mixin(CommandNode.class, remap=false)`), exposes `children` and `literals` maps
- [x] Registered in `seam.mixins.json`
- [x] `MessageCommand.register()` removes `msg`, `tell`, `w` before calling `super.register()`

### Phase C — MessageCommand
- [x] `MessageCommand extends CommandBase` — `super("msg", "seam.msg", 0, "message", "tell", "w", "whisper")`
  - Tree: `<player:string> <message:greedyString>`
  - Player suggestion provider (online players)
  - Validate target is online; prevent messaging self
  - Delegate to `MessageUtil.sendPrivateMessage()`

### Phase D — ReplyCommand
- [x] `ReplyCommand extends CommandBase` — `super("reply", "seam.reply", 0, "r")`
  - Tree: `<message:greedyString>`
  - Look up `MessageUtil.getLastMessagePartner(sender.getUuid())`
  - Resolve target from server player manager; error if offline or no partner
  - Delegate to `MessageUtil.sendPrivateMessage()`

### Phase E — PlayerData + SocialSpyCommand
- [x] Added `boolean socialSpyToggle` to `PlayerData` — persisted
- [x] `SocialSpyCommand` — full Brigadier tree: toggle self, on/off self, `<player> on/off` (requires `seam.socialspy.others`)

### Phase F — SocialSpiesCommand
- [x] `SocialSpiesCommand extends CommandBase` — `super("socialspies", "seam.socialspy", 2)`
  - Lists all online players with `socialSpyToggle == true`
  - Iterate `Seam.getServer().getPlayerManager().getPlayerList()`, filter via `PlayerStorageManager.get(uuid).socialSpyToggle`

### Phase G — Lang, Config, Registration
- [x] All lang entries added to `lang.yml`
- [x] `msg`, `reply`, `socialspy`, `socialspies` added to `modules.yml`
- [x] All four commands registered in `Seam.java`
- [x] `MessageUtil.removePlayer()` wired into `DISCONNECT` event
- [x] Build verify — PASSED

---

# Code Review — Senior Engineer Audit

Full codebase review. Findings organized by severity. Each item includes the file, line(s), what's wrong, and what the fix should be.

---

## 🔴 CRITICAL — Will crash or corrupt data in production

### CR-1. `TPAUtil` — `assert` used for null-safety in production code
**Files**: `TPAUtil.java:66`, `TPAUtil.java:100-101`

`assert sender != null` and `assert target != null` are used to guard against offline players. **`assert` is disabled by default in Java** (`-ea` flag is almost never set on production servers). When the sender/target disconnects between request creation and acceptance/timeout, `sender` will be `null` and the next line will `NullPointerException`, crashing the handler.

- [ ] Replace all `assert x != null` with explicit `if (x == null) return;` null checks
- [ ] Audit entire codebase for any other `assert` used for runtime safety (also in `Seam.java:322`)

### CR-2. `TPAUtil` — Race condition between server thread and async timeout thread
**File**: `TPAUtil.java:17`

`teleportRequests` is a plain `HashMap<UUID, List<TeleportRequest>>`. It's mutated from:
- Main server thread: `createTeleportRequest`, `handleTeleportRequest`, `removeTeleportRequest`, `cancelTeleportRequest`
- Async thread: `handleTeleportTimeouts` (runs every 1s on `SeamExecutor` pool)

`cancelTeleportRequest` creates a defensive `new HashMap<>(teleportRequests)` copy of the outer map, but the inner `ArrayList` values are **shared mutable references** — `requests.removeIf()` on the copy mutates the same list the server thread sees.

- [ ] Either: (a) make `handleTeleportTimeouts` dispatch to the main thread via `ServerScheduler.runSync()` before touching the map, or (b) use `ConcurrentHashMap` + `CopyOnWriteArrayList`
- [ ] Remove the `new HashMap<>()` defensive copy pattern once the root cause is fixed

### CR-3. `Seam.saveResource` — Resource leak + assert on null stream
**File**: `Seam.java:318-327`

```java
FileOutputStream outputStream = new FileOutputStream(file);  // never closed
...
assert in != null;  // disabled in production → NPE on next line
in.transferTo(outputStream);
```

- [ ] Wrap both streams in try-with-resources
- [ ] Replace `assert in != null` with `if (in == null) { SeamLogger.error(...); return; }`

### CR-4. `WaypointManager.saveConfig` — Serializes `Waypoint` object directly as YAML value
**File**: `WaypointManager.java:157`

`config.set("First-Join-Spawn", firstJoinSpawn)` stores a full `Waypoint` Java object. When SnakeYAML serializes this `Configuration`, the `Waypoint` is not a primitive, `Map`, or `Configuration` — it will either serialize as a Java object tag (`!!me.novoro.seam.objects.Waypoint`) producing unreadable YAML, or throw.

- [ ] Store the spawn name string instead: `config.set("First-Join-Spawn", firstJoinSpawn != null ? firstJoinSpawn.getName() : "")`
- [ ] Add a `getName()` method to `Waypoint` if not already present (it has `name` field but no getter)

### CR-5. `Location.copy()` — NPE when pitch or yaw is null
**File**: `Location.java:175`

```java
return new Location(this.world, this.x, this.y, this.z, this.pitch, this.yaw);
```

`this.pitch` and `this.yaw` are `@Nullable Float`. The constructor takes primitive `float`. Auto-unboxing `null` to `float` throws `NullPointerException`. This is called from `LocationUtil.getNextSafeBelow/Above` via `.copy()`.

- [ ] Add null guard: use the 3-arg constructor when pitch/yaw are null, or create a dedicated copy constructor

### CR-6. `Waypoint.setNBTLocation` — NPE on null pitch/yaw via auto-unboxing
**File**: `Waypoint.java:62`

```java
if (this.getPitch() != -1000 && this.getYaw() != -1000)
```

`getPitch()` returns `@Nullable Float`. Comparing `null != -1000` auto-unboxes null → NPE. Should check `this.getPitch() != null && this.getYaw() != null`.

- [ ] Replace sentinel comparison with null checks

---

## 🟠 HIGH — Bugs or correctness issues under normal conditions

### CR-7. `WaypointManager.reload` — Doesn't clear SPAWNS/WARPS before repopulating
**File**: `WaypointManager.java:34-49`

`reload()` adds spawns/warps from config but **never calls `SPAWNS.clear()` or `WARPS.clear()` first**. If a warp is deleted from `waypoints.yml` and `/seam reload` is run, the deleted warp persists in memory until a full server restart.

- [ ] Add `SPAWNS.clear()` and `WARPS.clear()` at the top of `reload()`

### CR-8. `GodCommand.toggleGod` — Varargs loop returns on first iteration
**File**: `GodCommand.java:57-64`

```java
private static boolean toggleGod(ServerPlayerEntity... targets) {
    for (ServerPlayerEntity target : targets) {
        ...
        return !wasGod;  // returns immediately — only first target is ever processed
    }
    return false;
}
```

The method signature accepts varargs but the `return` inside the loop means only the first player is ever toggled. If this were ever called with multiple targets, all but the first would be silently skipped. Currently only called with one target, but the API is misleading.

- [ ] Remove varargs, take a single `ServerPlayerEntity` parameter

### CR-9. `ClearInventoryCommand.clearInventoryConfirmations` — Keys are entity references
**File**: `ClearInventoryCommand.java:27`

`Map<ServerPlayerEntity, Long>` uses live entity references as keys. Disconnected `ServerPlayerEntity` objects (and their entire world/inventory graph) are pinned in memory, preventing garbage collection. On a busy server this is a memory leak.

- [ ] Change to `Map<UUID, Long>`

### CR-10. `TimeUtil.addReplacements` — Hours calculation is inverted
**File**: `TimeUtil.java:114`

```java
long hours = TimeUnit.DAYS.toHours(days) - hoursNoDays;
```

`hoursNoDays` is the **total** hours. `TimeUnit.DAYS.toHours(days)` is the hours accounted for by full days. Subtracting the larger from the smaller always yields a negative number. Should be:

```java
long hours = hoursNoDays - TimeUnit.DAYS.toHours(days);
```

- [ ] Fix the subtraction order
- [ ] Same bug exists in `addCleanReplacements` (line 127)

### CR-11. `TPAUtil.isTPToggled` — Inconsistent with `PlayerData.tpToggle`
**File**: `TPAUtil.java:16`

There's a `private static final Set<UUID> isTPToggled` that's never synced with `PlayerData.tpToggle`. The `PlayerData` field is persisted to YAML, but the `TPAUtil` set is purely in-memory and reset on restart. If any code checks one but not the other, behavior will be inconsistent.

- [ ] Remove `isTPToggled` from `TPAUtil`; use `PlayerStorageManager.get(uuid).tpToggle` everywhere
- [ ] Wire TPA commands to check `PlayerData.tpToggle` before creating requests

### CR-12. `Seam.reloadConfigs` — Doesn't reload `ModuleManager`
**File**: `Seam.java:113-126`

`reloadConfigs()` reloads lang, settings, teleportation, and waypoints — but **not `ModuleManager`**. The module manager is only loaded during `onInitialize()` (line 94). This means `/seam reload` cannot enable/disable commands. Server operators would need a full restart.

- [ ] Add `this.moduleManager.reload()` to `reloadConfigs()`, or document that command toggles require a restart

---

## 🟡 MEDIUM — Performance or design problems

### CR-13. `ModuleManager` — `List<String>` for active modules/commands, O(n) lookups
**File**: `ModuleManager.java:15-17`

`ACTIVE_MODULES` and `ACTIVE_COMMANDS` are `ArrayList<String>`. `.contains()` is O(n) and is called during every command registration and potentially at runtime.

- [ ] Change both to `Set<String>` (e.g., `HashSet`)

### CR-14. `TeleportationConfig` — Redundant string-based block check methods
**File**: `TeleportationConfig.java:24-26, 69-74`

`UNSAFE_BLOCKS` and `AIR_BLOCKS` (`List<String>`) are kept alongside `UNSAFE_BLOCKS_SET` and `AIR_BLOCKS_SET` (`Set<Block>`). The string-based `isBlockSafe(String)` / `isAirBlock(String)` methods still exist and do O(n) `List.contains()`. They should be deleted since the `Block`-based methods are now available.

- [ ] Remove `UNSAFE_BLOCKS` and `AIR_BLOCKS` lists and their corresponding string methods
- [ ] Update any callers still using the string overloads

### CR-15. `ServerPlayerEntityMixin.cacheLastLocation` — Full disk write on every teleport
**File**: `ServerPlayerEntityMixin.java:53-54`

`PlayerStorageManager.save(player.getUuid())` triggers YAML serialization to disk on **every teleport and every death**. The disconnect handler already saves all data. This is excessive I/O for servers with frequent teleportation.

- [ ] Remove the `save()` call from the mixin; rely on periodic auto-save + disconnect save
- [ ] Alternatively, mark the data as dirty and batch writes on a timer

### CR-16. `ColorUtil.replaceCodes` / `replaceLegacyCodes` — O(n²) replacement loop
**File**: `ColorUtil.java:45-50, 54-59`

Both methods run a `while(matcher.find())` loop that calls `input.replace()` (which scans the entire string), then **re-creates the matcher** on the new string. For input with `k` color codes, this is O(k × n) where n is string length.

- [ ] Use `Matcher.appendReplacement()` / `appendTail()` pattern for single-pass replacement

### CR-17. `SeamExecutorManager.THREAD_FACTORY` — Shared mutable `ThreadFactoryBuilder`
**File**: `SeamExecutorManager.java:16`

The static `THREAD_FACTORY` builder is mutated by `setNameFormat()` on every `create()` call. If `create()` is ever called concurrently, thread names will be corrupted. Even sequentially, the builder accumulates state from previous calls.

- [ ] Create a new `ThreadFactoryBuilder` inside each `create()` call instead of reusing a static one

### CR-18. `PlayerStorageManager.findByUsername` — Full disk scan of all player files
**File**: `PlayerStorageManager.java:74-92`

For offline player lookup, this scans every `.yml` file in the `players/` directory, parsing each with SnakeYAML until a match is found. On a server with thousands of players, this is very slow.

- [ ] Maintain a `Map<String, UUID>` name→uuid index (loaded once at startup, updated on join)

### CR-19. `InvseeCommand` / `EnderchestCommand` — Disk write per inventory slot change
**Files**: `InvseeCommand.java:88-97`, `EnderchestCommand.java:101-110`

The `inventory.addListener()` callback fires on **every single slot change** and writes the full NBT compound to disk each time. Dragging items around will produce dozens of writes per second.

- [ ] Save on screen close instead (override `onClose()` in a screen handler wrapper)

### CR-20. `NearCommand` — Computes `distanceTo()` three times per player
**File**: `NearCommand.java:29-30, 39`

`p.distanceTo(player)` is called in the filter, the sort comparator, and the display format — computing the same square root three times per player.

- [ ] Compute distance once into a record/pair, then filter/sort/display from the cached value

### CR-21. `VersionedConfig` — Uses `double` for config version comparison
**File**: `VersionedConfig.java:14, 34`

Floating-point comparison for version numbers (e.g., `1.1 > 1.0`) is fragile due to IEEE 754 precision issues. `1.1 + 0.1 != 1.2` in Java.

- [ ] Switch to integer versioning (e.g., `1`, `2`, `3`)

---

## 🔵 LOW — Code quality and minor issues

### CR-22. `Seam.java:41` — Unused import `java.util.List`
- [ ] Remove

### CR-23. `SettingsManager.java:10` — Unused import `java.sql.Time`
- [ ] Remove

### CR-24. `PlayerData` — All data fields are `public`
**File**: `PlayerData.java:9-19`

Fields like `username`, `flyToggle`, `socialSpyToggle`, `firstJoin`, `lastLogin` are all public. Any code anywhere can set invalid state (e.g., negative timestamps, null username after initialization).

- [ ] Make fields private, add getters/setters

### CR-25. `PlayerData` — Getters return boxed `Boolean` for primitive fields
**File**: `PlayerData.java:44-62`

`getFlyToggle()`, `getGodToggle()`, etc. return `Boolean` (boxed) when the underlying field is primitive `boolean`. This falsely suggests the value could be null.

- [ ] Change return types to primitive `boolean`

### CR-26. `SeamLogger` — Double-prefixes every log message
**File**: `SeamLogger.java:17, 25, 33`

Logger name is `"Seam"` → SLF4J output includes `[Seam]`. Each log method also prepends `"[Seam]: "`. Result: `[Seam] [Seam]: message`.

- [ ] Remove the `"[Seam]: "` prefix from the format string; rely on the logger name

### CR-27. `BroadcastCommand` — Unnecessary `Audience` variable
**File**: `BroadcastCommand.java:37-38`

```java
Audience audience = player;
audience.sendMessage(adventureComponent);
```

`ServerPlayerEntity` already implements `Audience`. The intermediate variable is pointless.

- [ ] Call `player.sendMessage(adventureComponent)` directly

### CR-28. `ClearInventoryCommand.clearInventory` — Redundant armor/offhand clearing
**File**: `ClearInventoryCommand.java:57-65`

`target.getInventory().clear()` already clears **all** slots including armor and offhand in 1.21.1. The explicit armor/offhand loops that follow are dead code.

- [ ] Remove the redundant loops

### CR-29. `LangManager.getOrDefault` — Instance method referencing static field via `this`
**File**: `LangManager.java:71`

`this.LANG` accesses the static `LANG` map through an instance reference. While Java allows this, it's misleading — it implies per-instance state.

- [ ] Change to `LangManager.LANG.getOrDefault(langKey, def)`

### CR-30. `Configuration.getList` — Raw `Collections.EMPTY_LIST`
**File**: `Configuration.java:325`

Uses `Collections.EMPTY_LIST` (raw type) instead of `Collections.emptyList()` (parameterized). Contributes to the unchecked/unsafe warnings.

- [ ] Replace with `Collections.emptyList()`

### CR-31. `Location.isEqualTo` — Direct `==` on doubles
**File**: `Location.java:73-75`

Floating-point `==` comparison is unreliable. Two locations that should be "equal" may differ by epsilon.

- [ ] Use `Double.compare()` or an epsilon-based comparison

### CR-32. `TPACommand` — Calls `getPlayerOrThrow()` three times
**File**: `TPACommand.java:25, 30, 31`

The same lambda calls `context.getSource().getPlayerOrThrow()` three separate times. While not a bug, it's messy.

- [ ] Store in a local `ServerPlayerEntity sender` variable

### CR-33. `Waypoint` — No `getName()` getter
**File**: `Waypoint.java:16`

`name` is `private final` but has no getter. Code that needs the name (like `addReplacements`) accesses it directly from within the class, but external code (like the `WaypointManager.saveConfig` fix from CR-4) cannot.

- [ ] Add `public String getName() { return this.name; }`

### CR-34. `InvseeScreen.onClose` — Empty override
**File**: `InvseeScreen.java:52`

`onClose()` is overridden to do nothing. If this is intentional (preventing default close behavior), it should have a comment. Otherwise it might suppress important cleanup in the parent class.

- [ ] Add a comment explaining intent, or remove if unnecessary

### CR-35. `FlyCommand` / `GodCommand` / `NightVisionCommand` / `WaterBreathingCommand` — Toggles not persisted
**Files**: `FlyCommand.java:1`, `GodCommand.java:56`

Multiple TODOs acknowledge that ability toggles should be synced with `PlayerData` (which already has `flyToggle`, `godToggle`, etc.). Currently, ability states are only set on the vanilla player abilities object and are lost on relog.

- [ ] Hook toggle commands to read/write `PlayerData` fields
- [ ] Apply persisted toggles on player join (e.g., re-enable fly if `data.flyToggle` is true)
