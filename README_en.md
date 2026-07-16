[简体中文](README.md) | [English](README_en.md)

# WooHolograms

🍵 A feature-rich Minecraft hologram plugin with GUI support

## Features

### 🎨 Display Types

- **Text**: Color codes, PlaceholderAPI, animation effects
- **Item**: Rendered via ItemDisplay entity, supports enchantment glow
- **Block**: Rendered via BlockDisplay entity, displays any block
- **Player Head**: Supports Base64 textures, player names, HeadDatabase
- **Custom Entity**: Display any entity type as hologram
- **Page Buttons**: Built-in #NEXT/#PREV pagination

### 🌟 Visual Effects

- **Brightness**: Custom sky light and block light (0-15)
- **Text Alignment**: Left, center, right alignment; multi-line text auto-aligns into unified rectangular background
- **Background Opacity**: Custom text background opacity (0=transparent ~ 255=opaque)
- **Background Color**: Color names and hex (#FF0000) supported
- **Billboard Modes**: Fixed, vertical, horizontal, center
- **Gradient Animation**: Smooth color gradient effects
- **Double-Sided**: Double-sided text rendering
- **Chroma**: Background color and glow color animate along HSL color wheel
- **Display Properties**: Scale (X/Y/Z), shadow, glow color, brightness; hologram-level defaults with per-line overrides

### 🎭 Animation System

- **Wave**: `<#ANIM:wave>text</#ANIM>`, supports custom color parameters
- **Typewriter**: `<#ANIM:typewriter>text</#ANIM>`, character-by-character reveal
- **Blink**: `<#ANIM:blink>text</#ANIM>`, supports speed parameter
- **Scroll**: `<#ANIM:scroll>text</#ANIM>`, supports width parameter
- **Gradient**: `<#ANIM:gradient:red,blue>text</#ANIM>`, multi-color gradient
- **Custom**: Create personalized animations via config
- **Pre-compiled**: Frames pre-calculated at registration, zero parsing overhead at runtime

### 🖱️ Interaction

- **Click Actions**: Left, right, shift+left, shift+right, any click
- **Per-Line Actions**: Each line can have independent click actions
- **Page Actions**: Hologram-level click actions
- **Action Types**: Command, message, sound, teleport, page navigation, etc.
- **Click Cooldown**: Prevents action spam, configurable cooldown

### 🔧 More Features

- **Display Entity**: Based on TextDisplay/ItemDisplay/BlockDisplay, excellent performance
- **Folia Support**: Full compatibility with Folia regionized multithreading
- **Incremental Rendering**: Only updates changed data, reduces network packets
- **Object Pool**: Renderer object reuse, reduces GC pressure
- **Pre-compiled Animations**: Frame data pre-calculated and cached, zero runtime parsing
- **Profiler**: Built-in lightweight profiler, on-demand, helps locate bottlenecks
- **Data Conversion**: One-click import from HolographicDisplays
- **DH Compatible**: Auto-reads and migrates DecentHolograms configs, built-in API compat layer
- **Fine-grained Permissions**: Independent permission node per command
- **TAB Completion**: Comprehensive command completion
- **GUI Management**: Visual editor, all features accessible via GUI

## Commands

### Basic Commands

| Command                | Description              | Permission                      |
| ---------------------- | ------------------------ | ------------------------------- |
| `/wh create <name>`    | Create a hologram        | `wooholograms.command.create`   |
| `/wh delete <name>`    | Delete a hologram        | `wooholograms.command.delete`   |
| `/wh copy <name> <new>`| Copy a hologram          | `wooholograms.command.copy`     |
| `/wh list [page]`      | List all holograms       | `wooholograms.command.list`     |
| `/wh info <name>`      | View hologram details    | `wooholograms.command.info`     |
| `/wh gui [name]`       | Open GUI manager         | `wooholograms.command.gui`      |
| `/wh near [range]`     | Show nearby holograms    | `wooholograms.command.near`     |
| `/wh reload`           | Reload config            | `wooholograms.command.reload`   |

### Position Commands

| Command                              | Description              | Permission                        |
| ------------------------------------ | ------------------------ | --------------------------------- |
| `/wh movehere <name>`                | Move to current position | `wooholograms.command.movehere`   |
| `/wh moveto <name> <x> <y> <z> [world]` | Move to coordinates   | `wooholograms.command.moveto`     |
| `/wh teleport <name>`                | Teleport to hologram     | `wooholograms.command.teleport`   |

### Line Management Commands

| Command                              | Description        | Permission                          |
| ------------------------------------ | ------------------ | ----------------------------------- |
| `/wh addline <name> <content>`       | Add a line         | `wooholograms.command.addline`      |
| `/wh setline <name> <line> <content>`| Set line content   | `wooholograms.command.setline`      |
| `/wh deleteline <name> <line>`       | Delete a line      | `wooholograms.command.removeline`   |
| `/wh insertline <name> <line> <content>` | Insert a line  | `wooholograms.command.insertline`   |
| `/wh offset <name> <line> <offset>`  | Set line offset    | `wooholograms.command.offset`       |
| `/wh height <name> <line> <height>`  | Set line height    | `wooholograms.command.height`       |

### Page Management Commands

| Command                              | Description          | Permission                          |
| ------------------------------------ | -------------------- | ----------------------------------- |
| `/wh addpage <name>`                 | Add a page           | `wooholograms.command.addpage`      |
| `/wh deletepage <name> <page>`       | Delete a page        | `wooholograms.command.removepage`   |
| `/wh swappage <name> <page1> <page2>`| Swap two pages       | `wooholograms.command.swappage`     |

### Property Commands

| Command                                                   | Description        | Permission                          |
| --------------------------------------------------------- | ------------------ | ----------------------------------- |
| `/wh setrange <name> <range>`                             | Set view range     | `wooholograms.command.setrange`     |
| `/wh setinterval <name> <interval>`                       | Set update interval| `wooholograms.command.setinterval`  |
| `/wh setpermission <name> [permission]`                   | Set view permission| `wooholograms.command.setpermission`|
| `/wh setfacing <name> <mode> [angle]`                     | Set billboard mode | `wooholograms.command.setfacing`    |
| `/wh setdoublesided <name> <true\|false>`                 | Set double-sided   | `wooholograms.command.setdoublesided`|
| `/wh setscale <name> [line] <x> <y> <z>`                  | Set scale          | `wooholograms.command.setscale`     |
| `/wh setshadow <name> [line] <radius> <strength>`         | Set shadow         | `wooholograms.command.setshadow`    |
| `/wh setglowcolor <name> [line] <color\|#RRGGBB\|reset>`  | Set glow color     | `wooholograms.command.setglowcolor` |
| `/wh setchroma <name> [line] background\|glow <true\|false>` | Set chroma      | `wooholograms.command.setchroma`    |
| `/wh enable <name>`                                       | Enable hologram    | `wooholograms.command.enable`       |
| `/wh disable <name>`                                      | Disable hologram   | `wooholograms.command.disable`      |

### Action Commands

| Command                                                | Description        | Permission                          |
| ------------------------------------------------------ | ------------------ | ----------------------------------- |
| `/wh actions <name>`                                   | List actions       | `wooholograms.command.actions`      |
| `/wh addaction <name> <line> <click_type> <action>`    | Add click action   | `wooholograms.command.addaction`    |
| `/wh deleteaction <name> <line> <action_index>`        | Delete action      | `wooholograms.command.deleteaction` |

### Utility Commands

| Command                              | Description                        | Permission                        |
| ------------------------------------ | ---------------------------------- | --------------------------------- |
| `/wh convert holographicdisplays`    | Import from HolographicDisplays    | `wooholograms.command.convert`    |
| `/wh profiler [on\|off\|reset]`      | View/control profiler              | `wooholograms.command.profiler`   |

## Line Type Formats

| Format               | Description                    | Example                     |
| -------------------- | ------------------------------ | --------------------------- |
| Plain text           | Display text content           | `&aWelcome to the server!`  |
| Multi-line text      | Use `\n` for line breaks       | `&aLine1\n&bLine2`          |
| `#ICON:<item>`       | Display item icon (ItemDisplay)| `#ICON:DIAMOND`             |
| `#BLOCK:<block>`     | Display block (BlockDisplay)   | `#BLOCK:STONE`              |
| `#HEAD:<type>:<value>` | Display player head          | `#HEAD:PLAYER:Notch`        |
| `#HEAD:URL:<Base64>` | Custom skin head               | `#HEAD:URL:eyJ0ZXh0...`     |
| `#HEAD:HDB:<ID>`     | HeadDatabase head              | `#HEAD:HDB:12345`           |
| `#SMALLHEAD:...`     | Small head display             | `#SMALLHEAD:PLAYER:Notch`   |
| `#ENTITY:<type>`     | Display entity                 | `#ENTITY:ZOMBIE`            |
| `#NEXT`              | Next page button               | `#NEXT Next`                |
| `#PREV`              | Previous page button           | `#PREV Previous`            |

### Item Parameters

Additional parameters after `#ICON`:

```
#ICON:DIAMOND_SWORD custom-model-data:10000 name:&6Legendary_Sword glow
```

| Parameter               | Description           | Example                        |
| ----------------------- | --------------------- | ------------------------------ |
| `custom-model-data:<n>` | Custom model data     | `custom-model-data:10000`      |
| `cmd:<n>`               | Shorthand for above   | `cmd:10000`                    |
| `color:<RGB>`           | Leather color         | `color:FF0000`                 |
| `name:<name>`           | Custom name           | `name:&6Legendary_Sword`       |
| `lore:<lore>`           | Item description      | `lore:&7Description`           |
| `glow`                  | Glow effect           | `glow`                         |
| `unbreakable`           | Unbreakable           | `unbreakable`                  |

## Animation Formats

### Basic Syntax

Two formats supported: `<#ANIM:name>text</#ANIM>` or `{#ANIM:name}text{/#ANIM}`

### Setting Animation Colors

**Method 1: Add color codes outside animation tags**

```
&a<#ANIM:typewriter>Welcome to the server</#ANIM>
&c<#ANIM:blink>Important announcement</#ANIM>
&b<#ANIM:scroll:15>This is a long scrolling announcement</#ANIM>
```

**Method 2: Use animation parameters (some animations only)**

```
<#ANIM:wave:&c,&e>Wave text</#ANIM>
<#ANIM:gradient:red,blue>Gradient</#ANIM>
```

### Built-in Animations

| Animation  | Format                                       | Parameters           | Example                                    |
| ---------- | -------------------------------------------- | -------------------- | ------------------------------------------ |
| Wave       | `<#ANIM:wave:primary,secondary>text</#ANIM>` | Primary, secondary (color codes) | `<#ANIM:wave:&e,&f>Hello</#ANIM>`     |
| Typewriter | `<#ANIM:typewriter>text</#ANIM>`             | None, add color outside | `&a<#ANIM:typewriter>Welcome</#ANIM>`      |
| Blink      | `<#ANIM:blink:speed>text</#ANIM>`            | Speed (number, default 10) | `&c<#ANIM:blink:5>Important</#ANIM>`  |
| Scroll     | `<#ANIM:scroll:width>text</#ANIM>`           | Width (number, default 20) | `&b<#ANIM:scroll:15>Announcement</#ANIM>` |
| Gradient   | `<#ANIM:gradient:color1,color2,...>text</#ANIM>` | Colors (name or HEX) | `<#ANIM:gradient:red,blue>Gradient</#ANIM>` |

### Gradient Color Support

Gradient animation supports these color formats:

- **Color names**: `red`, `blue`, `green`, `yellow`, `cyan`, `magenta`, `white`, `black`, `orange`, `purple`, `pink`, `gold`, `gray`, `aqua`, `lime`, etc.
- **HEX format**: `#FF0000`, `#00FF00`, `#0000FF`, etc.

Examples:

```
<#ANIM:gradient:red,blue>Red-blue gradient</#ANIM>
<#ANIM:gradient:#FF0000,#00FF00,#0000FF>Three-color gradient</#ANIM>
<#ANIM:gradient:gold,orange,red>Fire effect</#ANIM>
```

## Billboard Modes

| Mode        | Description                              |
| ----------- | ---------------------------------------- |
| `fixed`     | Fixed at specified angle                 |
| `horizontal`| Follows player view horizontally, fixed vertically |
| `vertical`  | Follows player view vertically, fixed horizontally  |
| `all`       | Fully follows player view (default)      |

## Text Alignment

Hologram-level text alignment; all text lines auto-align into unified rectangular background:

| Alignment | Description           | Effect                    |
| --------- | --------------------- | ------------------------- |
| `LEFT`    | Left align (default)  | Short lines padded right  |
| `CENTER`  | Center align          | Short lines padded both sides |
| `RIGHT`   | Right align           | Short lines padded left   |

## Background Settings

### Background Opacity

Controls text background opacity, range 0-255:

- `0` = Fully transparent (invisible background)
- `128` = Semi-transparent (default)
- `255` = Fully opaque

### Background Color

Supports color names and hex format:

| Color Name             | Value   | Color Name   | Value   |
| ---------------------- | ------- | ------------ | ------- |
| black                  | #000000 | white        | #FFFFFF |
| red                    | #FF0000 | green        | #00FF00 |
| blue                   | #0000FF | yellow       | #FFFF00 |
| aqua / cyan            | #00FFFF | gray / grey  | #808080 |
| dark\_red              | #AA0000 | dark\_green  | #00AA00 |
| dark\_blue             | #0000AA | dark\_aqua   | #00AAAA |
| dark\_purple / purple  | #AA00AA | dark\_gray   | #404040 |
| gold / orange          | #FFAA00 |              |         |

Also supports hex format: `#FF0000`, `#00FF00`, etc.

### Chroma

Background color and glow color animate along HSL color wheel, updated every frame:

```
/wh setchroma <name> background true     # Hologram-level: background chroma
/wh setchroma <name> glow true           # Hologram-level: glow chroma
/wh setchroma <name> 2 background true   # Line-level: line 2 background chroma
/wh setchroma <name> 2 glow true         # Line-level: line 2 glow chroma
```

## Action Types

| Type        | Description                       | Example                            |
| ----------- | --------------------------------- | ---------------------------------- |
| `COMMAND`   | Execute command as player         | `COMMAND:spawn`                    |
| `CONSOLE`   | Execute command as console        | `CONSOLE:give {player} diamond 1`  |
| `MESSAGE`   | Send message                      | `MESSAGE:&aHello {player}!`        |
| `SOUND`     | Play sound                        | `SOUND:ENTITY_PLAYER_LEVELUP`      |
| `TELEPORT`  | Teleport player                   | `TELEPORT:world,100,64,200`        |
| `SERVER`    | Connect to another server (BungeeCord) | `SERVER:lobby`               |
| `NEXT_PAGE` | Next page                         | `NEXT_PAGE`                        |
| `PREV_PAGE` | Previous page                     | `PREV_PAGE`                        |
| `PAGE`      | Jump to specific page             | `PAGE:3`                           |

### Click Types

| Type           | Description    |
| -------------- | -------------- |
| `ANY`          | Any click      |
| `LEFT`         | Left click     |
| `RIGHT`        | Right click    |
| `SHIFT_LEFT`   | Shift + Left   |
| `SHIFT_RIGHT`  | Shift + Right  |

## Built-in Placeholders

| Placeholder             | Description      |
| ----------------------- | ---------------- |
| `{player}`              | Player name      |
| `{player_uuid}`         | Player UUID      |
| `{player_displayname}`  | Player display name |
| `{player_x}`            | Player X coord   |
| `{player_y}`            | Player Y coord   |
| `{player_z}`            | Player Z coord   |
| `{player_world}`        | Player world     |
| `{player_health}`       | Player health    |
| `{player_level}`        | Player level     |

## PlaceholderAPI Placeholders

| Placeholder                       | Description              |
| --------------------------------- | ------------------------ |
| `%wooholograms_count%`            | Total hologram count     |
| `%wooholograms_player_page%`      | Player's current page    |

## API Usage Example

```java
import com.oolonghoo.holograms.api.WooHologramsAPI;
import com.oolonghoo.holograms.api.event.HologramClickEvent;
import com.oolonghoo.holograms.hologram.Billboard;
import com.oolonghoo.holograms.hologram.Brightness;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.action.Action;
import com.oolonghoo.holograms.action.ActionType;
import com.oolonghoo.holograms.action.ClickType;

// Check if API is available
if (!WooHologramsAPI.isLoaded()) {
    return;
}

// Create hologram (returns Optional)
Optional<Hologram> opt = WooHologramsAPI.createHologram("test", player.getLocation());
if (opt.isEmpty()) {
    player.sendMessage("Creation failed, name may already exist");
    return;
}
Hologram holo = opt.get();

// Add lines
HologramPage page = holo.getPage(0);
page.addLine("&aWelcome!");
page.addLine("#ICON:DIAMOND");
page.addLine("#BLOCK:GOLD_BLOCK");
page.addLine("#NEXT Next");

// Set hologram-level Display properties
holo.setScale(1.5f, 1.5f, 1.0f);           // Scale
holo.setGlowColor(0xFF0000);                 // Glow color (pure RGB)
holo.setBrightness(new Brightness(15, 15));  // Brightness (sky light, block light)
holo.setChromaBackground(true);              // Chroma background
holo.setChromaGlow(true);                    // Chroma glow

// Non-TEXT lines can override individually
HologramLine iconLine = page.getLine(1);
iconLine.setScale(2.0f, 2.0f, 2.0f);
iconLine.setShadowRadius(0.5f);
iconLine.setBillboard(Billboard.FIXED_ANGLE);
iconLine.setCustomYaw(90f);
iconLine.setCustomPitch(0f);

// Add actions
iconLine.addAction(ClickType.LEFT, new Action(ActionType.COMMAND, "spawn"));
page.addAction(ClickType.RIGHT, new Action(ActionType.MESSAGE, "&aClicked!"));

// Show to player
holo.show(player);

// Get existing hologram
Optional<Hologram> existing = WooHologramsAPI.getHologram("test");
existing.ifPresent(h -> h.show(player));

// Listen for click events
@EventHandler
public void onHologramClick(HologramClickEvent event) {
    Player player = event.getPlayer();
    Hologram hologram = event.getHologram();
    HologramPage page = event.getPage();
    ClickType clickType = event.getClickType();
}
```

## Permissions

### Admin Permissions

| Permission                 | Description                          | Default |
| -------------------------- | ------------------------------------ | ------- |
| `wooholograms.admin`       | Admin (includes all sub-permissions) | OP      |
| `wooholograms.command.*`   | All command permissions              | OP      |

### Command Permissions

| Permission                             | Description            |
| -------------------------------------- | ---------------------- |
| `wooholograms.command.create`          | Create hologram        |
| `wooholograms.command.delete`          | Delete hologram        |
| `wooholograms.command.copy`            | Copy hologram          |
| `wooholograms.command.near`            | Show nearby holograms  |
| `wooholograms.command.enable`          | Enable hologram        |
| `wooholograms.command.disable`         | Disable hologram       |
| `wooholograms.command.list`            | List holograms         |
| `wooholograms.command.info`            | View details           |
| `wooholograms.command.teleport`        | Teleport to hologram   |
| `wooholograms.command.movehere`        | Move to current pos    |
| `wooholograms.command.moveto`          | Move to coordinates    |
| `wooholograms.command.addline`         | Add line               |
| `wooholograms.command.removeline`      | Delete line            |
| `wooholograms.command.setline`         | Set line content       |
| `wooholograms.command.insertline`      | Insert line            |
| `wooholograms.command.addpage`         | Add page               |
| `wooholograms.command.removepage`      | Delete page            |
| `wooholograms.command.swappage`        | Swap pages             |
| `wooholograms.command.setrange`        | Set view range         |
| `wooholograms.command.setinterval`     | Set update interval    |
| `wooholograms.command.setpermission`   | Set view permission    |
| `wooholograms.command.setfacing`       | Set billboard mode     |
| `wooholograms.command.setdoublesided`  | Set double-sided       |
| `wooholograms.command.addaction`       | Add action             |
| `wooholograms.command.deleteaction`    | Delete action          |
| `wooholograms.command.actions`         | View actions           |
| `wooholograms.command.offset`          | Set offset             |
| `wooholograms.command.height`          | Set height             |
| `wooholograms.command.reload`          | Reload config          |
| `wooholograms.command.setpage`         | Set page               |
| `wooholograms.command.gui`             | Open GUI               |
| `wooholograms.command.help`            | View help              |
| `wooholograms.command.convert`         | Import data            |
| `wooholograms.command.profiler`        | Profiler               |
| `wooholograms.command.setscale`        | Set scale              |
| `wooholograms.command.setshadow`       | Set shadow             |
| `wooholograms.command.setglowcolor`    | Set glow color         |
| `wooholograms.command.setchroma`       | Set chroma             |

## DecentHolograms Config Compatibility

WooHolograms can read DecentHolograms config format for easy migration:

- Auto-detects `.yml` files in `plugins/DecentHolograms/holograms/` on first start
- Auto-migrates DH format data to `plugins/WooHolograms/holograms/`
- Uses WooHolograms native format after migration; subsequent edits auto-save in new format
- Reads both DH kebab-case keys (e.g. `facing-direction`) and WH camelCase keys (e.g. `facingDirection`)

## DecentHolograms API Compatibility

WooHolograms includes a DecentHolograms API compatibility layer via `provides: DecentHolograms` declaration and same-package same-class delegation pattern.

**When DecentHolograms is not installed**, plugins depending on DH API will automatically use WooHolograms as backend, with no code changes required.

Covered DHAPI methods:

- Create/delete holograms
- Page operations (add/insert/delete/get)
- Line operations (add/insert/set/delete/get)
- Teleport hologram
- Show/hide hologram

## Data Import

### HolographicDisplays

Use `/wh convert holographicdisplays` (alias `hd`) to import:

- Scans `.yml` files in `plugins/HolographicDisplays/`
- Auto-parses HD format hologram positions and text content
- Auto-skips with prompt on name conflicts
- Does not support HD 3.x database files (will prompt)

### CMI

Use `/wh convert cmi` to import:

- Reads `plugins/CMI/Saves/holograms.yml` (falls back to `plugins/CMI/holograms.yml`)
- Auto-skips CMI-generated pagination button holograms (ending with `#>` / `#<`)
- `!nextpage!` separators auto-split into pages, each with pagination actions
- `ICON:` lines auto-converted to `#ICON:` format

## Profiler

Built-in lightweight profiler, disabled by default, enable on demand:

```
/wh profiler on       # Enable
/wh profiler          # View report
/wh profiler reset    # Reset data
/wh profiler off      # Disable
```

Report shows average duration and call count per module, sorted by total time descending, helping locate performance bottlenecks.

---

❤️ The author is a beginner developer. If there's anything that could be improved, feedback is welcome. Let's communicate together!

⭐ If you find this useful, please give a Star!
