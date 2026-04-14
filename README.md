# 🚀 Addoners Optimizer

**Smart FPS Optimization for Minecraft (Fabric)**

Addoners Optimizer is a dynamic, adaptive performance engine that automatically adjusts your game settings based on real-time FPS to deliver smoother gameplay. Features a powerful `.addoners` profile system for creator-driven customization and shader-aware optimization.

**Version**: 1.5 (Production Release)  
**Minecraft**: 26.1.2  
**Loader**: Fabric

---

## ⚡ Features

- 🎯 **Automatic FPS Optimization** — Dynamically adjusts settings based on performance
- 🧠 **Adaptive Engine** — Responds to FPS changes with intelligent cooldown system
- 🛡️ **Stability** — Smooth optimization with rolling average FPS sampling (no jitter)
- 👁️ **Status Overlay** — Real-time HUD showing current optimization level, FPS, and more
- 🎮 **All Shaders Supported** — Works with Iris, Canvas, and all shader packs
- 🔥 **Addoners Shader Synergy** — Optimized profiles for Quartzglow & Skygleam
- 📄 **.addoners Profile System** — Creator-driven customization with dynamic rules
- ⚙️ **In-Game Menu** — Mod Menu integration for easy configuration
- 📊 **Structured Logging** — Comprehensive diagnostics for troubleshooting

---

## 📦 Installation

