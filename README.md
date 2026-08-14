![Simple Config Lib](banner.png)  
  
# Simple Config Lib  
  
**Simple Config Lib** is a library for Minecraft mods designed to handle settings. It lets a developer describe a configuration with an ordinary Java class, after which the library automatically builds an in-game settings screen and takes care of saving and loading the data.  
  
The main goal of the project is to free developers from routinely implementing a settings system and to provide a single, predictable result across all supported platforms.  
  
> The library supports the **Fabric**, **Forge**, and **NeoForge** loaders.  
  
> Detailed documentation is available in the wiki: wiki_link  
  
![Test configuration screen](test_screen.png)  
  
---  
  
## Purpose  
  
The library provides a unified way to work with mod settings, covering configuration description, storage, and display.  
  
Simple Config Lib offers a single approach to solving this task:  
  
- the configuration is described once as a class;  
- the settings screen is generated automatically;  
- data is saved and loaded without any additional code;  
- behavior stays identical on Fabric, Forge, and NeoForge.  
  
This approach lets developers focus on the mod's functionality rather than on the infrastructure behind its settings.  
  
---  
  
## Who It's For  
  
- **Mod developers** — it is intended for those who want to give users a convenient settings screen without spending time implementing the interface and file handling themselves.  
- **Players** — the library is a dependency and is usually installed because another mod requires it. It needs no separate configuration and simply ensures that the settings of other mods work correctly.  
  
---  
  
## Features  
  
- Configuration described at the class level with automatic generation of the settings screen.  
- A ready-made settings screen that can be obtained with a single call right after describing the configuration — the library handles building the interface.  
- Automatic saving and loading of values without any manual file handling.  
- Consistent behavior across all supported loaders.  
- Automatic localization based on the configuration structure.  
- A rich set of interface elements for representing various data types.  
- A set of client-side helper utilities that simplify creating screens. Like the configuration itself, these utilities can be used independently of the game version.  
  
---  
  
## Installation  
  
On the **Fabric** loader, **Fabric API** must be installed additionally for the library to work. On **Forge** and **NeoForge**, no additional dependencies are required.  
  
---  
  
## FAQ  
  
**Do players need to install the library?**  
Only if another mod requires it. Simple Config Lib is a dependency and needs no separate configuration — it simply makes the settings of the mods that use it work.  
  
**Can the library be added to modpacks?**  
Yes. The library can be included in modpacks as long as a link to its original page is provided.  
  
**Does the library conflict with other mods?**  
No. Simple Config Lib does not interfere with other mods and does not conflict with them.  
  
**Do I need to write different code for different versions and loaders?**  
No. Both the configuration and the helper utilities are designed to be used independently of the game version and loader — the same code works everywhere.  
  
**Where do I report bugs and suggestions?**  
Bugs and suggestions can be reported on GitHub.  
  
---  
  
## Project Status  
  
Simple Config Lib was originally created for my own projects. I had to take it on because many existing libraries stopped supporting Forge on the newest versions of Minecraft.  
  
The project is open and continues to develop: the feature set will expand, and any bugs found will be fixed.
