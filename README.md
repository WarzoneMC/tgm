# TeamGG
Team oriented minecraft pvp suite

## Project Goals

1. **Advanced Game Engine with game logic implementated through modular programming.** 
Managers should offer hooks and data models to modules. 
Modules should be capable of communicating with one another.
The project should strive to make new gametype development as straightforward as possible.

2. **Map.json scripting language.**
Maps need access to a baseline scripting service that allow for map-specific dynamic content.
As an example, a map should be able to provide different spawn points as the match time progresses.
```
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
