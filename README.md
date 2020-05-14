# TGM [![Build Status](https://jenkins.bennydoesstuff.me/buildStatus/icon?job=TGM)](https://jenkins.bennydoesstuff.me/job/TGM) ![Minecraft Version](https://img.shields.io/badge/supports%20MC%20version-1.15.2-brightgreen.svg) [![Discord](https://img.shields.io/badge/chat-on%20discord-blue.svg)](https://warz.one/discord)
Team Game Manager. A Team Oriented Minecraft PVP plugin.

## Project Goals

1. **Advanced Game Engine with game logic implemented through modular programming.** 
Managers should offer hooks and data models to modules. 
Modules should be capable of communicating with one another.
The project should strive to make new game type development as straightforward as possible.

2. **Map.json Scripting Language.**
Maps need access to a baseline scripting service that allows for map-specific dynamic content.
As an example, a map should be able to provide different spawn points as the match time progresses.

3. This project is heavily influenced by [PGM](https://github.com/OvercastNetwork/ProjectAres). Our goal with TGM is to shift more of the game logic to Java as opposed to map configuration files. This allows for the rapid development and modernization of gamemodes over time. 

Here's a basic example of what map configuration files look like:
```json
"spawns": [
    {"teams": ["spectators"], "coords": "59, 48, 184.5, -180"},
    {"teams": ["red"], "coords": "149.5, 7, 184.5, 90"},
    {"teams": ["blue"], "coords": "-30.5, 7, 184.5, -90"}
],
"regions": [
    {"id": "red-spawn-protection", "type": "cuboid", "min": "126, 0, 168", "max": "152, oo, 199"},
    {"id": "blue-spawn-protection", "type": "cuboid", "min": "-8, 0, 198", "max": "-34, oo, 167"},

    {"id": "build-height", "type": "cuboid", "min": "-oo, 40, -oo", "max": "oo, oo, oo"}
],
"filters": [
    {
        "type": "enter", "evaluate": "deny", "teams": ["blue"],
        "regions": ["red-spawn-protection"], "message": "&cYou may not enter this region."
    },
    {
        "type": "enter", "evaluate": "deny", "teams": ["red"],
        "regions": ["blue-spawn-protection"], "message": "&cYou may not enter this region."
    },
    {
        "type": "build", "evaluate": "deny", "teams": ["red", "blue"],
        "regions": ["build-height"], "message": "&cYou have reached the max build height."
    }
]
```
  
  
## Local Server Setup
 
1. Start with the latest stable [Paper (PaperSpigot)](https://papermc.io/downloads) build. 

2. Compile the latest version of TGM or download it from our [Jenkins](https://jenkins.bennydoesstuff.me/job/TGM/).
 
3. Create a `Maps` folder in the root directory of your server and insert a supported TGM map. Make sure you also include a `rotation.txt` file with the names of maps you would like to be present in the rotation.
    - You can download our Maps folder as a reference on the Maps repository located [here](https://github.com/WarzoneMC/Maps).
    - If you would like to load multiple map repositories or simply change the location, you can change the setting in the `plugins/TGM/config.yml` file.
 
4. Start the server. 
   - Additionally, if you would like stats to be saved, you need to set up the API [here](https://github.com/WarzoneMC/api) and enable the API feature in the `plugins/TGM/config.yml` file.

5. (Optional) Install WorldEdit for added telelport tool functionallity

## Compiling

1. Clone the repo to your computer.

2. Compile CraftBukkit by downloading and running the BuildTools jar from [here](https://hub.spigotmc.org/jenkins/job/BuildTools). Make sure you include the `-compile craftbukkit` flag when compiling it, otherwise you might receive errors when later compiling the plugin.

3. Make sure you have Maven installed and run `mvn clean install` in the top-level folder to generate the required libraries. You can then download the jar from the `target` folder.

## Developer Tips

1. We use [Lombok](https://projectlombok.org/). Make sure you have the Lombok plugin installed on your preferred IDE.

## Documentation

This plugin takes advantage of a ``map.json`` required by every map to configure the gamemode and to document the coordinates needed by the gamemode to function.  To learn more about the available gamemodes in the plugin, how to configure a map.json, and what features are currently offered through this file, we recommend checking out our Documentation repository located [here](https://github.com/Warzone/Docs). If you need any additional examples on how to format the JSON's, consider checking out our Maps repository as well located [here](https://github.com/Warzone/Maps).
