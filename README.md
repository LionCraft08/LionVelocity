# Features 
Info: The basic commands for Player Management are accessible through `/lvc`.<br>
This way it is easy to separate them from commands like `/whitelist` from the backend servers.<br>
Player management commands send requests to the Mojang Auth Servers to receive the uuid of a player.<br>
This does result in a delay visible at the command result delay.<br> 

## Whitelist
Implements the basics of every Server: A Proxy side Whitelist
```
/lvc whitelist [add|remove|toggle|list|...]
```
## Bans
Self-explaining 

```
/lvc [ban|unban] <playername> <duration>
```
## Operator
Makes a player velocity operator.<br>
This allows him to execute all velocity commands AND makes him operator on every backend Server with LionAPI installed.
```
/lvc op <playername>
```

## Shutdown
Allows you to shutdown the whole server network.
This will send a shutdown command to every backend server, kick every player with a configurable message and stop the proxy.
```
/shutdown <args>
```

## Send Players
You can send single players or whole servers to another backend server
```
/players send <player/server> <server>
```

## Auto Reconnect 
If a Server is resetting, players will be automatically reconnected to this Server. <br>
Requirements:<br>
The player has this feature enabled in the Settings<br>
The Server is connected to LionVelocity<br>
The player is still in the lobby when the server is back online.

# How to set up
0. Set up your Velocity connections to the backend [Velocity Docs](https://docs.papermc.io/velocity/getting-started/), and make sure you have lionAPI installed on every backend server you want to connect.
1. Identify the IP address and the port of the velocity server and the backend server you want to connect. If possible, take local addresses to reduce network traffic and ensure security. The port of the velocity can be configured in lionvelocity config.json. This means you don't need the port players are connected to, but rather another, custom configurable port. If you use a server hosting service, you need to ensure that the port is accessible by the backend.
2. Configure the backend Server: Open plugins/lionAPI/config.yml go to the path server-connection.velocity and enter the IP address, the port, set type to auto or direct, only use player if direct doesn't work.<br>
[Since version 1.2.0] Ensure to have a database set up to use shared player data. Configure the access in config.json.
3. Make sure to start the velocity server before the backend server and look for messages in both consoles.
4. If successful, enter the command shutdown in the velocity console and if the backend server shuts down as well, the connection is successful.