### Requirements
- [Fabric Loader](https://fabricmc.net/) ≥ 0.18.6
- [Fabric API](https://modrinth.com/mod/fabric-api)
- Java 25+
- Minecraft 26.1.2

### Setup
1. Download the latest `addoners-optimizer.jar` from Releases
2. Place it in your `mods` folder
3. Launch Minecraft with Fabric
4. Done! The optimizer runs automatically

---

## 🎮 Quick Start

### Play Now
Install and play! The optimizer runs **completely automatically**:
- ✅ Monitors your FPS every second
- ✅ Adjusts settings in real-time
- ✅ Shows status overlay (top-left corner)
- ✅ No manual configuration needed

### Access Settings (Mod Menu)
Press **ESC** → **Mods Folder** → Find **"Addoners Optimizer"** → Click to configure

---

## ⚙️ Configuration

### In-Game Settings (Mod Menu)

Open the Optimizer config screen from Mod Menu to toggle:

| Setting | Default | Purpose |
|---------|---------|---------|
| **Enable Optimizer** | ON | Master switch for all optimization |
| **Debug Logs** | OFF | Enable structured logging in console |
| **Status Overlay** | ON | Show HUD with FPS and level |
| **Shader Optimization** | ON | Reduce aggressiveness when shaders active |
| **Cycle Interval** | 20 ticks | How often to check FPS (1-200) |

### Config File

**Location**: `%appdata%/.minecraft/config/teamaddoners/modconfig.json`

```json
{
  "enabled": true,
  "debugLogs": false,
  "showStatus": true,
  "shaderOptimization": true,
  "optimizerIntervalTicks": 20
}
```

---

## 🎯 Optimization Levels

The optimizer determines when to apply changes based on FPS:

| Level | FPS Range | Behavior | Goal |
|-------|-----------|----------|------|
| **LOW** | ≥ 80 | Minimal changes | Maintain quality |
| **MEDIUM** | 50–79 | Moderate reductions | Balance quality/performance |
| **AGGRESSIVE** | < 50 | Maximum optimization | Recover FPS |

---

## 👁️ Status Overlay

The HUD shows current optimizer state:

```
Addoners Optimizer: MEDIUM
FPS: 62
Profile: quartzglow
Shaders: ON
```

- **Green** = LOW level (good FPS)
- **Yellow** = MEDIUM level (acceptable performance)
- **Red** = AGGRESSIVE level (low FPS)

Toggle via Mod Menu → "Show Status Overlay"

---

## 📄 .addoners Profile System (v1.5)

Profiles are JSON files that define **how** the optimizer should behave. Each profile contains base settings and dynamic rules triggered by FPS thresholds.

### Profile Locations

```
%appdata%/.minecraft/config/teamaddoners/profiles/
├── default.addoners        (Universal profile)
├── quartzglow.addoners     (Quartzglow shader optimized)
└── skygleam.addoners       (Skygleam shader optimized)
```

### Profile Structure

```json
{
  "type": "optimizer_profile",
  "version": 1,
  "name": "quartzglow",
  "shader": "quartzglow",
  "performanceTier": "MID_END",
  
  "settings": {
    "renderDistance": 10,
    "particles": 1,
    "smoothLighting": true,
    "disableShaders": false
  },
  
  "dynamicRules": {
    "fpsBelow60": {
      "renderDistance": 8,
      "particles": 0
    },
    "fpsBelow30": {
      "renderDistance": 6,
      "particles": 0,
      "disableShaders": true
    },
    "fpsBelow20": {
      "renderDistance": 4,
      "renderDistance": 2,
      "smoothLighting": false
    }
  }
}
```

### Field Definitions

| Field | Type | Purpose | Example |
|-------|------|---------|---------|
| `type` | string | Always `"optimizer_profile"` | `"optimizer_profile"` |
| `version` | int | Schema version for compatibility | `1` |
| `name` | string | Profile identifier | `"quartzglow"` |
| `shader` | string | Target shader or `"none"` | `"quartzglow"` |
| `performanceTier` | string | Hardware target: `LOW_END`, `MID_END`, `HIGH_END` | `"MID_END"` |
| `settings` | object | Static settings applied at load | See Settings Section |
| `dynamicRules` | object | FPS-triggered rule overrides | See Rules Section |

---

## ⚙️ Supported Settings

These are the game settings the optimizer can adjust:

| Setting | Type | Range | Purpose |
|---------|------|-------|---------|
| `renderDistance` | int | 2–32 | Chunk render distance |
| `particles` | int | 0–2 | Particle count (0=none, 1=decreased, 2=all) |
| `smoothLighting` | bool | true/false | Ambient occlusion |
| `disableShaders` | bool | true/false | Disable shader packs |
| `shadowQuality` | int | 0–3 | Shadow cascade quality |

---

## 📋 Rule Engine (Dynamic Rules)

Rules are triggered automatically based on FPS thresholds. They layer progressively—lower FPS means more aggressive overrides.

### Rule Types

| Rule | Trigger | Priority | Use Case |
|------|---------|----------|----------|
| `fpsBelow60` | FPS < 60 | Low | Mild adjustments |
| `fpsBelow30` | FPS < 30 | Medium | Moderate optimization |
| `fpsBelow20` | FPS < 20 | High | Emergency optimization |

### How Rules Work

1. **Profile settings** are applied first (base configuration)
2. **Dynamic rules** layer on top based on current FPS
3. **Shader safety** prevents overly aggressive cuts
4. **Engine fallback** applies if no profile is active

**Example**: With FPS dropping to 25:
- Base settings (renderDistance: 10) applied
- `fpsBelow60` rule applied (renderDistance: 8)
- `fpsBelow30` rule applied (renderDistance: 6, disableShaders: true) ← **final result**

---

## 🛠️ Creating Custom Profiles

### Basic Profile (Universal)

```json
{
  "type": "optimizer_profile",
  "version": 1,
  "name": "myprofile",
  "shader": "none",
  "performanceTier": "MID_END",
  "settings": {
    "renderDistance": 12,
    "particles": 1,
    "smoothLighting": true
  },
  "dynamicRules": {
    "fpsBelow60": {
      "renderDistance": 10,
      "particles": 0
    },
    "fpsBelow30": {
      "renderDistance": 6
    }
  }
}
```

### Shader-Specific Profile

```json
{
  "type": "optimizer_profile",
  "version": 1,
  "name": "my-iris-profile",
  "shader": "iris",
  "performanceTier": "HIGH_END",
  "settings": {
    "renderDistance": 16,
    "particles": 2,
    "smoothLighting": true,
    "shadowQuality": 2
  },
  "dynamicRules": {
    "fpsBelow60": {
      "renderDistance": 12,
      "shadowQuality": 1,
      "particles": 1
    },
    "fpsBelow30": {
      "renderDistance": 8,
      "shadowQuality": 0,
      "particles": 0
    }
  }
}
```

### Low-End PC Profile

```json
{
  "type": "optimizer_profile",
  "version": 1,
  "name": "lowend",
  "shader": "none",
  "performanceTier": "LOW_END",
  "settings": {
    "renderDistance": 6,
    "particles": 0,
    "smoothLighting": false
  },
  "dynamicRules": {
    "fpsBelow60": {
      "renderDistance": 4
    },
    "fpsBelow30": {
      "renderDistance": 2
    }
  }
}
```

### Installation Steps

1. Create a new `.json` file in `config/teamaddoners/profiles/`
2. Name it `yourprofile.addoners` (must end with `.addoners`)
3. Paste your profile JSON
4. Restart Minecraft
5. Select via Mod Menu or it loads automatically

---

## 🎮 Shader Support

### Built-in Shader Recognition

The optimizer automatically detects:
- **Iris** — Full shader pack support with intelligent level capping
- **Canvas** — PBR rendering pipeline
- Custom shaders via system property

### How Shader Awareness Works

When shaders are detected:
1. Optimization aggressiveness is **reduced**
2. Render distance cuts are less extreme
3. Visual quality is **prioritized over FPS**
4. Status overlay shows "Shaders: ON"

### Disable Shader Optimization

If you want aggressive optimization even with shaders:
- Open Mod Menu
- Uncheck "Shader Optimization"
- Save

---

## 🔄 Optimization Cycle

The optimizer runs on a configurable interval (default: 20 ticks = 1 second):

```
Tick 1-20:   Sample FPS (rolling average)
Tick 20:     Check optimization level
Tick 20:     Detect shader changes (~5s interval)
Tick 20:     Check cooldown (3-second minimum)
Tick 20:     Apply changes if needed
Tick 20:     Update status overlay
```

### Cooldown System

- **Minimum 3 seconds** between optimization changes
- Prevents jitter from rapid level switching
- Logs record when cooldown is active

---

## 📊 Structured Logging

Enable debug logs in Mod Menu to see detailed diagnostics:

```
[Addoners Optimizer] FPS: 62 → MEDIUM
[Addoners Optimizer] Cooldown active: false
[Addoners Optimizer] Profile applied: quartzglow
[Addoners Optimizer] Shader detected: true
```

View logs in:
- Console (both debug and info levels)
- `logs/latest.log`
- `logs/debug.log`

---

## 🐛 Troubleshooting

### Optimizer Not Working

**Check**:
1. Is it enabled? → Mod Menu → "Enable Optimizer: ON"
2. Is the profile loaded? → `config/teamaddoners/profiles/` has `.addoners` files
3. Check logs → Enable debug logs in Mod Menu

**Solution**:
```json
{
  "enabled": true,
  "showStatus": true,
  "debugLogs": true
}
```

### Settings Not Applying

**Possible Causes**:
- Profile has no settings defined
- FPS is too high (LOW level doesn't adjust)
- Cooldown is active (wait 3 seconds)

**Check**:
1. Switch profile manually
2. View status overlay
3. Wait for cooldown to expire
4. Monitor debug logs

### Shaders Disabled Unexpectedly

**Cause**: `fpsBelow30` or `fpsBelow20` rule triggered  
**Solution**: Modify profile rules or disable shader optimization

```json
"dynamicRules": {
  "fpsBelow30": {
    "disableShaders": false
  }
}
```

### Low FPS Despite Optimization

**Check**:
1. Is aggressive optimization being applied? → Check status overlay
2. Are render distance cuts sufficient? → Try FPS < 50 for AGGRESSIVE
3. Check for profile conflicts

**Debug**:
- Enable debug logs
- Monitor FPS in overlay
- Check profile settings

---

## 🌟 Best Practices

### For Performance
- Use **LOW_END** tier for mid-range PCs
- Set aggressive `fpsBelow60` rules
- Keep base render distance moderate

### For Quality
- Use **HIGH_END** tier
- Scale down gradually via `fpsBelow60` → `fpsBelow30`
- Keep particles enabled in base settings

### For Shaders
- Create **shader-specific** profiles (quartzglow.addoners, etc.)
- Use `shader` field to target specific packs
- Set higher base settings; rules handle drops

### For Stability
- Test rules with FPS drops
- Keep cooldown enabled (3 seconds default)
- Monitor logs after rule changes

---

## 📁 File Locations

```
%appdata%/.minecraft/
├── config/teamaddoners/
│   ├── modconfig.json                    (Main config)
│   └── profiles/
│       ├── default.addoners             (Built-in)
│       ├── quartzglow.addoners          (Built-in)
│       ├── skygleam.addoners            (Built-in)
│       └── yourprofile.addoners         (Custom)
└── logs/
    ├── latest.log                       (All logs)
    └── debug.log                        (Debug only)
```

---

## 🛠️ Development

This project is open source to promote transparency and community contribution.

### Contribute
- Report issues on GitHub
- Suggest new profile types
- Contribute shader-specific optimizations
- Submit custom `.addoners` profiles

### Build from Source
```powershell
git clone https://github.com/devharmanpreet/addoners-optimizers
cd addoners-optimizers
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
.\gradlew build
.\gradlew runClient
```

### Architecture
- **OptimizerEngine** — Core optimization scheduler
- **DynamicOptimizer** — Applies settings based on level
- **RuleEngine** — Evaluates FPS-based rules
- **ProfileManager** — Loads and manages profiles
- **ShaderDetector** — Detects active shaders
- **StatusOverlay** — Renders HUD

---

## 📜 License

This project is licensed under the **MIT License**.

---

## 💬 Credits

**Developed by**: Team Addoners  
**Contributors**: Community members and shader pack creators

---

## ⭐ Support

If you enjoy this project:
- ⭐ Star the repository
- 📢 Share with friends
- 🎥 Feature in content
- 💬 Give feedback

---

**Addoners Optimizer** — Performance made automatic ⚡

*For questions or support, open an issue on GitHub.*