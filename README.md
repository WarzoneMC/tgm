[![Build Status](https://jenkins.bennydoesstuff.me/buildStatus/icon?job=TGM)](https://jenkins.bennydoesstuff.me/job/TGM)
[![Discord](https://img.shields.io/badge/chat-on%20discord-blue.svg)](https://discord.io/WarzoneMC)

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
 
1. Start with the latest stable [Paper (PaperSpigot)](https://destroystokyo.com/ci/job/Paper/) build. 
 
2. Create a `maps` folder inside of the server and insert a supported TGM map. You can also just clone our `Maps` repository as a folder. 
 
4. (Optional) Install WorldEdit to enable the Teleport Tool. 
 
5. Start the server.
 
## Developer Tips

1. We use Lombok. Make sure you have the Lombok plugin installed on your preferred IDE.

2. We use maven. Like any other maven project, run `mvn clean install` in the top level folder to generate the required libraries.
