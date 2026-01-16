# 🐔 JParkour [![CodeFactor](https://www.codefactor.io/repository/github/mitriyf/jparkour/badge)](https://www.codefactor.io/repository/github/mitriyf/jparkour)
## 🗡️ Ride a chicken or any other entity by destroying objects
This plugin adds a mini-game in the form of various mobs that you can ride and knock down stands with.
- $ Versions 1.8.1-1.21+ are supported. It may be 1.7, but it has not been tested.
- $ Has been tested on versions: 1.8.8, 1.12.2, 1.16.5, 1.18.2, 1.21+. The best performance was observed on these versions: 1.8.8, 1.12.2, and 1.16.5.
- $ Some plugin updates on SpigotMC.ru may be delayed.
- $ There may be a future FAQ about the plugin here.
- $ Attention! An additional plugin is required for the plugin to work. You can read the requirements below.
- $ The plugin requires the server to be shut down properly, as if the server is killed (Killed java) during a player's game, it may have an abnormal number of hearts.
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
- You can watch the gameplay on YouTube:
  - https://youtu.be/SZLOos1hHf0
  - https://www.youtube.com/watch?v=SZLOos1hHf0
## 🚀 Requirements:
- FastAsyncWorldEdit (FAWE).
  - You can download it here: https://intellectualsites.github.io/download/fawe.html
  - GitHub: https://github.com/IntellectualSites/FastAsyncWorldEdit
