
# Server Utils

For Fabric 1.18.2

A group of utilities for fabric servers.

# Installation

You can download a binary from the [Releases page](https://github.com/kyrptonaught/Server-Utils/releases) or compile it yourself with `./gradlew build`

# Modules

This mod has multiple modules, their usage instructions can be found below.

## Velocity Server Switch

The Velocity Powered Proxy includes a command to allow the player to switch between servers, but the command does not actually exist on the servers themselves. This causes issues when you'd like to run the command through another source like a datapack or a command block.

This module adds the following command: `/velocityserverswitch <servername>`

This command requires operator permissions, and works like any other command, interactable via datapacks, /execute, etc..

## Take Everything

Serverside module to easily take all items from an inventory. 
Taking everything from an inventory will also auto equip any armor items that you took and will swap armor if you are already wearing something of lesser value.

### Usage

If you are on 1.17, you may middle click in a container to take all items, you may also double click on an empty slot inside of conatiner to do the same.

If you are on 1.18 or higher, middle clicking will not work unless you are in creative, so you will need to double click on an empty slot.

If you are using a datapack, or having another mod run a command for you the command `/takeeverything` will take all items from a chest, as long as you have a container open while running it

If you'd like to disable the functionality of the mod, you may run the command `/takeeverything false` to disable it, and `/takeeverything true` to re-enable it.

## Velocity Command

This module adds 2 commands:

* `/velocityforward <selector> <speed>`
* `/modifyvelocity <selector> <add/set> <x> <y> <z>`

The speed and xyz options are a float

using the `add` option for modifyvelocity will add onto the player's current velocity, while `set` will replace their current velocity with your new values

## Extended Structures

This module makes a few improvements to structure blocks
* Extended size limit from 48x48x48 to 512x512x512
* Fixed [MC-102223](https://bugs.mojang.com/browse/MC-102223) (Paintings shifting when loaded by structure block)

Notice: You must have this mod installed on the server for any functionality, and on the client for interacting with structure blocks



## Scoreboard Suffix

This module adds two commands for adding a more customizable suffix for players in tablist.

### How to use

This module adds three commands: 

    /setScoreboardSuffix <format>
    /setSuffixFont <fontPlaceHolder> <font> [player]
    /scoreboardSuffixForceUpdate
`/setScoreboardSuffix` has it's own format, here's an example of how they work: `[{MinecraftJsonHere}, "String", "scoreboard=ScoreboardObjectiveHere"]`

`MinecraftJsonHere` Uses standard Minecraft tellraw JSON data, you can easily generate some using [MinecraftJson.com](https://www.minecraftjson.com/). Now when referencing a font, you can reference a placeholder, which can then be set by `/setSuffixFont`

`String` Is pretty self-explanitory, you can put a string of text here.

`scoreboard=ScoreboardObjectiveHere` Can reference a scoreboard objective here, and it will be displayed. Do keep in mind if a player is on a team with a color, that color will be applied to the number displayed.

`/setSuffixFont` Has 3 arguments `fontPlaceHolder`, `font`, and `player`

`fontPlaceHolder` Can be set in `/setScoreboardSuffix` when referencing a font.

`font` Is the font to set the placeholder to reference, if you are using a custom namespace in a resource pack, put the font in quotations, for example `"mypack:fontname"`.

`/scoreboardSuffixForceUpdate` Simply forces the tablist to update, you may need to do this when changing a team's prefix to make it appear in tablist correctly.

Here are some examples of these commands:

    /setScoreboardSuffix [{"text":"This is in uniform font","color":"white","font":"minecraft:uniform"}, {"text":"placeholder font!","color":"white","font":"font1"}, "scoreboard=killcount"]
    /setSuffixFont font1 "mypack:coolskeleton95" @a[gamemode=survival]

## SwitchableResourcepacks

This module adds a command to trigger a resourcepack to be sent to the client, using the same method as the built in required resourcepack function when joining a server.

### How to use

This mod adds one command: `/loadresource <packname> <selector>`

Executing this command will trigger the client to load a pack with the alias specified. `<packname>` being a config specified alias to a resourcepack. This can also be done from command blocks/datapacks using `/execute as`

The `<selector>` argument is optional, if it is not specified it will automatically run on the player running the command

SwitchableResourcepacks also makes use of advancement criteria to inform about the status of the client loading the resourcepack. This can be used with datapacks to test if the player has unlocked the advancement, thus giving an update on the clients progress, or if it failed.
The included criteria :

 - `switchableresourcepacks:started` - Triggers when the client accepts the resourcepack and loading begins.
 - `switchableresourcepacks:finished` - Triggers when the resourcepack finished loading successfully.
 - `switchableresourcepacks:failed` - Triggers if the client fails to load the pack.

An example advancement is available [here](https://github.com/kyrptonaught/SwitchableResourcepacks/blob/main/example/exampleadvancement.json).

**Config**

Upon first start up, a basic config will be generated featuring an example pack setup. It will look like [so](https://github.com/kyrptonaught/SwitchableResourcepacks/blob/main/example/exampleconfig.json5). 

**Adding a new Pack**

Adding a new pack is as simple as adding a new entry inside of the config. See the example config above. 
		
    {
    
    "packname": "example_pack" - The alias used for the command to load this pack.
    
    "url": "https://example.com/resourcepack.zip", - Public url used to download the pack
    
    "hash": "examplehash", - The pack's SHA-1 hash
    
    "required": true, - Is the user required to use this pack? If the user declines they wull be kicked
    
    "hasPrompt": true, - Is the user prompted to use the pack? The prompt will always show if required is true
    
    "message": "plz use me" - Message to be displayed with the prompt
    
		"packCompatibility": "BOTH" - Has 3 options, BOTH, OPTIFINE, and VANILLA. Used for lemclienthelper to identify pack compatibility
    
    }
The config also features `autoRevoke`. Enabling this will automatically revoke all of the above advancement criteria when another resourcepack is downloaded. If set to false, you must do the revoking on your own, else the criteria cannot be triggered again.

## Chest Tracker

This module adds one command with many arguments:

* `/chesttracker enabled true/false` - Enables or disables the module.
* `/chesttracker fillChests <x> <y> <z>` - Mark a chest as filled with coordinates, this will spawn particles above the chest for 40 seconds.
* `/chesttracker reset` - Resets the module.
* `/chesttracker scoreboardObjective <objective>` - Set a scoreboard objective to incriment whenever a player opens a chest they have never opened before

## Drop Event

This module adds a config file in `config/serverutils/dropevent.json5`

The command set in `runCommand` will run whenever a player tries to drop an item without anything in their hand.

## Health command

This module adds one command: `/health <selector> add/remove/set scoreboard/healthValue`

when using the `scoreboard` argument you can provide a scoreboard objective to set the player's health to

## Lockdown

This module adds one command: `/lockdown selector/clear/true/false`

When using a selector, you will be able to set it to `true/false` per-player, not using a selector will apply the lockdown to all players.

Clear will reset it for all players and disable it if its enabled globally

## Ride command

This module adds one command: `/ride <selector>`

The `<selector>` argument determines what entity the entity executing the command will start riding

This command can be used with `/execute as` to force any entity to ride another entity

## Water Freezer

This module adds one command: `/waterFreezer freeze true/false`

Setting it to true will disable all interactions with liquids, water and lava. This will allow you to have water and lava exist next to each other without them turning into stone/obsidian/cobblestone.
