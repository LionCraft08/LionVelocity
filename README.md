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