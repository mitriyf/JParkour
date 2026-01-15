# 🐔 JParkour [![CodeFactor](https://www.codefactor.io/repository/github/mitriyf/jparkour/badge)](https://www.codefactor.io/repository/github/mitriyf/jparkour)
## 🗡️ Ride a chicken or any other entity by destroying objects
This plugin adds a mini-game in the form of various mobs that you can ride and knock down stands with.
- $ Versions 1.8.1-1.21+ are supported. It may be 1.7, but it has not been tested.
- $ Has been tested on versions: 1.8.8, 1.12.2, 1.16.5, 1.18.2, 1.21+.
- $ Some plugin updates on SpigotMC.ru may be delayed.
- $ There may be a future FAQ about the plugin here.
- $ Attention! An additional plugin is required for the plugin to work. You can read the requirements below.
## 🐓 Ride a chicken
Ride a chicken and experience a storm of emotions!

<img width="1920" height="1009" alt="2026-01-16_01 40 49" src="https://github.com/user-attachments/assets/acdd9458-3ee2-4f41-ad55-c28c041aff39" />

![2026-01-16 02-32-49](https://github.com/user-attachments/assets/d9783e3d-5c3a-4510-b5b2-1a62be7dbb89)

## 🪙 Knock down the stands
Destroy stands by identifying their type by color and the item inside.

<img width="1920" height="1009" alt="2026-01-16_02 43 59" src="https://github.com/user-attachments/assets/9e7f0457-f1b6-4163-9371-4e1512a7b43d" />

![2026-01-16 02-12-18](https://github.com/user-attachments/assets/a02945d4-a35e-499d-94e5-5c4f899cdcf2)

## 🥇 Finish and get the result!
Finish the race, find out your score, and return home!

<img width="1920" height="1009" alt="2026-01-16_02 37 39" src="https://github.com/user-attachments/assets/c0636ce8-7d0f-4f73-ba04-3f60dcc20203" />

![2026-01-16 02-37-32](https://github.com/user-attachments/assets/ca361be9-b7f3-439c-8f25-fdd24e27b69b)

## 🎮 The game process
The game process is watching videos (YouTube):
https://youtu.be/SZLOos1hHf0 or https://www.youtube.com/watch?v=SZLOos1hHf0
## 🚀 Requirements:
- FastAsyncWorldEdit (FAWE). You can download it here: https://intellectualsites.github.io/download/fawe.html
GitHub: https://github.com/IntellectualSites/FastAsyncWorldEdit
## 🌠 Optional requirements: 
- PlaceholderAPI. You can download it here: https://github.com/PlaceholderAPI/PlaceholderAPI/releases

<img width="556" height="177" alt="image" src="https://github.com/user-attachments/assets/cd03b156-8335-4e73-b372-d9633be14a37" />

## ⌨️ Commands (/jparkour):
- /jparkour status - Check the status of the plugin.
- /jparkour join - Create or join any available room.
- /jparkour join roomId - Attempt to connect to the room (if it is free).
- /jparkour exit - Exit the game/queue.
- /jparkour admin - Get a Admin Help.
- /jparkour admin add playerName - Add a player to a random game.
- /jparkour admin add playerName Map - Add a player to a specific game.
- /jparkour admin item - Get a Item Help.
- /jparkour admin gameeditor - Get a GameEditor Help.
- /jparkour admin gameeditor new - Create a new game schematic.
- /jparkour admin gameeditor new OtherGame - Create a new game schematic by copying another game schematic.
- /jparkour admin gameeditor list - Get a list of game schematics.
- /jparkour admin gameeditor remove Name - Delete the game schematic.
- /jparkour admin game - Set a GameEditor Settings.
- /jparkour admin game set pose 1/2/3/portal - Stand at the border of one of the points and select it.
- /jparkour admin game set stand Type - Strike the block where stand should be with the axe in your hands.
- /jparkour admin game set loc Type - Strike the block where loc should be with the axe in your hands.
- /jparkour admin game set point Number(1to∞) RadiusStartPoint(0.5/?.?) Teleportation(true/false) addX addY addZ Yaw Pitch - Go to the location where the point should be and enter this command. The normal block values will be taken (there is an add for this), and if yaw and pitch are not specified, the values that the player has looked at will be taken.
- /jparkour admin game get pose 1/2/3/portal - Find out the coordinates of the boundaries of point 1 or 2...
- /jparkour admin game get stand - Find out the type of block selected by the axe.
- /jparkour admin game get loc - Find out the type of block selected by the axe.
- /jparkour admin game get point number(1to∞) - Find out information about the point.
- /jparkour admin game get locs - Get all locs.
- /jparkour admin game get stands - Get all stands.
- /jparkour admin game get items - Get all Items.
- /jparkour admin game remove point number(1to∞) - Delete a point.
- /jparkour admin restart playerName - Restart the player's game.
- /jparkour admin updatetops - Update the tops.
- /jparkour admin kick playerName - Kick the player out of the game.
- /jparkour reload - Reload the plugin configuration.

## 📖 Permissions:
- **jparkour.help** - Can a player get help with subcommands?
- **jparkour.join** - Can a player join/exit games?
- **jparkour.status** - Can the player find out the status of the games?
- **jparkour.reload** - Can the player reload the plugin configuration?
- **jparkour.admin** - Can the player access the gameeditor, game, item, and other commands? + Removing restrictions on commands and walking through worlds.
- **jparkour.gameeditor** - Can a player access the AdminGameEditor?
- **jparkour.game** - Can a player access the game settings in the AdminGameEditor?
- **jparkour.item** - Can the player access item settings?

## 📝 Configurations:
You can view the configurations by going to src/main/resources/:
- locales/ru_RU.yml
- locales/en_US.yml
- locales/de_DE.yml
- config.yml
- config13.yml
- slots.yml
- slots13.yml
or by following the link:
- https://github.com/mitriyf/JParkour/blob/main/src/main/resources/locales/ru_RU.yml
- https://github.com/mitriyf/JParkour/blob/main/src/main/resources/locales/en_US.yml
- https://github.com/mitriyf/JParkour/blob/main/src/main/resources/locales/de_DE.yml
- https://github.com/mitriyf/JParkour/blob/main/src/main/resources/config.yml
- https://github.com/mitriyf/JParkour/blob/main/src/main/resources/config13.yml
- https://github.com/mitriyf/JParkour/blob/main/src/main/resources/slots.yml
- https://github.com/mitriyf/JParkour/blob/main/src/main/resources/slots13.yml