## 🛠️ Supported:
### 🔮 Support HEX (1.16+, it will also work on lower versions, but without displaying the correct colors), MiniMessage (1.18+)
### 🌍 Languages:
- en_US (English (US))
- ru_RU (Russian)
- de_DE (German)
- Others (Don't forget to enable locales in the plugin configuration for language support)
  - $ You can find out the language code by clicking on the link below and looking at the In-Game section. Or you can find out your language code by running the command (don't forget to change the language on the client): **/jparkour admin locale**
  - $ https://minecraft.fandom.com/wiki/Language#Languages
  - $ You can configure locales in the plugin folder where the locales. The folder will be created when you enable locales in the plugin configuration. You can create your own files.
### 🌐 Plugins: 
- PlaceholderAPI (The tops and scoreboard system).
  - You can download it here: https://github.com/PlaceholderAPI/PlaceholderAPI/releases
  - Don't forget to enable it in the plugin configuration.
  - Placeholders:
    - %jparkour_map% - Displays the name of the map.
    - %jparkour_id% - Displays the ID of the map.
    - %jparkour_status% - Displays the status of the map.
    - %jparkour_lefts% - Displays the number of clicks on the stand.
    - %jparkour_maxlefts% - Displays the maximum number of clicks on the stand.
    - %jparkour_rights% - Displays the number of clicks on the bomb.
    - %jparkour_maxrights% - Displays the maximum number of bomb clicks.
    - %jparkour_tops_SchematicName_Number_name% - Get the name of a player who is on a specific top.
    - %jparkour_tops_SchematicName_Number_top% - Get a player's place in the top.
    - %jparkour_tops_SchematicName_Number_accuracy% - Get the accuracy of a player who is on a specific top.
    - %jparkour_tops_SchematicName_Number_time% - Get the exact completion time of a player who is on a specific top.
  - $ Change "SchematicName" to the schematics' ID, and "Number" to the number in the top that you require.
  - $ If you don't know the player's position in the top, you can get it by replacing "Number" with "name=PlayerName".
  - $ Enter the player's nickname in PlayerName. Examples: %jparkour_tops_nether_1_top%,
  - $ %jparkour_tops_nether_name=Mitriyf_top%, %jparkour_tops_nether_name=%player%_top%.
  - $ Ready-made configuration of scoreboards: https://github.com/mitriyf/JParkour/blob/main/downloads/tab/config.yml
<img width="556" height="177" alt="image" src="https://github.com/user-attachments/assets/cd03b156-8335-4e73-b372-d9633be14a37" />
<img width="280" height="192" alt="image" src="https://github.com/user-attachments/assets/4f9b944f-6356-429d-b3d3-0095b867dd8b" />

### 🔎 Checks:
- Automatic check for new versions that contain important updates. However, if it is a release without important fixes, there will be no alert unless it is enabled in the plugin configuration.
- Checking if there are any ready-made schematics. Automatic Downloading from GitHub if there are none: https://github.com/mitriyf/JParkour/tree/main/downloads
- The plugin will automatically detect your server version so that it starts working correctly with your project.
- Replacement of some parts of the configuration in case of their absence.
- Checking for an old configuration version and updating to a new one.
- Checking if a game with the same world name already exists.
- Checking whether the player is in the game.
- Checking if there is such an ID Map.
- Permissions verification.
- Fixing some user errors.
- And much more...

## ♾️ Functions:
### ✏️ Editor:
- You can configure maps very flexibly and conveniently using:
  - /jparkour admin gameeditor new - Create an empty map.
  - /jparkour admin gameeditor new nether - Copy a ready-made map and start working on it. (nether - any scheme).
  
  <img width="1920" height="1009" alt="2026-01-16_15 38 43" src="https://github.com/user-attachments/assets/ca884544-3fe9-4e3b-9c1b-e7947bb3dc0a" />
- You can change the stands and create any map you want.
- You can configure the gamerules and initial location in the configuration.
- Don't forget to select positions 1, 2 and 3 to create the map, otherwise you won't be able to save it!
  - /jparkour admin game set pose 1 - Select 1 position point, as in WorldEdit (always need to be adjusted).
  - /jparkour admin game set pose 2 - Select 2 position point, as in WorldEdit (always need to be adjusted).
  - /jparkour admin game set pose 3 - Select the 3 point position where the schematic will spawn. (only needed if you're making a map without copying someone else's settings).
- Also, don't forget about the first 4 locations that you need to set up (Select them with an axe and break the blocks where you don't need them):
  - /jparkour admin game set loc spawn - Enter this command when you select a block with an axe, and a player will spawn at that location with the settings from the config.
  - /jparkour admin game set pose portal - Approach the block that should start the game (determined by the material), and when the player enters it, the game will begin.
  - /jparkour admin game set loc start - Enter this command when you select the axe block, and the player will appear at this location when they enter the portal block. The game will begin.
  - /jparkour admin game set loc end - Enter this command when you select the end of the chicken run. At this point, the player will receive their results and be kicked.
- Save the game easily and simply:
  - /jparkour admin game save nameSchematic - Saving the game's layout and configuration will be successful if the positions and locations are selected.
### ⌨️ Command (/jparkour):
- /jparkour status - Check the status of the plugin.
- /jparkour join - Create or join any available room.
- /jparkour join roomId - Attempt to connect to the room (if it is free).
- /jparkour exit - Exit the game/queue.
- /jparkour admin - Get a Admin Help.
  - /jparkour admin add playerName - Add a player to a random game.
  - /jparkour admin add playerName Map - Add a player to a specific game.
  - /jparkour admin item - Get a Item Help.
    - /jparkour admin item add default/schematicName slot itemName - Add the item in your hand to the selected schematic.
    - /jparkour admin item list - Get a list of schematics.
    - /jparkour admin item list default/schematicName - Get a list of items in the selected schematic.
    - /jparkour admin item info default/schematicName itemName - Get the item from the selected schematic.
    - /jparkour admin item remove default/schematicName itemName - Remove an item from the selected schematic.
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
  - /jparkour admin locale - Get the client's language code.
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

## 🏃 Actions:
  - [actionbar] message - Send the actionbar with your message. For 1.11+
  - [connect] server - Send a player to a specific BungeeCord server.
    - WARNING: Requires BungeeMessaging. This is present on BungeeCord and WaterFall.
    - On Velocity it might be disabled by default. Check your proxy config.
  - [message] message - Send a message to the player.
  - [broadcast] message - Send a message to all players.
  - [log] message - Send a message to the console.
  - [delay] ticks - Make a delay between actions. In ticks. (20 ticks = 1 second)
  - [player] command - Run the command on behalf of the player.
  - [teleport] world;x;y;z;yaw;pitch;delay - Teleport the player to the specified coordinates. The delay is measured in ticks.
  - [console] command - Run the command on behalf of the console.
  - [title] title;subtitle;fadeIn;stay;fadeOut - Send the title to the player. For 1.8+
  - [sound] sound;volume;pitch;delay - Perform a sound for the player. The delay is measured in ticks.
    - Search for sounds here: https://helpch.at/docs/$version$/org/bukkit/Sound.html
    - // Replace $version$ with the version of your server, for example: https://helpch.at/docs/1.8.8/org/bukkit/Sound.html
  - [effect] type;duration;amplifier;delay - Give the effect to the player. The delay and duration is measured in ticks.
    - Find the types of effects here: https://helpch.at/docs/$version$/org/bukkit/potion/PotionEffectType.html
  - [explosion] power;setFire;breakBlocks;delay;addX;addY;addZ - Create an explosion. The delay is measured in ticks.
    - setFire, breakBlocks - set to false or true. addX, addY, addZ - double values that are added to the player's explosion location.
  - [bossbar] message;color;type;time;style;flag - Send a bossbar to a player with a message for a specific time. For 1.9+
    - Types:
      - stop - The bossbar will disappear after the time you specified in time (seconds)
      - time - Bossbar will animate the time that is running out.
    - Functions:
      - %time% - Seconds left.
    - You can find all the functions like color, style, and flag here: https://helpch.at/docs/$version$/org/bukkit/boss/BossBar.html
  - Built-in placeholders:
    - %accuracy% - Display the player's execution accuracy.
    - %star_win% - Displays the stars that the player has earned.
    - %star_loss% - Displays the stars that the player has not earned.
  - Info:
    - Messages sent to the console may not replace placeholders or perform certain actions above.
  - Built-in functions:
    - %player% - Get the player name.
## ⚙️ Config:
- Send actions to players using messages. (HEX support from 1.16+, MiniMessage support form 1.18+)
- Settings for schematics, default maps, and more.

## 🔐Storage:
- Backup of updated configurations after updating to a new plugin version.
- Automatic loading of schematics if they are missing.
- Automatic correction of broken configurations.
- Automatic folder creation.

## 🔄ConfigUpdater:
- The plugin will check all configuration conditions and update them as much as possible, and it will also create backups of previous ones.
- There are some moments where it can work when you are working with the editor.

### 📝 Configurations:
You can view the configurations by going to src/main/resources/:
- locales/ru_RU.yml
- locales/en_US.yml
- locales/de_DE.yml
- config.yml
- config13.yml
- slots.yml
- slots13.yml
- schematics/default.yml

or by following the link:
- https://github.com/mitriyf/JParkour/blob/main/src/main/resources/locales/ru_RU.yml
- https://github.com/mitriyf/JParkour/blob/main/src/main/resources/locales/en_US.yml
- https://github.com/mitriyf/JParkour/blob/main/src/main/resources/locales/de_DE.yml
- https://github.com/mitriyf/JParkour/blob/main/src/main/resources/config.yml
- https://github.com/mitriyf/JParkour/blob/main/src/main/resources/config13.yml
- https://github.com/mitriyf/JParkour/blob/main/src/main/resources/slots.yml
- https://github.com/mitriyf/JParkour/blob/main/src/main/resources/slots13.yml
- https://github.com/mitriyf/JParkour/blob/main/src/main/resources/schematics/default.yml

# 🤗 You can consider the rest of the possibilities when using the plugin.
