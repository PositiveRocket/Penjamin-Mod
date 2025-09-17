# Penjamin Mod (Forge 1.20.1)

Penjamin is a vibe mod that adds a clean, rip-ready pen to your world. Rips give status effects, and a full blinker hits you with **Nausea**. All about the vibe—just the pen, the feel, and the consequences.

---

## Features

- **The Pen** — a compact, rip-ready gadget.
- **Battery system** — one charge lasts **~40 rips**; rechargeable by **right-clicking a Redstone Block**.
- **Carts** — craft an **Empty Cart**, then **infuse** it with extracts (see below). Each cart lasts **~20 rips**.
- **Effects on rip** — different effects depending on what you’re running.
- **Blinker penalty** — push a full blinker and you’ll get **Nausea**.

> Extracts come from the **Smokeleaf Industries** mod. You’ll infuse those into Penjamin carts via a brewing stand.
>  
> 🔗 Smokeleaf Industries: — `[Smokeleaf Industries](https://www.curseforge.com/minecraft/mc-mods/smokeleaf-industry)`

---

## Requirements

- **Minecraft:** 1.20.1  
- **Forge:** 47.x (e.g., 47.4.0)  
- **Java:** 17  
- **Smokeleaf Industries** (for extracts/infusion content)

---

## How it works (quick tour)

1. **Craft a Battery** (any color).  
2. **Craft an Empty Cart.**  
3. **Grow & process** with **Smokeleaf Industries** to get extracts.  
4. **Infuse** the Empty Cart in a **Brewing Stand** using those extracts.  
5. **Rip the Pen**:
   - Each **Cart** ≈ **20 rips**.  
   - The **Battery** ≈ **40 rips** (so ~2 full carts per charge).  
   - **Recharge** by right-clicking a **Redstone Block**.  
   - Hold too long (a **blinker**) → **Nausea**.

_(Exact recipes/values may change per version; check JEI/EMI or your data pack if you customize.)_

---

## Installation

1. Install **Forge 1.20.1**.  
2. Drop the Penjamin mod `.jar` into your `mods/` folder.  
3. Also install **Smokeleaf Industries** if you want to craft/infuse carts with extracts.  
4. Launch the game.

> **Servers:** This is a content mod. Server and clients should both have the same mod set.

---

## Compatibility

- **Mod Loader:** Forge only (no Fabric/Quilt builds).  
- **JEI/EMI:** Optional but recommended for viewing recipes.  
- **Data packs:** Cart infusion and values can be data-driven; pack authors can tweak via tags/recipes.

---

## FAQ

**Do I need Smokeleaf Industries?**  
You can load Penjamin by itself, but you’ll need **Smokeleaf Industries** to make extracts and infuse carts.

**How many uses do I get?**  
About **20 rips per cart** and **40 rips per battery** (→ ~2 carts per full charge).

**How do I recharge?**  
Right-click a **Redstone Block** with the pen/battery.

**What’s a “blinker”?**  
Holding a full rip. If you push a blinker, Penjamin slaps you with **Nausea**.

**Multiplayer?**  
Yep. Install on the server and on all clients.

---

## Building from source

```bash
# Requires Java 17
git clone https://github.com/<you>/penjamin-mod.git
cd penjamin-mod

# On Windows:
gradlew build

# On macOS/Linux:
./gradlew build
