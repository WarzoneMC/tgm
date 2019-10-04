![Minecraft Version](https://img.shields.io/badge/supports%20MC%20versions-1.13%20--%201.14.4-brightgreen.svg)
[![Build Status](https://jenkins.bennydoesstuff.me/buildStatus/icon?job=TGM)](https://jenkins.bennydoesstuff.me/job/TGM)
[![Discord](https://img.shields.io/badge/chat-on%20discord-blue.svg)](https://warz.one/discord)

# Warzone
Team Oriented Minecraft PVP Suite

## Project Goals

1. **Advanced Game Engine with game logic implemented through modular programming.** 
Managers should offer hooks and data models to modules. 
Modules should be capable of communicating with one another.
The project should strive to make new gametype development as straightforward as possible.

2. **Map.json Scripting Language.**
Maps need access to a baseline scripting service that allow for map-specific dynamic content.
As an example, a map should be able to provide different spawn points as the match time progresses.

3. This project is heavily influenced by [PGM](https://github.com/OvercastNetwork/ProjectAres). Our goal with TGM is to shift more of the game logic to Java as opposed to map configuration files. This allows for rapid development and modernization of gamemodes over time. 

```json
"spawns": [
    { 
        "teams": ["blue"], "x": 54.6, "y": 83.4, "z": 93.4, "yaw": 90,
        "conditions": ["time <= 120"]
    },
    { 
        "teams": ["blue"], "x": 54.6, "y": 83.4, "z": 93.4, "yaw": 90,
        "conditions": ["time > 120", "time < 240"]
    },
    { 
        "teams": ["blue"], "x": 54.6, "y": 83.4, "z": 93.4, "yaw": 90,
        "conditions": ["time >= 240"]
    },
    { 
        "teams": ["yellow"], "x": 54.6, "y": 83.4, "z": 93.4, "yaw": 90,
        "conditions": ["points yellow >= 10"]
    }
]
  ```
  
  
## Local Server Setup
 
1. Start with the latest stable [Paper (PaperSpigot)](https://papermc.io/downloads) build. 

2. Compile the latest version of TGM or download it from our [Jenkins](https://jenkins.bennydoesstuff.me/job/TGM/).
 
3. Create a `Maps` folder in the root folder and insert a supported TGM map. Make sure you also include a rotation.txt with the maps you would like to be present in the rotation.
    - You can download our Maps folder as a reference on the Maps repo located [here](https://github.com/WarzoneMC/Maps).
    - If you would like to access multiple repositories or simply change the repository location, you can change the repository location found in the `plugins/TGM/config.yml` file.
 
4. (Optional) Install WorldEdit to enable the Teleport Tool. 
 
5. Start the server. 
   - Additionally, if you would like stats to be saved, you need to setup the API [here](https://github.com/WarzoneMC/api) and enable the API feature in the `plugins/TGM/config.yml` file.
 
## Developer Tips

1. We use Lombok. Make sure you have the Lombok plugin installed on your preferred IDE.

2. We use maven. Like any other maven project, run `mvn clean install` in the top level folder to generate the required libraries.
